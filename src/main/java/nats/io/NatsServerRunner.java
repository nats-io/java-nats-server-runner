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

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nats.io.NatsRunnerUtils.*;

public class NatsServerRunner implements AutoCloseable {

    private static Logger LOGGER = Logger.getLogger(NatsServerRunner.class.getName());

    private final int port;
    private final boolean debug;
    private final boolean jetstream;
    private final String[] customArgs;
    private final String[] configInserts;
    private final String configFilePath;

    private Process process;
    private String cmdLine;
    private final ProcessBuilder.Redirect errorRedirector = ProcessBuilder.Redirect.PIPE;
    private final ProcessBuilder.Redirect outputRedirector = ProcessBuilder.Redirect.PIPE;

    public void setLogger(Logger logger) {
        if (logger == null) {
            LOGGER = Logger.getLogger(NatsServerRunner.class.getName());
        }
        else {
            LOGGER = logger;
        }
    }

    public NatsServerRunner() throws IOException {
        this(0, false, false, null, null, null, true);
    }

    public NatsServerRunner(boolean debug) throws IOException {
        this(0, debug, false, null, null, null, true);
    }

    public NatsServerRunner(boolean debug, boolean jetstream) throws IOException {
        this(0, debug, jetstream, null, null, null, true);
    }

    public NatsServerRunner(int port, boolean debug) throws IOException {
        this(port, debug, false, null, null, null, true);
    }

    public NatsServerRunner(int port, boolean debug, boolean jetstream) throws IOException {
        this(port, debug, jetstream, null, null, null, true);
    }

    public NatsServerRunner(String configFilePath, boolean debug) throws IOException {
        this(0, debug, false, configFilePath, null, null, true);
    }

    public NatsServerRunner(String configFilePath, boolean debug, boolean jetstream) throws IOException {
        this(0, debug, jetstream, configFilePath, null, null, true);
    }

    public NatsServerRunner(String configFilePath, String[] configInserts, int port, boolean debug) throws IOException {
        this(port, debug, false, configFilePath, configInserts, null, true);
    }

    public NatsServerRunner(String configFilePath, int port, boolean debug) throws IOException {
        this(port, debug, false, configFilePath, null, null, true);
    }

    public NatsServerRunner(String[] customArgs, boolean debug) throws IOException {
        this(0, debug, false, null, null, customArgs, true);
    }

    public NatsServerRunner(String[] customArgs, int port, boolean debug) throws IOException {
        this(port, debug, false, null, null, customArgs, true);
    }

    public NatsServerRunner(int port, boolean debug, boolean jetstream, String configFilePath, String[] configInserts, String[] customArgs, boolean autoStart) throws IOException {
        this.port = port <= 0 ? nextPort() : port;
        this.debug = debug;
        this.jetstream = jetstream;
        this.customArgs = customArgs;
        this.configInserts = configInserts;
        this.configFilePath = configFilePath;
        if (autoStart) {
            start();
        }
    }


    public void start() throws IOException {
        List<String> cmd = new ArrayList<>();

        String server_path = System.getenv("nats_server_path");

        if (server_path == null) {
            server_path = NATS_SERVER;
        }

        cmd.add(server_path);

        // Rewrite the port to a new one, so we don't reuse the same one over and over
        if (configFilePath != null) {
            Pattern portPattern = Pattern.compile("port: (\\d+)");
            Matcher portMatcher = portPattern.matcher("");
            File tmp;

            try {
                tmp = File.createTempFile("nats_java_test", ".conf");
                BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));
                BufferedReader reader = new BufferedReader(new FileReader(configFilePath));

                String line = reader.readLine();
                while (line != null) {
                    portMatcher.reset(line);

                    if (portMatcher.find()) {
                        line = line.replace(portMatcher.group(1), String.valueOf(port));
                        cmd.add("--port");
                        cmd.add(String.valueOf(port));
                    }

                    writer.write(line);
                    writer.write("\n");

                    line = reader.readLine();
                }

                reader.close();

                if (configInserts != null) {
                    for (String s : configInserts) {
                        writer.write(s);
                        writer.write("\n");
                    }
                }

                writer.flush();
                writer.close();

                cmd.add("--config");
                cmd.add(tmp.getAbsolutePath());
            }
            catch (IOException ioe) {
                LOGGER.severe("%%% Error processing config file: " + ioe);
                throw ioe;
            }
        } else {
            cmd.add("--port");
            cmd.add(String.valueOf(port));
        }

        if (jetstream) {
            cmd.add("-js");
        }

        if (customArgs != null) {
            cmd.addAll(Arrays.asList(customArgs));
        }

        if (debug) {
            cmd.add("-DV");
        }

        cmdLine = String.join(" ", cmd);

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);

            pb.redirectErrorStream(true);
            pb.redirectError(errorRedirector);
            pb.redirectOutput(outputRedirector);
            LOGGER.info("%%% Starting [" + cmdLine + "] with redirected IO");

            process = pb.start();

            NatsOutputLogger.logOutput(LOGGER, process, NATS_SERVER, port);

            int tries = 10;
            // wait at least 1x and maybe 10
            do {
                try {
                    Thread.sleep(100);
                } catch (Exception exp) {
                    //Give the server time to get going
                }
                tries--;
            } while (!process.isAlive() && tries > 0);

            SocketAddress addr = new InetSocketAddress("localhost", port);
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            boolean scanning = true;
            while (scanning) {
                try {
                    socketChannel.connect(addr);
                } finally {
                    socketChannel.close();
                }
                scanning = false;
            }

            LOGGER.info("%%% Started [" + cmdLine + "]");
        } catch (IOException ex) {
            LOGGER.info("%%% Failed to start [" + cmdLine + "] with message:");
            LOGGER.info("\t" + ex.getMessage());
            LOGGER.info("%%% Make sure that the nats-server is installed and in your PATH.");
            LOGGER.info("%%% See https://github.com/nats-io/nats-server for information on installation");

            throw new IllegalStateException("Failed to run [" + cmdLine + "]");
        }
    }

    public int getPort() {
        return port;
    }

    public String getURI() {
        return getURIForPort(port);
    }

    public void shutdown(boolean wait) throws InterruptedException {

        if (process == null) {
            return;
        }

        process.destroy();

        LOGGER.info("%%% Shut down [" + cmdLine + "]");

        if (wait)
            process.waitFor();

        process = null;
    }

    public void shutdown() throws InterruptedException {
        shutdown(true);
    }

    /**
     * Synonymous with shutdown.
     */
    public void close() throws InterruptedException {
        shutdown();
    }
}