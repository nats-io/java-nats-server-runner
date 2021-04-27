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
    public static final String NATS_SERVER = "nats-server";

    /**
     * Build a standard nats://localhost:port uri
     * @param port the port
     * @return the uri
     */
    public static String getURIForPort(int port) {
        return "nats://localhost:" + port;
    }

    /**
     * Get a port number automatically allocated by the system, typically from an ephemeral port range.
     * @return the port number
     * @throws IOException if there is a problem getting a port
     */
    public static int nextPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            while ( !socket.isBound() ) {
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

        String server_path = System.getenv("nats_server_path");

        if(server_path == null){
            server_path = NATS_SERVER;
        }

        cmd.add(server_path);
        cmd.add("--version");

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
