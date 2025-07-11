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
package io.nats;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static io.nats.NatsRunnerUtils.*;
import static io.nats.NatsServerRunner.DefaultLoggingSupplier;
import static io.nats.NatsServerRunner.getDefaultOutputSupplier;
import static org.junit.jupiter.api.Assertions.*;

@Isolated
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
    @Test
    public void testFixedConstructors() throws Exception {
        validateVariousConstructors(false, false, NatsServerRunner::new);
        validateVariousConstructors(false, false, () -> NatsServerRunner.builder().build());
    }

    @SuppressWarnings("resource")
    @ParameterizedTest
    @MethodSource("debugAndJetStreamArgs")
    public void testParameterizedConstructors(boolean debug, boolean jetStream) throws Exception {
        validateVariousConstructors(debug, false, () -> new NatsServerRunner(debug));
        validateVariousConstructors(debug, jetStream, () -> new NatsServerRunner(debug, jetStream));
        validateVariousConstructors(debug, jetStream, () -> NatsServerRunner.builder().debug(debug).jetstream(jetStream).build());
        validateVariousConstructors(debug, jetStream, () -> {
            try {
                return new NatsServerRunner(NatsServerRunner.builder().debug(debug).jetstream(jetStream).buildOptions());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        validateVariousConstructors(debug, false, () -> new NatsServerRunner((String)null, debug));
        validateVariousConstructors(debug, jetStream, () -> new NatsServerRunner((String)null, debug, jetStream));

        validateVariousConstructors(false, false, () -> new NatsServerRunner((String[])null));
        validateVariousConstructors(debug, false, () -> new NatsServerRunner((String[])null, debug));
        validateVariousConstructors(debug, jetStream, () -> new NatsServerRunner((String[])null, debug, jetStream));

        int port1 = NatsRunnerUtils.nextPort();
        NatsServerRunner runner = validateVariousConstructors(debug, false, () -> new NatsServerRunner(port1, debug));
        assertEquals(port1, runner.getPort());

        int port2 = NatsRunnerUtils.nextPort();
        runner = validateVariousConstructors(debug, jetStream, () -> new NatsServerRunner(port2, debug, jetStream));
        assertEquals(port2, runner.getPort());

        int port3 = NatsRunnerUtils.nextPort();
        runner = validateVariousConstructors(debug, false, () -> new NatsServerRunner((String)null, port3, debug));
        assertEquals(port3, runner.getPort());
    }

    interface RunnerSupplier {
        NatsServerRunner get() throws IOException;
    }

    private NatsServerRunner validateVariousConstructors(boolean debug, boolean jetStream, RunnerSupplier supplier) throws Exception {
        NatsServerRunner runner = supplier.get();
        validateBasics(runner, debug, jetStream);
        assertTrue(runner.getExecutablePath().contains("nats-server"));
        String cmd = runner.getCmdLine();
        assertEquals(debug, cmd.contains(" -DV"));
        assertEquals(jetStream, cmd.contains(" -js"));
        runner.shutdown(true);
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
            Arguments.of("config_port_missing_ws_no.conf", true),
            Arguments.of("config_port_user_ws_no.conf", true),
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
    public void testWithConfigParams(String configFile, boolean checkConnect) throws Exception {
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

    private static final int MATCH_NOTHING = 0;
    private static final int MATCH_USER = -1;
    private static final int MATCH_MAP = 999;
    private static final int MATCH_4222 = 4222;

    private static Stream<Arguments> mappedPortsArgs() {
        return Stream.of(
            Arguments.of("config_port_mapped_ws_mapped.conf",  true,  true,  MATCH_MAP,  MATCH_MAP),
            Arguments.of("config_port_mapped_ws_user.conf",    true,  false, MATCH_MAP,  MATCH_USER),
            Arguments.of("config_port_mapped_ws_no.conf",      true,  false, MATCH_MAP,  MATCH_NOTHING),
            Arguments.of("config_port_missing_ws_mapped.conf", false, true,  MATCH_USER, MATCH_MAP),
            Arguments.of("config_port_missing_ws_user.conf",   false, false, MATCH_4222, MATCH_USER),
            Arguments.of("config_port_missing_ws_no.conf",     false, false, MATCH_USER, MATCH_NOTHING),
            Arguments.of("config_port_user_ws_mapped.conf",    false, true,  MATCH_USER, MATCH_MAP),
            Arguments.of("config_port_user_ws_no.conf",        false, false, MATCH_USER, MATCH_MAP)
        );
    }

    @ParameterizedTest
    @MethodSource("mappedPortsArgs")
    public void testMappedPorts(String configFile, boolean pMapped, boolean wsMapped, int natsMatch, int wsMatch) {
        try {
            NatsServerRunner.Builder builder = NatsServerRunner.builder()
                .configFilePath(SOURCE_CONFIG_FILE_PATH + configFile);

            int pPortIn = -1;
            if (pMapped) {
                pPortIn = nextPort();
                builder.port("p", pPortIn);
            }

            int wsPortIn = -1;
            if (wsMapped) {
                wsPortIn = nextPort();
                builder.port("ws", wsPortIn);
            }

            try (NatsServerRunner runner = builder.build()) {
                assertEquals(-1, runner.getConfigPort());

                Integer userPort = runner.getPort(USER_PORT_KEY);
                assertEquals(userPort, runner.getUserPort());

                Integer natsPort = runner.getPort(NATS_PORT_KEY);
                assertEquals(natsPort, runner.getNatsPort());

                Integer nonNatsPort = runner.getPort(NON_NATS_PORT_KEY);
                assertEquals(nonNatsPort, runner.getNonNatsPort());

                if (pMapped) {
                    assertEquals(pPortIn, runner.getPort("p"));
                }

                if (wsMapped) {
                    assertEquals(wsPortIn, runner.getPort("ws"));
                }

                switch (natsMatch) {
                    case MATCH_USER:
                        assertEquals(userPort, natsPort);
                        break;
                    case MATCH_MAP:
                        assertEquals(pPortIn, natsPort);
                        break;
                    case MATCH_4222:
                        assertEquals(4222, natsPort);
                        break;
                }
                connect(runner);

                switch (wsMatch) {
                    case MATCH_USER:
                        assertEquals(nonNatsPort, userPort);
                        break;
                    case MATCH_MAP:
                        assertEquals(nonNatsPort, wsPortIn);
                        break;
                }
            }
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testTooManyUserPorts() {
        assertThrows(IOException.class, () ->
            NatsServerRunner.builder()
                .configFilePath(SOURCE_CONFIG_FILE_PATH + "config_port_user_ws_user.conf")
                .build());
    }

    @Test
    public void testBuilder() {
        Path p = Paths.get(".");
        NatsServerRunner.Builder builder = NatsServerRunner.builder()
            .port(1)
            .debugLevel(DebugLevel.DEBUG_VERBOSE_TRACE)
            .jetstream()
            .configFilePath(p)
            .configFilePath(p.toString())
            .configInserts(new String[0])
            .configInserts((String[])null)
            .configInserts(new ArrayList<>())
            .configInserts((List<String>)null)
            .configInserts(new String[]{"discarded"})
            .configInserts(Collections.singletonList("inserts"))
            .customArgs(new String[0])
            .customArgs((String[])null)
            .customArgs(new ArrayList<>())
            .customArgs((List<String>)null)
            .customArgs(new String[]{"discarded"})
            .customArgs(Collections.singletonList("custom"))
            .executablePath((String)null)
            .executablePath((Path)null)
            .executablePath(p)
            .executablePath(p.toString())
            .outputLevel(Level.OFF)
            .output(null)
            .processCheckWait(11L)
            .processCheckTries(12)
            .connectCheckWait(13L)
            .connectCheckTries(14)
            ;

        assertNull(builder.output);
        assertEquals(11L, builder.processCheckWait);
        assertEquals(12, builder.processCheckTries);
        assertEquals(13L, builder.connectCheckWait);
        assertEquals(14, builder.connectCheckTries);

        validateOptions(p, false, new NatsServerRunnerOptionsImpl(builder));
        validateOptions(p, false, NatsServerRunner.builder().runnerOptions(new NatsServerRunnerOptionsImpl(builder)).buildOptions());
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
    public void testBuilderMoreCoverage() throws Exception {
        Map<String, Integer> map = new HashMap<>();
        map.put("foo", 1);
        NatsServerRunner.Builder builder = NatsServerRunner.builder()
            .ports(map)
            .output(new ConsoleOutput())
            .fullErrorReportOnStartup(false);

        try (NatsServerRunner sr = builder.build()) {
            assertNotEquals(4222, sr.getNatsPort());
            assertEquals(1, sr.getPort("foo"));
        }

        try(NatsServerRunner sr = new NatsServerRunner((String[])null, -1, true)) {
            assertNotEquals(4222, sr.getNatsPort());
        }
    }

    @Test
    public void testStaticStuff() {
        Level initial = NatsServerRunner.getDefaultOutputLevel();
        NatsServerRunner.setDefaultOutputLevel(Level.ALL);
        assertEquals(Level.ALL, NatsServerRunner.getDefaultOutputLevel());

        NatsServerRunner.setDefaultOutputLevel(initial);
        assertEquals(initial, NatsServerRunner.getDefaultOutputLevel());

        Supplier<Output> dflt = getDefaultOutputSupplier();
        Supplier<Output> supplier = ConsoleOutput::new;
        assertEquals(dflt, getDefaultOutputSupplier());
        assertNotEquals(supplier, getDefaultOutputSupplier());

        NatsServerRunner.setDefaultOutputSupplier(supplier);
        assertNotEquals(dflt, getDefaultOutputSupplier());
        assertEquals(supplier, getDefaultOutputSupplier());

        NatsServerRunner.setDefaultOutputSupplier(null);
        assertEquals(DefaultLoggingSupplier, getDefaultOutputSupplier());
    }

    @Test
    public void testShutdownCoverage() throws Exception {
        NatsServerRunner runner = NatsServerRunner.builder().build();
        runner.shutdown(false);
        Thread.sleep(1000);
        runner.shutdown();
    }

    @Test
    public void testBadConfig() throws Exception {
        try (NatsServerRunner runner = NatsServerRunner.builder()
            .configFilePath(SOURCE_CONFIG_FILE_PATH + "bad.conf")
            .output(new ConsoleOutput())
            .build())
        {
            fail("Config was bad, should have exceptioned.");
        }
        catch (Exception e) {
            assertTrue(e.getMessage().contains("nats-server: Parse error on line 2"));
        }
    }

    @Test
    public void testBuilderPortTakesPrecedence() throws Exception {
        String[] configInserts = new String[] {"port:4777"};

        NatsServerRunner.Builder b = NatsServerRunner.builder()
            .debug(false)
            .jetstream(true)
            .configInserts(configInserts)
            .port(4242)
            .connectCheckTries(0)
            ;

        try (NatsServerRunner runner = b.build()) {
            List<String> lines = Files.readAllLines(Paths.get(runner.getConfigFile()));
            int portCount = 0;
            int portFound = -1;
            for (String line : lines) {
                if (line.startsWith("port:")) {
                    portCount++;
                    portFound = Integer.parseInt(line.substring(5).trim());
                }
            }
            assertEquals(4242, runner.getPort());
            assertEquals(1, portCount);
            assertEquals(4242, portFound);
        }
    }
}
