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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class NatsRunnerUtils {
    public static final String NATS_SERVER_PATH_ENV = "nats_server_path";
    public static final String DEFAULT_NATS_SERVER = "nats-server";
    public static final String NATS = "nats";

    public static final String CONFIG_FILE_OPTION_NAME = "--config";
    public static final String JETSTREAM_OPTION = "-js";

    public static final String CONF_FILE_PREFIX = "nats_java_test";
    public static final String CONF_FILE_EXT = ".conf";
    public static final String JS_STORE_DIR_REGEX = "\\s*store_dir\\s*=";
    public static final String PORT_REGEX = "port: (\\d+)";
    public static final String PORT_MAPPED_REGEX = "port: <(\\w+)>";
    public static final String PORT_PROPERTY = "port: ";
    public static final String CONFIG_PORT_KEY = "config_port";
    public static final String USER_PORT_KEY = "user_port";
    public static final String NATS_PORT_KEY = "nats_port";
    public static final String NON_NATS_PORT_KEY = "non_nats_port";

    public enum LocalHost {
        name("localhost"), ip("127.0.0.1"), unspecified("0.0.0.0");
        public final String host;
        LocalHost(String host) { this.host = host; }
    }

    protected static final Supplier<Output> DefaultLoggingSupplier = () -> new LoggingOutput(Logger.getLogger("io.nats.NatsServerRunner"));
    protected static Supplier<Output> DefaultOutputSupplier = DefaultLoggingSupplier;
    protected static Level DefaultOutputLevel = Level.INFO;
    protected static String PreferredServerPath = null;
    protected static long DefaultProcessAliveCheckWait = 100;
    protected static int DefaultProcessAliveCheckTries = 10;
    protected static int DefaultConnectValidateTries = 3;
    protected static long DefaultConnectValidateTimeout = 100;
    protected static OutputThreadProvider DefaultOutputThreadProvider = new OutputThreadProvider() {};
    protected static Integer ManualStartPort = null;
    protected static LocalHost DefaultLocalhostHost;

    static {
        if (System.getProperty("java.version").contains("1.8")) {
            setDefaultLocalhostHost(NatsRunnerUtils.LocalHost.name);
        }
        else {
            setDefaultLocalhostHost(NatsRunnerUtils.LocalHost.ip);
        }
    }

    /**
     * Build a nats://localhost:port uri
     * @param port the port
     * @return the uri
     */
    public static String getNatsLocalhostUri(int port) {
        return getUri(NATS, DefaultLocalhostHost.host, port);
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
        return getUri(schema, DefaultLocalhostHost.host, port);
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

    static AtomicInteger NEXT_PORT;

    /**
     * Get a port number automatically allocated by the system, typically from an ephemeral port range.
     * @return the port number
     * @throws IOException if there is a problem getting a port
     */
    public static int nextPort() throws IOException {
        if (ManualStartPort == null || ManualStartPort < 1) {
            try (ServerSocket socket = new ServerSocket(0)) {
                while (!socket.isBound()) {
                    //noinspection BusyWait
                    Thread.sleep(50);
                }
                return socket.getLocalPort();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Thread interrupted", e);
            }
        }
        else if (NEXT_PORT == null) {
            NEXT_PORT = new AtomicInteger(ManualStartPort);
            return ManualStartPort;
        }
        else {
            return NEXT_PORT.incrementAndGet();
        }
    }

    /**
     * Resolves the server executable path in this order:
     * <ol>
     * <li>Checking the {@value #NATS_SERVER_PATH_ENV} environment variable</li>
     * <li>Checking the value set via {@link #setPreferredServerPath} method</li>
     * <li>{@value #DEFAULT_NATS_SERVER}</li>
     * </ol>
     * @return the resolved path
     */
    public static String getResolvedServerPath() {
        String serverPath = getPreferredServerPath();
        if (serverPath == null) {
            serverPath = System.getenv(NATS_SERVER_PATH_ENV);
            if (serverPath == null) {
                serverPath = DEFAULT_NATS_SERVER;
            }
        }
        return serverPath;
    }

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

    public static int getDefaultProcessAliveCheckTries() {
        return DefaultProcessAliveCheckTries;
    }

    public static void setDefaultProcessAliveCheckTries(int tries) {
        DefaultProcessAliveCheckTries = tries;
    }

    public static long getDefaultProcessAliveCheckWait() {
        return DefaultProcessAliveCheckWait;
    }

    public static void setDefaultProcessAliveCheckWait(long wait) {
        DefaultProcessAliveCheckWait = wait;
    }

    public static int getDefaultConnectValidateTries() {
        return DefaultConnectValidateTries;
    }

    public static void setDefaultConnectValidateTries(int tries) {
        DefaultConnectValidateTries = tries;
    }

    public static long getDefaultConnectValidateTimeout() {
        return DefaultConnectValidateTimeout;
    }

    public static void setDefaultConnectValidateTimeout(long delay) {
        DefaultConnectValidateTimeout = delay;
    }

    public static void setDefaultOutputThreadProvider(OutputThreadProvider defaultOutputThreadProvider) {
        DefaultOutputThreadProvider = defaultOutputThreadProvider == null ? new OutputThreadProvider() {} : defaultOutputThreadProvider;
    }

    public static int getManualStartPort(Integer manualStartPort) {
        return ManualStartPort == null || ManualStartPort < 1 ? -1 : ManualStartPort;
    }

    public static void setManualStartPort(Integer manualStartPort) {
        ManualStartPort = manualStartPort;
    }

    public static LocalHost getDefaultLocalhostHost() {
        return DefaultLocalhostHost;
    }

    public static void setDefaultLocalhostHost(LocalHost defaultLocalhostHost) {
        DefaultLocalhostHost = defaultLocalhostHost;
    }
}
