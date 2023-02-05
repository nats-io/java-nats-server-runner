// Copyright 2022 The NATS Authors
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static nats.io.NatsServerRunner.defaultOutputSupplier;
import static org.junit.jupiter.api.Assertions.*;

public class NatsServerRunnerTest extends TestBase {

    private static Stream<Arguments> debugAndJetStreamArgs() {
        return Stream.of(
                Arguments.of(false, false),
                Arguments.of(true, false),
                Arguments.of(false, true),
                Arguments.of(true, true)
        );
    }

    @SuppressWarnings("resource")
    @ParameterizedTest
    @MethodSource("debugAndJetStreamArgs")
    public void testVariousConstructors(boolean debug, boolean jetStream) throws Exception {
        valdiateVariousConstructors(false, false, NatsServerRunner::new);
        valdiateVariousConstructors(debug, false, () -> new NatsServerRunner(debug));
        valdiateVariousConstructors(debug, jetStream, () -> new NatsServerRunner(debug, jetStream));
        valdiateVariousConstructors(false, false, () -> NatsServerRunner.builder().build());
        valdiateVariousConstructors(debug, jetStream, () -> NatsServerRunner.builder().debug(debug).jetstream(jetStream).build());

        int port1 = NatsRunnerUtils.nextPort();
        NatsServerRunner runner = valdiateVariousConstructors(debug, false, () -> new NatsServerRunner(port1, debug));
        assertEquals(port1, runner.getPort());

        int port2 = NatsRunnerUtils.nextPort();
        runner = valdiateVariousConstructors(debug, jetStream, () -> new NatsServerRunner(port2, debug, jetStream));
        assertEquals(port2, runner.getPort());
    }

    interface RunnerSupplier {
        NatsServerRunner get() throws IOException;
    }

    private NatsServerRunner valdiateVariousConstructors(boolean debug, boolean jetStream, RunnerSupplier supplier) throws IOException {
        NatsServerRunner runner = supplier.get();
        validateBasics(runner, debug, jetStream);
        String cmd = runner.getCmdLine();
        assertEquals(debug, cmd.contains(" -DV"));
        assertEquals(jetStream, cmd.contains(" -js"));
        return runner;
    }

    private static final String[] CUSTOMS_CONFIG_INSERTS = { "# custom insert this comment" };
    private static final String[] CUSTOMS_ARGS = { "--user", "uuu", "--pass", "ppp" };

    private void _testCustoms(NatsServerRunner runner) throws IOException {
        validateCommandLine(runner, false, false, "--user uuu", "--pass ppp");
        validateHostAndPort(runner);
        validateConfigLines(runner, Arrays.asList(CUSTOMS_CONFIG_INSERTS));
        connect(runner);
    }

    @Test
    public void testCustoms() throws Exception {
        try (NatsServerRunner runner = new NatsServerRunner(-1, false, false, null, CUSTOMS_CONFIG_INSERTS, CUSTOMS_ARGS)) {
            _testCustoms(runner);
        }
    }

    @Test
    public void testCustomsBuilder() throws Exception {
        try (NatsServerRunner runner = NatsServerRunner.builder()
            .configInserts(CUSTOMS_CONFIG_INSERTS)
            .customArgs(CUSTOMS_ARGS)
            .build())
        {
            _testCustoms(runner);
        }
    }

    private static Stream<Arguments> withConfigArgs() {
        return Stream.of(
                Arguments.of("port_not_specified.conf", true),
                Arguments.of("port_specified.conf", true),
                Arguments.of("websocket.conf", false),
                Arguments.of("ws.conf", false)
        );
    }

    private void _testWithConfig(String configFile, boolean checkConnect, String[] configInserts, NatsServerRunner runner) throws IOException {
        validateCommandLine(runner, false, false);
        validateHostAndPort(runner);
        validateConfigLines(runner, configFile, configInserts);
        if (checkConnect) {
            connect(runner);
        }
    }

    @ParameterizedTest
    @MethodSource("withConfigArgs")
    public void testWithConfig(String configFile, boolean checkConnect) throws Exception {
        String[] configInserts = { "# custom insert this comment " + configFile };
        try (NatsServerRunner runner = new NatsServerRunner(SOURCE_CONFIG_FILE_PATH + configFile, configInserts, -1, false)) {
            _testWithConfig(configFile, checkConnect, configInserts, runner);
        }
    }

    @ParameterizedTest
    @MethodSource("withConfigArgs")
    public void testWithConfigBuilder(String configFile, boolean checkConnect) throws Exception {
        String[] configInserts = { "# custom insert this comment " + configFile };
        try (NatsServerRunner runner = NatsServerRunner.builder()
            .configFilePath(SOURCE_CONFIG_FILE_PATH + configFile)
            .configInserts(configInserts)
            .build())
        {
            _testWithConfig(configFile, checkConnect, configInserts, runner);
        }
    }

    @Test
    public void testNatsServerRunnerOptionsImpl() {
        Path p = Paths.get(".");
        NatsServerRunner.Builder builder = NatsServerRunner.builder()
            .port(1)
            .debugLevel(DebugLevel.DEBUG_VERBOSE_TRACE)
            .jetstream()
            .configFilePath(p)
            .configInserts(new String[]{"inserts"})
            .customArgs(new String[]{"custom"})
            .executablePath(p)
            .outputLevel(Level.OFF);

        validateOptions(p, false, new NatsServerRunnerOptionsImpl(builder));
        validateOptions(p, false, builder.buildOptions());
        validateOptions(p, true, new NatsServerRunnerOptionsImpl(builder.outputLogger(Logger.getLogger("testNatsServerRunnerOptionsImpl"))));
    }

    private static void validateOptions(Path p, boolean logger, NatsServerRunnerOptions impl) {
        assertEquals(1, impl.port());
        assertEquals(DebugLevel.DEBUG_VERBOSE_TRACE, impl.debugLevel());
        assertTrue(impl.jetStream());
        assertEquals(p, impl.configFilePath());
        assertEquals(1, impl.configInserts().size());
        assertEquals("inserts", impl.configInserts().get(0));
        assertEquals(1, impl.customArgs().size());
        assertEquals("custom", impl.customArgs().get(0));
        assertEquals(p, impl.executablePath());
        assertEquals(Level.OFF, impl.logLevel());
        if (logger) {
            assertNotNull(impl.logger());
            assertNotSame(Level.OFF, impl.logger().getLevel());
        }
        else {
            assertNull(impl.logger());
        }
    }

    @Test
    public void testStaticStuff() {
        Level initial = NatsServerRunner.defaultOutputLevel();
        NatsServerRunner.setDefaultOutputLevel(Level.ALL);
        assertEquals(Level.ALL, NatsServerRunner.defaultOutputLevel());

        NatsServerRunner.setDefaultOutputLevel(initial);
        assertEquals(initial, NatsServerRunner.defaultOutputLevel());

        Supplier<Output> dflt = defaultOutputSupplier();
        Supplier<Output> supplier = ConsoleOutput::new;
        assertEquals(dflt, defaultOutputSupplier());
        assertNotEquals(supplier, defaultOutputSupplier());

        NatsServerRunner.setDefaultOutputSupplier(supplier);
        assertNotEquals(dflt, defaultOutputSupplier());
        assertEquals(supplier, defaultOutputSupplier());

        NatsServerRunner.setDefaultOutputSupplier(null);
        assertEquals(dflt, defaultOutputSupplier());
        assertNotEquals(supplier, defaultOutputSupplier());
    }
}
