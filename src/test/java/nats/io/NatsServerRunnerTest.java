/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package nats.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static nats.io.NatsRunnerUtils.DEBUG_OPTION;
import static nats.io.NatsRunnerUtils.JETSTREAM_OPTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.commons.util.CollectionUtils.toUnmodifiableList;

class NatsServerRunnerTest {

    public static final String SOURCE_CONFIG_FILE_PATH = "src/test/resources/";

    @Test
    public void testWithoutConfigDefault() throws IOException, InterruptedException {
        try (NatsServerRunner runner = new NatsServerRunner()) {
            validateCommandLine(runner, false, false);
            validateHostAndPort(runner);
            validateConfigLines(runner);
            connect(runner);
        }
    }

    private static Stream<Arguments> withoutDebugAndJetStreamArgs() {
        return Stream.of(
                Arguments.of(false, false),
                Arguments.of(true, false),
                Arguments.of(false, true),
                Arguments.of(true, true)
        );
    }

    @ParameterizedTest
    @MethodSource("withoutDebugAndJetStreamArgs")
    public void testWithoutDebugAndJetStream(boolean debug, boolean jetStream) throws IOException, InterruptedException {
        try (NatsServerRunner runner = new NatsServerRunner(debug, jetStream)) {
            validateCommandLine(runner, debug, jetStream);
            validateHostAndPort(runner);
            validateConfigLines(runner);
            connect(runner);
        }
    }

    @Test
    public void testCustoms() throws IOException, InterruptedException {
        String[] configInserts = { "# custom insert this comment" };
        String[] customArgs = { "--user", "uuu", "--pass", "ppp" };
        try (NatsServerRunner runner = new NatsServerRunner(-1, false, false, null, configInserts, customArgs)) {
            validateCommandLine(runner, false, false, "--user uuu", "--pass ppp");
            validateHostAndPort(runner);
            validateConfigLines(runner, Arrays.asList(configInserts));
            connect(runner);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "port_not_specified.conf",
            "port_specified.conf",
            "websocket.conf",
            "ws.conf"
    })
    public void testWithConfig(String configFile) throws IOException, InterruptedException {
        String[] configInserts = { "# custom insert this comment " + configFile };
        try (NatsServerRunner runner = new NatsServerRunner(SOURCE_CONFIG_FILE_PATH + configFile, configInserts, -1, false)) {
            validateCommandLine(runner, false, false);
            validateHostAndPort(runner);
            validateConfigLines(runner, configFile, configInserts);
            connect(runner);
        }
    }

    private void validateCommandLine(NatsServerRunner runner, boolean debug, boolean jetStream, String... customArgs) {
        assertEquals(debug, runner.getCmdLine().contains(" " + DEBUG_OPTION));
        assertEquals(jetStream, runner.getCmdLine().contains(" " + JETSTREAM_OPTION));
        for (String ca : customArgs) {
            assertTrue(runner.getCmdLine().contains(" " + ca));
        }
    }

    private void validateHostAndPort(NatsServerRunner server) {
        assertTrue(server.getPort() > 0);
        assertTrue(server.getPort() != 1234);
        assertTrue(server.getURI().startsWith("nats://localhost"));
    }

    private void validateConfigLines(NatsServerRunner runner) throws IOException {
        validateConfigLines(runner, null);
    }

    private void validateConfigLines(NatsServerRunner runner, String configFile, String[] configInserts) throws IOException {
        List<String> expected = Files.lines(new File(SOURCE_CONFIG_FILE_PATH + configFile).toPath())
                .map(String::trim)
                .filter(s -> s.length() > 0)
                .filter(s -> !s.contains("port"))
                .collect(toList());
        Collections.addAll(expected, configInserts);
        validateConfigLines(runner, expected);
    }

    private void validateConfigLines(NatsServerRunner runner, List<String> expected) throws IOException {
        List<String> lines = Files.lines(new File(runner.getConfigFile()).toPath())
                .map(String::trim)
                .filter(s -> s.length() > 0)
                .collect(toUnmodifiableList());

        assertTrue(lines.contains("port: " + runner.getPort()));
        if (expected != null) {
            for (String ex : expected) {
                System.out.println("EX " + ex);
                assertTrue(lines.contains(ex));
            }
        }
    }

    private static final byte[] CONNECT_BYTES = "CONNECT {\"lang\":\"java\",\"version\":\"2.11.5\",\"protocol\":1,\"verbose\":false,\"pedantic\":false,\"tls_required\":false,\"echo\":true,\"headers\":true,\"no_responders\":true}\r\n".getBytes();
    private void connect(NatsServerRunner runner) throws IOException {
        Socket socket = new Socket();
        SocketAddress socketAddress=new InetSocketAddress(InetAddress.getByName("localhost"), runner.getPort());
        socket.bind(socketAddress);
        socket.connect(socketAddress);
        assertEquals(runner.getPort(), socket.getLocalPort());

        socket.getOutputStream().write(CONNECT_BYTES);
        socket.getOutputStream().flush();

        InputStream in = socket.getInputStream();
        // give the server time to respond or this flaps
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }
        assertTrue(in.available() > 8); // want to make sure CONNECT is there
        int x = -1;
        int i = in.read();
        while (i != -1 && x++ < 8) {
            assertEquals(CONNECT_BYTES[x], i);
            i = in.read();
        }
        in.close();
    }
}
