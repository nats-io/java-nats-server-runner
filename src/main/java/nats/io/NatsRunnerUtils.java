// Copyright 2020-2023 The NATS Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package nats.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public abstract class NatsRunnerUtils {
    public static final String NATS_SERVER_PATH_ENV = "nats_server_path";
    public static final String DEFAULT_NATS_SERVER = "nats-server";

    public static final String CONFIG_FILE_OPTION_NAME = "--config";
    public static final String VERSION_OPTION = "--version";
    public static final String JETSTREAM_OPTION = "-js";

    public static final String CONF_FILE_PREFIX = "nats_java_test";
    public static final String CONF_FILE_EXT = ".conf";
    public static final String PORT_REGEX = "port: (\\d+)";
    public static final String PORT_PROPERTY = "port: ";

    private static String SERVER_PATH = null;

    private NatsRunnerUtils() {}

    /**
     * Build a standard nats://localhost:port uri
     * @param port the port
     * @return the uri
     * @deprecated Use {@link #getNatsLocalhostUri(int)} instead.
     */
    @Deprecated
    public static String getURIForPort(int port) {
        return getLocalhostUri("nats", port);
    }

    /**
     * Build a nats://localhost:port uri
     * @param port the port
     * @return the uri
     */
    public static String getNatsLocalhostUri(int port) {
        return getLocalhostUri("nats", port);
    }

    /**
     * Build a schema://localhost:port uri
     * @param schema the schema
     * @param port the port
     * @return the uri
     */
    public static String getLocalhostUri(String schema, int port) {
        return schema + "://localhost:" + port;
    }

    /**
     * Set the path for the server. Will be used if {@value #NATS_SERVER_PATH_ENV} environment variable is not set.
     * @deprecated Use {@link NatsServerRunner.Builder} instead
     * @param serverPath the fully qualified path of the server
     */
    @Deprecated
    public static void setServerPath(String serverPath) {
        SERVER_PATH = serverPath;
    }

    /**
     * Clear the path for the server. Will use {@value #DEFAULT_NATS_SERVER}
     * if {@value #NATS_SERVER_PATH_ENV} environment variable is not set.
     * @deprecated Use {@link NatsServerRunner.Builder} instead
     */
    @Deprecated
    public static void clearServerPath() {
        SERVER_PATH = null;
    }

    /**
     * Resolves the server executable path in this order:
     * <ol>
     * <li>Checking the {@value #NATS_SERVER_PATH_ENV} environment variable</li>
     * <li>Checking the value set via {#setServerPath} method</li>
     * <li>{@value #DEFAULT_NATS_SERVER}</li>
     * </ol>
     * @return the resolved path
     */
    public static String getResolvedServerPath() {
        String serverPath = System.getenv(NATS_SERVER_PATH_ENV);
        if (serverPath == null) {
            serverPath = SERVER_PATH == null ? DEFAULT_NATS_SERVER : SERVER_PATH;
        }
        return serverPath;
    }

    /**
     * Get a port number automatically allocated by the system, typically from an ephemeral port range.
     * @return the port number
     * @throws IOException if there is a problem getting a port
     */
    public static int nextPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            while ( !socket.isBound() ) {
                //noinspection BusyWait
                Thread.sleep(50);
            }
            return socket.getLocalPort();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Thread interrupted", e);
        }
    }

    /**
     * Get the version string from the nats server i.e. nats-server: v2.2.2
     * @return the version string
     */
    public static String getNatsServerVersionString() {
        ArrayList<String> cmd = new ArrayList<String>();

        // order of precedence is environment, value set statically, default

        cmd.add(getResolvedServerPath());
        cmd.add(VERSION_OPTION);

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process process = pb.start();
            if (0 != process.waitFor()) {
                throw new IllegalStateException(String.format("Process %s failed", pb.command()));
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            ArrayList<String> lines = new ArrayList<>();
            String line = "";
            while ((line = reader.readLine())!= null) {
                lines.add(line);
            }

            if (lines.size() > 0) {
                return lines.get(0);
            }

            return null;
        }
        catch (Exception exp) {
            return null;
        }
    }

    public static List<ClusterInsert> createClusterInserts() throws IOException {
        return createClusterInserts(3, "clstr", "srvr");
    }

    public static List<ClusterInsert> createClusterInserts(int count) throws IOException {
        return createClusterInserts(count, "clstr", "srvr");
    }

    public static List<ClusterInsert> createClusterInserts(int count, String clusterName, String serverNamePrefix) throws IOException {
        List<ClusterInsert> clusterInserts = new ArrayList<>();
        for (int x = 0; x < count; x++) {
            ClusterInsert ci = new ClusterInsert();
            ci.id = x + 1;
            ci.port = nextPort();
            ci.listen = nextPort();
            clusterInserts.add(ci);
        }
        return finishCreateClusterInserts(clusterName, serverNamePrefix, clusterInserts);
    }

    public static List<ClusterInsert> createClusterInserts(int[] ports, int[] listens, String clusterName, String serverNamePrefix) throws IOException {
        List<ClusterInsert> clusterInserts = new ArrayList<>();
        for (int x = 0; x < ports.length; x++) {
            ClusterInsert ci = new ClusterInsert();
            ci.id = x + 1;
            ci.port = ports[x];
            ci.listen = listens[x];
            clusterInserts.add(ci);
        }
        return finishCreateClusterInserts(clusterName, serverNamePrefix, clusterInserts);
    }

    private static List<ClusterInsert> finishCreateClusterInserts(String clusterName, String serverNamePrefix, List<ClusterInsert> clusterInserts) {
        for (ClusterInsert ci : clusterInserts) {
            List<String> lines = new ArrayList<>();
            lines.add("server_name=" + serverNamePrefix + ci.id);
            lines.add("cluster {");
            lines.add("  name: " + clusterName);
            lines.add("  listen: 127.0.0.1:" + ci.listen);
            lines.add("  routes: [");
            for (ClusterInsert ciRoutes : clusterInserts) {
                if (ciRoutes.id != ci.id) {
                    lines.add("    nats-route://127.0.0.1:" + ciRoutes.listen);
                }
            }
            lines.add("  ]");
            lines.add("}");
            ci.setInsert(lines);
        }
        return clusterInserts;
    }
}
