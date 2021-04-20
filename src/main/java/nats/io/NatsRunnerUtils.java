package nats.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;

public class NatsRunnerUtils {
    public static final String NATS_SERVER = "nats-server";

    public static String getURIForPort(int port) {
        return "nats://localhost:" + port;
    }

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
