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

package io.nats;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.nats.NatsRunnerUtils.*;

/**
 * Server Runner
 */
public class NatsServerRunner implements AutoCloseable {
    public static final String ERROR_NOTE_PART_1 = "Make sure that the nats-server is installed and in your PATH.";
    public static final String ERROR_NOTE_PART_2 = "See https://github.com/nats-io/nats-server for information on installation";
    public static long DEFAULT_PROCESS_CHECK_WAIT = 100;
    public static int DEFAULT_PROCESS_CHECK_TRIES = 10;
    public static long DEFAULT_RUN_CHECK_WAIT = 100;
    public static int DEFAULT_RUN_CHECK_TRIES = 3;

    private final String _executablePath;
    private final Output _displayOut;
    private final Map<String, Integer> _ports;
    private final File _configFile;
    private final String _cmdLine;
    private Process process;

    /**
     * Get a new Builder
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

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
        this(builder());
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
        this(builder().debug(debug));
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * Consider using {@link Builder}
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
        this(builder().debug(debug).jetstream(jetstream));
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
        this(builder().port(port).debug(debug));
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
        this(builder().port(port).debug(debug).jetstream(jetstream));
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * Consider using {@link Builder}
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
        this(builder().debug(debug).configFilePath(configFilePath));
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * Consider using {@link Builder}
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
        this(builder().debug(debug).jetstream(jetstream).configFilePath(configFilePath));
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * Consider using {@link Builder}
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
        this(builder().port(port).debug(debug).configFilePath(configFilePath).configInserts(configInserts));
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * Consider using {@link Builder}
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
        this(builder().port(port).debug(debug).configFilePath(configFilePath));
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * Consider using {@link Builder}
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
        this(builder().customArgs(customArgs));
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * Consider using {@link Builder}
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
        this(builder().debug(debug).customArgs(customArgs));
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * Consider using {@link Builder}
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
        this(builder().debug(debug).jetstream(jetstream).customArgs(customArgs));
    }

    /**
     * Construct and start the Nats Server runner with defaults:
     * Consider using {@link Builder}
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
        this(builder().port(port).debug(debug).customArgs(customArgs));
    }

    /**
     * Construct and start the Nats Server runner with options
     * Consider using {@link Builder}
     * @param port the port to start on or &lt;=0 to use an automatically allocated port
     * @param debug whether to start the server with the -DV flags
     * @param jetstream whether to enable JetStream
     * @param configFilePath path to a custom config file
     * @param configInserts an array of custom lines to add to the config file
     * @param customArgs any custom args to add to the command line
     * @throws IOException thrown when the server cannot start
     */
    public NatsServerRunner(int port, boolean debug, boolean jetstream, String configFilePath, String[] configInserts, String[] customArgs) throws IOException {
        this(builder().port(port).debug(debug).jetstream(jetstream).configFilePath(configFilePath).configInserts(configInserts).customArgs(customArgs));
    }

    public NatsServerRunner(NatsServerRunnerOptions natsServerRunnerOptions) throws Exception {
        this(builder().runnerOptions(natsServerRunnerOptions));
    }

    // ----------------------------------------------------------------------------------------------------
    // ACTUAL CONSTRUCTION
    // ----------------------------------------------------------------------------------------------------

