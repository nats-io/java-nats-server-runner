// Copyright 2020 The NATS Authors
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

public class NatsRunnerUtils {
    public static final String NATS_SERVER_PATH_ENV = "nats_server_path";
    public static final String DEFAULT_NATS_SERVER = "nats-server";

    public static final String CONFIG_FILE_OPTION_NAME = "--config";
    public static final String VERSION_OPTION = "--version";
    public static final String DEBUG_OPTION = "-DV";
    public static final String JETSTREAM_OPTION = "-js";

    public static final String CONF_FILE_PREFIX = "nats_java_test";
    public static final String CONF_FILE_EXT = ".conf";
    public static final String PORT_REGEX = "port: (\\d+)";
    public static final String PORT_PROPERTY = "port: ";

    private static String SERVER_PATH = null;

    /**
     * Build a standard nats://localhost:port uri
     * @param port the port
     * @return the uri
     */
    public static String getURIForPort(int port) {
        return "nats://localhost:" + port;
    }

    /**
     * Set the path for the
     * @param serverPath
     */
    public static void setServerPath(String serverPath) {
        SERVER_PATH = serverPath;
    }

    public static void clearServerPath() {
        SERVER_PATH = null;
    }

    public static String getResolvedServerPath() {
        String serverPath = System.getenv(NATS_SERVER_PATH_ENV);
        if (serverPath == null){
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
            ArrayList<String> lines = new ArrayList<String>();
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
}
