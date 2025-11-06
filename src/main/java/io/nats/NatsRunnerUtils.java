// Copyright 2020-2025 The NATS Authors
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

package io.nats;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public static final String PORT_MAPPED_REGEX = "port: <(\\w+)>";
    public static final String PORT_PROPERTY = "port: ";
    public static final String CONFIG_PORT_KEY = "config_port";
    public static final String USER_PORT_KEY = "user_port";
    public static final String NATS_PORT_KEY = "nats_port";
    public static final String NON_NATS_PORT_KEY = "non_nats_port";
    public static final int DEFAULT_CLUSTER_COUNT = 3;
    public static final String DEFAULT_CLUSTER_NAME = "cluster";
    public static final String DEFAULT_SERVER_NAME_PREFIX = "server";
    public static final String NATS = "nats";
    public static final String LOCALHOST = "localhost";

    public static String DEFAULT_HOST = "127.0.0.1";
    public static int DEFAULT_PORT_START = 4220;
    public static int DEFAULT_LISTEN_START = 4230;
    public static int DEFAULT_MONITOR_START = 4280;

    private NatsRunnerUtils() {}

    /**
     * Build a nats://localhost:port uri
     * @param port the port
     * @return the uri
     */
    public static String getNatsLocalhostUri(int port) {
        return getUri(NATS, LOCALHOST, port);
    }

    /**
     * Build a nats://host:port uri
     * @param host the host
     * @param port the port
     * @return the uri
     */
    public static String getNatsUri(String host, int port) {
        return getUri(NATS, host, port);
    }

    /**
     * Build a schema://localhost:port uri
     * @param schema the schema
     * @param port the port
     * @return the uri
     */
    public static String getLocalhostUri(String schema, int port) {
        return getUri(schema, LOCALHOST, port);
    }

    /**
     * Build a schema://host:port uri
     * @param schema the schema
     * @param host the host
     * @param port the port
     * @return the uri
     */
    public static String getUri(String schema, String host, int port) {
        return schema + "://" + host + ":" + port;
    }

    /**
     * Resolves the server executable path in this order:
     * <ol>
     * <li>Checking the {@value #NATS_SERVER_PATH_ENV} environment variable</li>
     * <li>Checking the value set via {@link NatsServerRunner#setPreferredServerPath} method</li>
     * <li>{@value #DEFAULT_NATS_SERVER}</li>
     * </ol>
     * @return the resolved path
     */
    public static String getResolvedServerPath() {
        String serverPath = NatsServerRunner.getPreferredServerPath();
        if (serverPath == null) {
            serverPath = System.getenv(NATS_SERVER_PATH_ENV);
            if (serverPath == null) {
                serverPath = DEFAULT_NATS_SERVER;
            }
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
     * Using the server resolved by {@link #getResolvedServerPath}
     * @return the version string
     */
    public static String getNatsServerVersionString() {
        return getNatsServerVersionString(getResolvedServerPath());
    }


    /**
     * Get the version string from the nats server i.e. nats-server: v2.2.2
     * @param serverPath the specific server path to check
     * @return the version string
     */
    public static String getNatsServerVersionString(String serverPath) {
        ArrayList<String> cmd = new ArrayList<String>();

        // order of precedence is environment, value set statically, default

        cmd.add(serverPath);
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
        return createClusterInserts(DEFAULT_CLUSTER_COUNT, DEFAULT_CLUSTER_NAME, DEFAULT_SERVER_NAME_PREFIX, false, null);
    }

    public static List<ClusterInsert> createClusterInserts(Path jsStoreDirBase) throws IOException {
        return createClusterInserts(DEFAULT_CLUSTER_COUNT, DEFAULT_CLUSTER_NAME, DEFAULT_SERVER_NAME_PREFIX, false, jsStoreDirBase);
    }

    public static List<ClusterInsert> createClusterInserts(int count) throws IOException {
        return createClusterInserts(count, DEFAULT_CLUSTER_NAME, DEFAULT_SERVER_NAME_PREFIX, false, null);
    }

    public static List<ClusterInsert> createClusterInserts(int count, Path jsStoreDirBase) throws IOException {
        return createClusterInserts(count, DEFAULT_CLUSTER_NAME, DEFAULT_SERVER_NAME_PREFIX, false, jsStoreDirBase);
    }

    public static List<ClusterInsert> createClusterInserts(int count, String clusterName, String serverNamePrefix) throws IOException {
        return createClusterInserts(createNodes(count, clusterName, serverNamePrefix, false, null));
    }

    public static List<ClusterInsert> createClusterInserts(int count, String clusterName, String serverNamePrefix, Path jsStoreDirBase) throws IOException {
        return createClusterInserts(createNodes(count, clusterName, serverNamePrefix, false, jsStoreDirBase));
    }

    public static List<ClusterInsert> createClusterInserts(int count, String clusterName, String serverNamePrefix, boolean monitor) throws IOException {
        return createClusterInserts(createNodes(count, clusterName, serverNamePrefix, monitor, null));
    }

    public static List<ClusterInsert> createClusterInserts(int count, String clusterName, String serverNamePrefix, boolean monitor, Path jsStoreDirBase) throws IOException {
        return createClusterInserts(createNodes(count, clusterName, serverNamePrefix, monitor, jsStoreDirBase));
    }

    public static Path getTemporaryJetStreamStoreDirBase() throws IOException {
       return Files.createTempDirectory(null);
    }

    public static void defaultHost(String defaultHost) {
        DEFAULT_HOST = defaultHost;
    }

    public static void defaultPortStart(int defaultPortStart) {
        DEFAULT_PORT_START = defaultPortStart;
    }

    public static void defaultListenStart(int defaultListenStart) {
        DEFAULT_LISTEN_START = defaultListenStart;
    }

    public static void defaultMonitorStart(int defaultMonitorStart) {
        DEFAULT_MONITOR_START = defaultMonitorStart;
    }

    public static List<ClusterNode> createNodes(int count, String clusterName, String serverNamePrefix, boolean monitor, Path jsStoreDirBase) {
        return createNodes(count, clusterName, serverNamePrefix, jsStoreDirBase,
            DEFAULT_HOST, DEFAULT_PORT_START, DEFAULT_LISTEN_START,
            monitor ? DEFAULT_MONITOR_START : null);
    }

    public static List<ClusterNode> createNodes(int count, String clusterName, String serverNamePrefix, Path jsStoreDirBase,
                                                String host, int portStart, int listenStart, Integer monitorStart) {
        List<ClusterNode> nodes = new ArrayList<>();
        for (int x = 0; x < count; x++) {
            int port = portStart + x;
            int listen = listenStart + x;
            Integer monitor = monitorStart == null ? null : monitorStart + x;
            Path jsStoreDir = jsStoreDirBase == null ? null : Paths.get(jsStoreDirBase.toString(), "" + port);
            nodes.add( new ClusterNode(clusterName, serverNamePrefix + x, host, port, listen, monitor, jsStoreDir));
        }
        return nodes;
    }

    public static List<ClusterInsert> createClusterInserts(List<ClusterNode> nodes) {
        List<ClusterInsert> inserts = new ArrayList<>();
        for (ClusterNode node : nodes) {
            List<String> lines = new ArrayList<>();
            lines.add("port: " + node.port);
            if (node.monitor != null) {
                lines.add("http: " + node.monitor);
            }
            if (node.jsStoreDir != null) {
                String dir = node.jsStoreDir.toString();
                if (File.separatorChar == '\\') {
                    dir = dir.replace("\\", "\\\\").replace("/", "\\\\");
                }
                else {
                    dir = dir.replace("\\", "/");
                }
                lines.add("jetstream {");
                lines.add("    store_dir=" + dir);
                lines.add("}");
            }
            lines.add("server_name=" + node.serverName);
            lines.add("cluster {");
            lines.add("  name: " + node.clusterName);
            lines.add("  listen: " + node.host + ":" + node.listen);
            lines.add("  routes: [");
            for (ClusterNode routeNode : nodes) {
                if (!routeNode.serverName.equals(node.serverName)) {
                    lines.add("    nats-route://" + node.host + ":" + routeNode.listen);
                }
            }
            lines.add("  ]");
            lines.add("}");
            inserts.add(new ClusterInsert(node, lines.toArray(new String[0])));
        }
        return inserts;

    }
}