    protected NatsServerRunner(Builder b) throws IOException {
        _executablePath = b.executablePath == null ? getResolvedServerPath() : b.executablePath.toString();
        _ports = b.ports;
        Integer tempPort = _ports.get(CONFIG_PORT_KEY);
        if (tempPort == null) {
            _ports.put(CONFIG_PORT_KEY, -1);
            tempPort = -1;
        }
        if (tempPort == -1) {
            tempPort = nextPort();
        }
        _ports.put(USER_PORT_KEY, tempPort);
        int userPort = tempPort;
        _ports.put(NATS_PORT_KEY, -1);
        _ports.put(NON_NATS_PORT_KEY, -1);

        if (b.output == null) {
            _displayOut = DefaultOutputSupplier.get();
            _displayOut.setLevel(DefaultOutputLevel);
        }
        else {
            _displayOut = b.output;
            if (b.outputLevel != null) {
                _displayOut.setLevel(b.outputLevel);
            }
        }

        long procCheckWait = b.processCheckWait == null ? DEFAULT_PROCESS_CHECK_WAIT : b.processCheckWait;
        int procCheckTries = b.processCheckTries == null ? DEFAULT_PROCESS_CHECK_TRIES : b.processCheckTries;
        long connCheckWait = b.connectCheckWait == null ? DEFAULT_RUN_CHECK_WAIT : b.connectCheckWait;
        int connCheckTries = b.connectCheckTries == null ? DEFAULT_RUN_CHECK_TRIES : b.connectCheckTries;

        List<String> cmd = new ArrayList<>();
        cmd.add(_executablePath);

        try {
            _configFile = File.createTempFile(CONF_FILE_PREFIX, CONF_FILE_EXT);
            BufferedWriter writer = new BufferedWriter(new FileWriter(_configFile));
            boolean portAlreadyDone;
            if (b.configFilePath == null) {
                _ports.put(NATS_PORT_KEY, userPort);
                writePortLine(writer, userPort);
                portAlreadyDone = true;
            }
            else {
                processSuppliedConfigFile(writer, b.configFilePath);
                portAlreadyDone = _ports.get(NATS_PORT_KEY) != -1;
            }

            if (b.configInserts != null) {
                for (String s : b.configInserts) {
                    if (portAlreadyDone && s.startsWith("port:")) {
                        continue;
                    }
                    writeLine(writer, s);
                }
            }

            writer.flush();
            writer.close();

            cmd.add(CONFIG_FILE_OPTION_NAME);
            cmd.add(_configFile.getAbsolutePath());
        }
        catch (IOException ioe) {
            _displayOut.error("%%% Error creating config file: " + ioe);
            throw ioe;
        }

        // Rewrite the port to a new one, so we don't reuse the same one over and over

        if (b.jetstream) {
            cmd.add(JETSTREAM_OPTION);
        }

        if (b.customArgs != null) {
            cmd.addAll(b.customArgs);
        }

        if (b.debugLevel != null) {
            cmd.add(b.debugLevel.getCmdOption());
        }

        _cmdLine = String.join(" ", cmd);

        NatsOutputLogger nol = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            pb.redirectError(ProcessBuilder.Redirect.PIPE);
            pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
            _displayOut.info("%%% Starting [" + _cmdLine + "] with redirected IO");

            process = pb.start();
            nol = NatsOutputLogger.logOutput(_displayOut, process, DEFAULT_NATS_SERVER);

            int tries = procCheckTries;
            do {
                sleep(procCheckWait);
            }
            while (!process.isAlive() && --tries > 0);

            SocketAddress addr = new InetSocketAddress("localhost", _ports.get(NATS_PORT_KEY));
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            if (connCheckTries > 0) {
                boolean checking = true;
                tries = connCheckTries;
                do {
                    try {
                        socketChannel.connect(addr);
                        checking = false;
                    }
                    catch (ConnectException e) {
                        if (--tries == 0) {
                            throw e;
                        }
                        sleep(connCheckWait);
                    } finally {
                        socketChannel.close();
                    }
                } while (checking);
            }

            _displayOut.info("%%% Started [" + _cmdLine + "]");
            nol.endStartupPhase();
        }
        catch (IOException ex) {
            _displayOut.error("%%% Failed to run [" + _cmdLine + "]");
            String exMsg = ex.getMessage();
            if (exMsg != null) {
                _displayOut.error("    " + ex.getMessage());
            }
            _displayOut.error("%%% " + ERROR_NOTE_PART_1);
            _displayOut.error("%%% " + ERROR_NOTE_PART_2);
            StringBuilder exMessage = new StringBuilder("Failed to run [").append(_cmdLine).append("]");
            if (b.fullErrorReportOnStartup) {
                if (nol != null) {
                    for (String line : nol.getStartupLines()) {
                        exMessage.append(System.lineSeparator()).append(line);
                    }
                }
                if (_cmdLine.contains(CONFIG_FILE_OPTION_NAME)) {
                    String configPath = _configFile.getAbsolutePath();
                    String configSep = getConfigSep(configPath);
                    exMessage.append(System.lineSeparator()).append(configSep);
                    exMessage.append(System.lineSeparator()).append(configPath);
                    exMessage.append(System.lineSeparator()).append(configSep);
                    try {
                        List<String> lines = Files.readAllLines(_configFile.toPath());
                        for (String line : lines) {
                            exMessage.append(System.lineSeparator()).append(line);
                        }
                    }
                    catch (Exception ignore) {
                    }
                    exMessage.append(System.lineSeparator()).append(configSep);
                }
            }
            throw new IllegalStateException(exMessage.toString());
        }
    }

    private String getConfigSep(String configPath) {
        StringBuilder sep = new StringBuilder("------------------------------");
        int len = configPath.length();
        while (sep.length() < len) {
            sep.append(sep);
        }
        return sep.substring(0, len);
    }

    // ----------------------------------------------------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------------------------------------------------
    private void processSuppliedConfigFile(BufferedWriter writer, Path configFilePath) throws IOException {
        Matcher constructionPortMatcher = Pattern.compile(PORT_REGEX).matcher("");
        Matcher mappedPortMatcher = Pattern.compile(PORT_MAPPED_REGEX).matcher("");

        BufferedReader reader = new BufferedReader(new FileReader(configFilePath.toFile()));

        boolean userTaken = false;
        int userPort = _ports.get(USER_PORT_KEY); // already ensured so it's not -1
        int natsPort = -1;
        String line = reader.readLine();
        int level = 0;
        while (line != null) {
            String trim = line.trim();
            if (trim.endsWith("{")) {
                level++;
            }
            else if (trim.startsWith("}")) {
                level--;
            }
            // replacing it here allows us to not care if the port is at the top level
            // or for instance inside a websocket block
            constructionPortMatcher.reset(line);
            if (constructionPortMatcher.find()) {
                if (userTaken) {
                    throw new IOException("Improper configuration, cannot assign port multiple times.");
                }
                userTaken = true;
                if (level == 0) {
                    natsPort = userPort;
                }
                else {
                    _ports.put(NON_NATS_PORT_KEY, userPort);
                }
                writeLine(writer, line.replace(constructionPortMatcher.group(1), Integer.toString(userPort)));
            }
            else {
                mappedPortMatcher.reset(line);
                if (mappedPortMatcher.find()) {
                    int start = line.indexOf("<");
                    int end = line.indexOf(">");
                    String key = line.substring(start + 1, end);
                    Integer mapped = _ports.get(key);
                    if (mapped == null) {
                        mapped = nextPort();
                        _ports.put(key, mapped);
                    }
                    writeLine(writer, line.replace("<" + key + ">", mapped.toString()));
                    if (level == 0) {
                        natsPort = mapped;
                    }
                    else {
                        _ports.put(NON_NATS_PORT_KEY, mapped);
                    }
                }
                else {
                    writeLine(writer, line);
                }
            }

            line = reader.readLine();
        }

        reader.close();

        if (natsPort == -1) {
            if (userTaken) {
                _ports.put(NATS_PORT_KEY, 4222);
            }
            else {
                _ports.put(NATS_PORT_KEY, userPort);
                writePortLine(writer, userPort);
            }
        }
        else {
            _ports.put(NATS_PORT_KEY, natsPort);
        }
    }

    private void writePortLine(BufferedWriter writer, int port) throws IOException {
        writeLine(writer, PORT_PROPERTY + port);
    }

    private void writeLine(BufferedWriter writer, String line) throws IOException {
        writer.write(line);
        writer.write("\n");
    }

    private void sleep(long sleep) {
        try {
            Thread.sleep(sleep);
        }
        catch (Exception ignore) {}
    }

    // ====================================================================================================
    // Getters
    // ====================================================================================================
    /**
     * The resolved server executable path being used
     * @return the path
     */
    public String getExecutablePath() {
        return _executablePath;
    }

    /**
     * Get the port number. Useful if it was automatically assigned
     * @return the port number
     */
    public int getPort() {
        return getUserPort();
    }

    public int getUserPort() {
        return _ports.get(USER_PORT_KEY);
    }

    public int getNatsPort() {
        return _ports.get(NATS_PORT_KEY);
    }

    public int getConfigPort() {
        return _ports.get(CONFIG_PORT_KEY);
    }

    public int getNonNatsPort() {
        return _ports.get(NON_NATS_PORT_KEY);
    }

    public Integer getPort(String key) {
        return _ports.get(key);
    }

    /**
     * Get the absolute path of the config file
     * @return the path
     */
    public String getConfigFile() {
        return _configFile.getAbsolutePath();
    }

    /**
     * Get the uri in the form nats://localhost:port
     * @return the uri string
     */
    public String getURI() {
        return getNatsLocalhostUri(getNatsPort());
    }

    /**
     * Get the command line used to start the server
     * @return the command line
     */
    public String getCmdLine() {
        return _cmdLine;
    }

    /**
     * Shut the server down
     * @param wait whether to block while waiting for the process to shut down
     * @throws InterruptedException if the wait was interrupted
     */
    public void shutdown(boolean wait) throws InterruptedException {
        if (process != null) {
            process.destroy();
            _displayOut.info("%%% Shut down [" + _cmdLine + "]");
            if (wait) {
                process.waitFor();
            }
            process = null;
        }
    }

    /**
     * Shut the server down, waiting (blocking)
     * @throws InterruptedException if the wait was interrupted
     */
    public void shutdown() throws InterruptedException {
        shutdown(true);
    }

    /**
     * For AutoCloseable, calls shutdown(true).
     */
    @Override
    public void close() throws Exception {
        shutdown(true);
    }

    // ====================================================================================================
    // Builder
    // ====================================================================================================
    public static class Builder {
        Map<String, Integer> ports = new HashMap<>();
        DebugLevel debugLevel;
        boolean jetstream;
        Path configFilePath;
        List<String> configInserts;
        List<String> customArgs;
        Path executablePath;
        Output output;
        Level outputLevel;
        Long processCheckWait;
        Integer processCheckTries;
        Long connectCheckWait;
        Integer connectCheckTries;
        boolean fullErrorReportOnStartup = true;

        public Builder port(Integer port) {
            return port(CONFIG_PORT_KEY, port);
        }

        public Builder port(String key, Integer port) {
            if (port == null) {
                ports.remove(key);
            }
            else {
                ports.put(key, port);
            }
            return this;
        }

        public Builder ports(Map<String, Integer> ports) {
            this.ports.clear();
            if (ports != null) {
                this.ports.putAll(ports);
            }
            return this;
        }

        public Builder debugLevel(DebugLevel debugLevel) {
            this.debugLevel = debugLevel;
            return this;
        }

        public Builder debug(boolean trueForDebugTraceFalseForNoDebug) {
            this.debugLevel = trueForDebugTraceFalseForNoDebug ? DebugLevel.DEBUG_TRACE : null;
            return this;
        }

        public Builder jetstream() {
            this.jetstream = true;
            return this;
        }

        public Builder jetstream(boolean jetStream) {
            this.jetstream = jetStream;
            return this;
        }

        public Builder configFilePath(String configFilePath) {
            this.configFilePath = configFilePath == null ? null : Paths.get(configFilePath);
            return this;
        }

        public Builder configFilePath(Path configFilePath) {
            this.configFilePath = configFilePath;
            return this;
        }

        public Builder configInserts(List<String> configInserts) {
            this.configInserts = configInserts == null || configInserts.isEmpty() ? null : configInserts;
            return this;
        }

        public Builder configInserts(String[] configInserts) {
            this.configInserts = configInserts == null || configInserts.length == 0 ? null : Arrays.asList(configInserts);
            return this;
        }

        public Builder customArgs(List<String> customArgs) {
            this.customArgs = customArgs == null || customArgs.isEmpty() ? null : customArgs;
            return this;
        }

        public Builder customArgs(String[] customArgs) {
            this.customArgs = customArgs == null || customArgs.length == 0 ? null : Arrays.asList(customArgs);
            return this;
        }

        public Builder executablePath(String executablePath) {
            this.executablePath = executablePath == null ? null : Paths.get(executablePath);
            return this;
        }

        public Builder executablePath(Path executablePath) {
            this.executablePath = executablePath;
            return this;
        }

        public Builder output(Output output) {
            this.output = output;
            return this;
        }

        public Builder outputLogger(Logger logger) {
            this.output = logger == null ? null : new LoggingOutput(logger);
            return this;
        }

        public Builder outputLevel(Level level) {
            this.outputLevel = level;
            return this;
        }

        public Builder processCheckWait(Long processWait) {
            this.processCheckWait = processWait;
            return this;
        }

        public Builder processCheckTries(Integer processCheckTries) {
            this.processCheckTries = processCheckTries;
            return this;
        }

        public Builder connectCheckWait(Long connectCheckWait) {
            this.connectCheckWait = connectCheckWait;
            return this;
        }

        public Builder connectCheckTries(Integer connectCheckTries) {
            this.connectCheckTries = connectCheckTries;
            return this;
        }

        public Builder fullErrorReportOnStartup(boolean fullErrorReportOnStartup) {
            this.fullErrorReportOnStartup = fullErrorReportOnStartup;
            return this;
        }

        public Builder runnerOptions(NatsServerRunnerOptions nsro) {
            port(nsro.port())
                .debugLevel(nsro.debugLevel())
                .jetstream(nsro.jetStream())
                .configFilePath(nsro.configFilePath())
                .configInserts(nsro.configInserts())
                .customArgs(nsro.customArgs())
                .executablePath(nsro.executablePath())
                .outputLogger(nsro.logger())
                .outputLevel(nsro.logLevel());
            return this;
        }

        public NatsServerRunner build() throws IOException {
            return new NatsServerRunner(this);
        }

        public NatsServerRunnerOptions buildOptions() {
            return new NatsServerRunnerOptionsImpl(this);
        }
    }

    // ====================================================================================================
    // Runner Wide Setting
    // ====================================================================================================
    static final Supplier<Output> DefaultLoggingSupplier = () -> new LoggingOutput(Logger.getLogger(NatsServerRunner.class.getName()));

    private static Supplier<Output> DefaultOutputSupplier = DefaultLoggingSupplier;
    private static Level DefaultOutputLevel = Level.INFO;
    private static String PreferredServerPath = null;

    public static Supplier<Output> getDefaultOutputSupplier() {
        return DefaultOutputSupplier;
    }

    public static void setDefaultOutputSupplier(Supplier<Output> outputSupplier) {
        DefaultOutputSupplier = outputSupplier == null ? DefaultLoggingSupplier : outputSupplier;
    }

    public static Level getDefaultOutputLevel() {
        return DefaultOutputLevel;
    }

    public static void setDefaultOutputLevel(Level defaultOutputLevel) {
        DefaultOutputLevel = defaultOutputLevel;
    }

    public static String getPreferredServerPath() {
        return PreferredServerPath;
    }

    public static void setPreferredServerPath(String preferredServerPath) {
        PreferredServerPath = preferredServerPath == null || preferredServerPath.length() == 0 ? null : preferredServerPath;
    }

    public static void clearPreferredServerPath() {
        PreferredServerPath = null;
    }
}