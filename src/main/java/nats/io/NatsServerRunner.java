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

    private static final Logger LOGGER = Logger.getLogger(NatsServerRunner.class.getName());

    private final int _port;
    private final File configFile;

    private Process process;
    private final String cmdLine;

    /**
     * Construct and start the Nats Server runner with all defaults:
     * <ul>
     * <li>use an automatically allocated port</li>
     * <li>no debug flag</li>
     * <li>jetstream not enabled</li>
     * <li>no custom config file</li>
     * <li>no config inserts</li>
     * <li>no custom args</li>
     * </ul>
     * @throws IOException thrown when the server cannot start
     */
    public NatsServerRunner() throws IOException {
        this(0, false, false, null, null, null);
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * <ul>
     * <li>use an automatically allocated port</li>
     * <li>jetstream not enabled</li>
     * <li>no custom config file</li>
     * <li>no config inserts</li>
     * <li>no custom args</li>
     * </ul>
     * and this option:
     * @param debug whether to start the server with the -DV flags
     * @throws IOException thrown when the server cannot start
     */
    public NatsServerRunner(boolean debug) throws IOException {
        this(0, debug, false, null, null, null);
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * <ul>
     * <li>use an automatically allocated port</li>
     * <li>no custom config file</li>
     * <li>no config inserts</li>
     * <li>no custom args</li>
     * </ul>
     * and these options:
     * @param debug whether to start the server with the -DV flags
     * @param jetstream whether to enable JetStream
     * @throws IOException thrown when the server cannot start
     */
    public NatsServerRunner(boolean debug, boolean jetstream) throws IOException {
        this(0, debug, jetstream, null, null, null);
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * <ul>
     * <li>jetstream not enabled</li>
     * <li>no custom config file</li>
     * <li>no config inserts</li>
     * <li>no custom args</li>
     * </ul>
     * and these options:
     * @param port the port to start on or &lt;=0 to use an automatically allocated port
     * @param debug whether to start the server with the -DV flags
     * @throws IOException thrown when the server cannot start
     */
    public NatsServerRunner(int port, boolean debug) throws IOException {
        this(port, debug, false, null, null, null);
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * <ul>
     * <li>no custom config file</li>
     * <li>no config inserts</li>
     * <li>no custom args</li>
     * </ul>
     * and these options:
     * @param port the port to start on or &lt;=0 to use an automatically allocated port
     * @param debug whether to start the server with the -DV flags
     * @param jetstream whether to enable JetStream
     * @throws IOException thrown when the server cannot start
     */
    public NatsServerRunner(int port, boolean debug, boolean jetstream) throws IOException {
        this(port, debug, jetstream, null, null, null);
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * <ul>
     * <li>use an automatically allocated port</li>
     * <li>jetstream not enabled</li>
     * <li>no config inserts</li>
     * <li>no custom args</li>
     * </ul>
     * and these options:
     * @param debug whether to start the server with the -DV flags
     * @param configFilePath path to a custom config file
     * @throws IOException thrown when the server cannot start
     */
    public NatsServerRunner(String configFilePath, boolean debug) throws IOException {
        this(0, debug, false, configFilePath, null, null);
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * <ul>
     * <li>use an automatically allocated port</li>
     * <li>jetstream not enabled</li>
     * <li>no custom config file</li>
     * </ul>
     * and these options:
     * @param debug whether to start the server with the -DV flags
     * @param jetstream whether to enable JetStream
     * @param configFilePath path to a custom config file
     * @throws IOException thrown when the server cannot start
     */
    public NatsServerRunner(String configFilePath, boolean debug, boolean jetstream) throws IOException {
        this(0, debug, jetstream, configFilePath, null, null);
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * <ul>
     * <li>jetstream not enabled</li>
     * <li>no custom args</li>
     * </ul>
     * and these options:
     * @param port the port to start on or &lt;=0 to use an automatically allocated port
     * @param debug whether to start the server with the -DV flags
     * @param configFilePath path to a custom config file
     * @param configInserts an array of custom lines to add to the config file
     * @throws IOException thrown when the server cannot start
     */
    public NatsServerRunner(String configFilePath, String[] configInserts, int port, boolean debug) throws IOException {
        this(port, debug, false, configFilePath, configInserts, null);
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * <ul>
     * <li>jetstream not enabled</li>
     * <li>no config inserts</li>
     * <li>no custom args</li>
     * </ul>
     * and these options:
     * @param port the port to start on or &lt;=0 to use an automatically allocated port
     * @param debug whether to start the server with the -DV flags
     * @param configFilePath path to a custom config file
     * @throws IOException thrown when the server cannot start
     */
    public NatsServerRunner(String configFilePath, int port, boolean debug) throws IOException {
        this(port, debug, false, configFilePath, null, null);
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * <ul>
     * <li>use an automatically allocated port</li>
     * <li>no debug flag</li>
     * <li>jetstream not enabled</li>
     * <li>no custom config file</li>
     * <li>no config inserts</li>
     * </ul>
     * and these options:
     * @param customArgs any custom args to add to the command line
     * @throws IOException thrown when the server cannot start
     */
    public NatsServerRunner(String[] customArgs) throws IOException {
        this(0, false, false, null, null, customArgs);
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * <ul>
     * <li>use an automatically allocated port</li>
     * <li>jetstream not enabled</li>
     * <li>no custom config file</li>
     * <li>no config inserts</li>
     * </ul>
     * and these options:
     * @param debug whether to start the server with the -DV flags
     * @param customArgs any custom args to add to the command line
     * @throws IOException thrown when the server cannot start
     */
    public NatsServerRunner(String[] customArgs, boolean debug) throws IOException {
        this(0, debug, false, null, null, customArgs);
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * <ul>
     * <li>use an automatically allocated port</li>
     * <li>no custom config file</li>
     * <li>no config inserts</li>
     * </ul>
     * and these options:
     * @param customArgs any custom args to add to the command line
     * @param debug whether to start the server with the -DV flags
     * @param jetstream whether to enable JetStream
     * @throws IOException thrown when the server cannot start
     */
    public NatsServerRunner(String[] customArgs, boolean debug, boolean jetstream) throws IOException {
        this(0, debug, jetstream, null, null, customArgs);
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * <ul>
     * <li>jetstream not enabled</li>
     * <li>no custom config file</li>
     * <li>no config inserts</li>
     * </ul>
     * and these options:
     * @param customArgs any custom args to add to the command line
     * @param port the port to start on or &lt;=0 to use an automatically allocated port
     * @param debug whether to start the server with the -DV flags
     * @throws IOException thrown when the server cannot start
     */
    public NatsServerRunner(String[] customArgs, int port, boolean debug) throws IOException {
        this(port, debug, false, null, null, customArgs);
    }


    /**
     * Construct and start the Nats Server runner with options
     *
     * @param port the port to start on or &lt;=0 to use an automatically allocated port
     * @param debug whether to start the server with the -DV flags
     * @param jetstream whether to enable JetStream
     * @param configFilePath path to a custom config file
     * @param configInserts an array of custom lines to add to the config file
     * @param customArgs any custom args to add to the command line
     * @throws IOException thrown when the server cannot start
     */
    public NatsServerRunner(int port, boolean debug, boolean jetstream, String configFilePath, String[] configInserts, String[] customArgs) throws IOException {
        _port = port <= 0 ? nextPort() : port;

        List<String> cmd = new ArrayList<>();

        cmd.add(getResolvedServerPath());

        try {
            configFile = File.createTempFile(CONF_FILE_PREFIX, CONF_FILE_EXT);
            BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
            if (configFilePath == null || processSuppliedConfigFile(writer, configFilePath)) {
                writePortLine(writer, _port);
            }

            if (configInserts != null) {
                for (String s : configInserts) {
                    writeLine(writer, s);
                }
            }

            writer.flush();
            writer.close();

            cmd.add(CONFIG_FILE_OPTION_NAME);
            cmd.add(configFile.getAbsolutePath());
        }
        catch (IOException ioe) {
            LOGGER.severe("%%% Error creating config file: " + ioe);
            throw ioe;
        }

        // Rewrite the port to a new one, so we don't reuse the same one over and over

        if (jetstream) {
            cmd.add(JETSTREAM_OPTION);
        }

        if (customArgs != null) {
            cmd.addAll(Arrays.asList(customArgs));
        }

        if (debug) {
            cmd.add(DEBUG_OPTION);
        }

        cmdLine = String.join(" ", cmd);

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            pb.redirectError(ProcessBuilder.Redirect.PIPE);
            pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
            LOGGER.info("%%% Starting [" + cmdLine + "] with redirected IO");

            process = pb.start();

            NatsOutputLogger.logOutput(LOGGER, process, DEFAULT_NATS_SERVER, _port);

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

            SocketAddress addr = new InetSocketAddress("localhost", _port);
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

    private boolean processSuppliedConfigFile(BufferedWriter writer, String configFilePath) throws IOException {
        Pattern portPattern = Pattern.compile(PORT_REGEX);
        Matcher portMatcher = portPattern.matcher("");

        BufferedReader reader = new BufferedReader(new FileReader(configFilePath));

        boolean needsPortLine = true;
        String line = reader.readLine();
        while (line != null) {
            portMatcher.reset(line);

            // replacing it here allows us to not care if the port is at the top level
            // or for instance inside a websocket block
            if (portMatcher.find()) {
                writeLine(writer, line.replace(portMatcher.group(1), String.valueOf(_port)));
                needsPortLine = false;
            }
            else {
                writeLine(writer, line);
            }

            line = reader.readLine();
        }

        reader.close();

        return needsPortLine;
    }

    private void writePortLine(BufferedWriter writer, int port) throws IOException {
        writeLine(writer, PORT_PROPERTY + port);
    }

    private void writeLine(BufferedWriter writer, String line) throws IOException {
        writer.write(line);
        writer.write("\n");
    }

    /**
     * Get the port number. Useful if it was automatically assigned
     *
     * @return the port number
     */
    public int getPort() {
        return _port;
    }

    /**
     * Get the absolute path of the config file
     *
     * @return the path
     */
    public String getConfigFile() {
        return configFile.getAbsolutePath();
    }

    /**
     * Get the uri in the form nats://localhost:port
     *
     * @return the uri string
     */
    public String getURI() {
        return getURIForPort(_port);
    }

    /**
     * Get the command line used to start the server
     *
     * @return the command line
     */
    public String getCmdLine() {
        return cmdLine;
    }

    /**
     * Shut the server down
     *
     * @param wait whether to block while waiting for the process to shutdown
     * @throws InterruptedException if the wait was interrupted
     */
    public void shutdown(boolean wait) throws InterruptedException {
        if (process != null) {
            process.destroy();
            LOGGER.info("%%% Shut down [" + cmdLine + "]");
            if (wait) {
                process.waitFor();
            }
            process = null;
        }
    }

    /**
     * Shut the server down, waiting (blocking)
     *
     * @throws InterruptedException if the wait was interrupted
     */
    public void shutdown() throws InterruptedException {
        shutdown(true);
    }

    /**
     * For AutoCloseable, synonymous with shutdown.
     */
    public void close() throws InterruptedException {
        shutdown();
    }
}