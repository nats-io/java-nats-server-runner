// Copyright 2022-2025 The NATS Authors
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.nats.NatsRunnerUtils.*;
import static io.nats.NatsServerRunner.builder;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("resource")
@Isolated
public class NatsServerRunnerTest extends TestBase {
    @Test
    public void testDefaultConstructor() throws Exception {
        validateVariousConstructors(false, false, NatsServerRunner::new);
    }

    @Test
    public void testDefaultBuilder() throws Exception {
        validateVariousConstructors(false, false, () -> builder().build());
    }

    @Test
    public void testDebugConstructorFalse() throws Exception {
        validateVariousConstructors(false, false, () -> new NatsServerRunner(false));
    }

    @Test
    public void testDebugConstructorTrue() throws Exception {
        validateVariousConstructors(true, false, () -> new NatsServerRunner(true));
    }

    @Test
    public void testDebugJsConstructorFalseFalse() throws Exception {
        validateVariousConstructors(false, false, () -> new NatsServerRunner(false, false));
    }

    @Test
    public void testDebugJsConstructorTrueFalse() throws Exception {
        validateVariousConstructors(true, false, () -> new NatsServerRunner(true, false));
    }

    @Test
    public void testDebugJsConstructorFalseTrue() throws Exception {
        validateVariousConstructors(false, true, () -> new NatsServerRunner(false, true));
    }

    @Test
    public void testDebugJsConstructorTrueTrue() throws Exception {
        validateVariousConstructors(true, true, () -> new NatsServerRunner(true, true));
    }

    @Test
    public void testDebugJsBuilderFalseFalse() throws Exception {
        validateVariousConstructors(false, false, () -> builder().debug(false).jetstream(false).build());
    }

    @Test
    public void testDebugJsBuilderTrueFalse() throws Exception {
        validateVariousConstructors(true, false, () -> builder().debug(true).jetstream(false).build());
    }

    @Test
    public void testDebugJsBuilderFalseTrue() throws Exception {
        validateVariousConstructors(false, true, () -> builder().debug(false).jetstream(true).build());
    }

    @Test
    public void testDebugJsBuilderTrueTrue() throws Exception {
        validateVariousConstructors(true, true, () -> builder().debug(true).jetstream(true).build());
    }

    @Test
    public void testDebugJsNewAndBuilderAndBuildOptionsFalseFalse() throws Exception {
        validateVariousConstructors(false, false, () -> {
            try {
                return new NatsServerRunner(builder().debug(false).jetstream(false).buildOptions());
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testDebugJsNewAndBuilderAndBuildOptionsTrueFalse() throws Exception {
        validateVariousConstructors(true, false, () -> {
            try {
                return new NatsServerRunner(builder().debug(true).jetstream(false).buildOptions());
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testDebugJsNewAndBuilderAndBuildOptionsFalseTrue() throws Exception {
        validateVariousConstructors(false, true, () -> {
            try {
                return new NatsServerRunner(builder().debug(false).jetstream(true).buildOptions());
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testDebugJsNewAndBuilderAndBuildOptionsTrueTrue() throws Exception {
        validateVariousConstructors(true, true, () -> {
            try {
                return new NatsServerRunner(builder().debug(true).jetstream(true).buildOptions());
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testDebugConstructorNullConfigFileFalse() throws Exception {
        validateVariousConstructors(false, false, () -> new NatsServerRunner((String) null, false));
    }

    @Test
    public void testDebugConstructorNullConfigFileTrue() throws Exception {
        validateVariousConstructors(true, false, () -> new NatsServerRunner((String) null, true));
    }

    @Test
    public void testDebugJsConstructorNullConfigFalseFalse() throws Exception {
        validateVariousConstructors(false, false, () -> new NatsServerRunner((String)null, false, false));
    }

    @Test
    public void testDebugJsConstructorNullConfigTrueFalse() throws Exception {
        validateVariousConstructors(true, false, () -> new NatsServerRunner((String)null, true, false));
    }

    @Test
    public void testDebugJsConstructorNullConfigFalseTrue() throws Exception {
        validateVariousConstructors(false, true, () -> new NatsServerRunner((String)null, false, true));
    }

    @Test
    public void testDebugJsConstructorNullConfigTrueTrue() throws Exception {
        validateVariousConstructors(true, true, () -> new NatsServerRunner((String)null, true, true));
    }

    @Test
    public void testConstructorNullCustom() throws Exception {
        validateVariousConstructors(false, false, () -> new NatsServerRunner((String[])null));
    }

    @Test
    public void testDebugConstructorNullCustomFalse() throws Exception {
        validateVariousConstructors(false, false, () -> new NatsServerRunner((String[])null, false));
    }

    @Test
    public void testDebugConstructorNullCustomTrue() throws Exception {
        validateVariousConstructors(true, false, () -> new NatsServerRunner((String[])null, true));
    }

    @Test
    public void testDebugJsConstructorNullCustomFalseFalse() throws Exception {
        validateVariousConstructors(false, false, () -> new NatsServerRunner((String[])null, false, false));
    }

    @Test
    public void testDebugJsConstructorNullCustomTrueFalse() throws Exception {
        validateVariousConstructors(true, false, () -> new NatsServerRunner((String[])null, true, false));
    }

    @Test
    public void testDebugJsConstructorNullCustomFalseTrue() throws Exception {
        validateVariousConstructors(false, true, () -> new NatsServerRunner((String[])null, false, true));
    }

    @Test
    public void testDebugJsConstructorNullCustomTrueTrue() throws Exception {
        validateVariousConstructors(true, true, () -> new NatsServerRunner((String[])null, true, true));
    }

    @Test
    public void testDebugConstructorNextPortFalse() throws Exception {
        int port = nextPort();
        NatsServerRunner runner = validateVariousConstructors(false, false, () -> new NatsServerRunner(port, false));
        assertEquals(port, runner.getPort());
    }

    @Test
    public void testDebugConstructorNextPortTrue() throws Exception {
        int port = nextPort();
        NatsServerRunner runner = validateVariousConstructors(true, false, () -> new NatsServerRunner(port, true));
        assertEquals(port, runner.getPort());
    }

    @Test
    public void testDebugJsConstructorNextPortFalseFalse() throws Exception {
        int port = nextPort();
        NatsServerRunner runner = validateVariousConstructors(false, false, () -> new NatsServerRunner(port, false, false));
        assertEquals(port, runner.getPort());
    }

    @Test
    public void testDebugJsConstructorNextPortTrueFalse() throws Exception {
        int port = nextPort();
        NatsServerRunner runner = validateVariousConstructors(true, false, () -> new NatsServerRunner(port, true, false));
        assertEquals(port, runner.getPort());
    }

    @Test
    public void testDebugJsConstructorNextPortFalseTrue() throws Exception {
        int port = nextPort();
        NatsServerRunner runner = validateVariousConstructors(false, true, () -> new NatsServerRunner(port, false, true));
        assertEquals(port, runner.getPort());
    }

    @Test
    public void testDebugJsConstructorNextPortTrueTrue() throws Exception {
        int port = nextPort();
        NatsServerRunner runner = validateVariousConstructors(true, true, () -> new NatsServerRunner(port, true, true));
        assertEquals(port, runner.getPort());
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
        validateConnection(runner);
    }

    @Test
    public void testCustoms() throws Exception {
        try (NatsServerRunner runner = new NatsServerRunner(-1, false, false, null, CUSTOMS_CONFIG_INSERTS, CUSTOMS_ARGS)) {
            _testCustoms(runner);
        }
    }

    @Test
    public void testCustomsBuilder() throws Exception {
        try (NatsServerRunner runner = builder()
            .configInserts(CUSTOMS_CONFIG_INSERTS)
            .customArgs(CUSTOMS_ARGS)
            .build())
        {
            _testCustoms(runner);
        }
    }

    @Test
    public void testWithConfigParams_config_port_missing_ws_no() throws Exception {
        _testWithConfigParams("config_port_missing_ws_no.conf", true);
    }

    @Test
    public void testWithConfigParams_config_port_user_ws_no() throws Exception {
        _testWithConfigParams("config_port_user_ws_no.conf", true);
    }

    @Test
    public void testWithConfigParams_websocket() throws Exception {
        _testWithConfigParams("websocket.conf", false);
    }

    @Test
    public void testWithConfigParams_ws() throws Exception {
        _testWithConfigParams("ws.conf", false);
    }

    @Test
    public void testWithConfigBuilder_config_port_missing_ws_no() throws Exception {
        _testWithConfigBuilder("config_port_missing_ws_no.conf", true);
    }

    @Test
    public void testWithConfigBuilder_config_port_user_ws_no() throws Exception {
        _testWithConfigBuilder("config_port_user_ws_no.conf", true);
    }

    @Test
    public void testWithConfigBuilder_websocket() throws Exception {
        _testWithConfigBuilder("websocket.conf", false);
    }

    @Test
    public void testWithConfigBuilder_ws() throws Exception {
        _testWithConfigBuilder("ws.conf", false);
    }

    private void _testWithConfig(String configFile, boolean checkConnect, String[] configInserts, NatsServerRunner runner) throws IOException {
        validateCommandLine(runner, false, false);
        validateHostAndPort(runner);
        validateConfigLines(runner, configFile, configInserts);
        if (checkConnect) {
            validateConnection(runner);
        }
    }

    private void _testWithConfigParams(String configFile, boolean checkConnect) throws Exception {
        String[] configInserts = { "# custom insert this comment " + configFile};
        try (NatsServerRunner runner = new NatsServerRunner(SOURCE_CONFIG_FILE_PATH + configFile, configInserts, -1, false)) {
            _testWithConfig(configFile, checkConnect, configInserts, runner);
        }
    }

    private void _testWithConfigBuilder(String configFile, boolean checkConnect) throws Exception {
        String[] configInserts = { "# custom insert this comment " + configFile};
        try (NatsServerRunner runner = builder()
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

    @Test
    public void testMappedPorts_mapped_ws_mapped() {
        _testMappedPorts("config_port_mapped_ws_mapped.conf", true, true, MATCH_MAP, MATCH_MAP);
    }

    @Test
    public void testMappedPorts_mapped_ws_user() {
        _testMappedPorts("config_port_mapped_ws_user.conf", true, false, MATCH_MAP, MATCH_USER);
    }

    @Test
    public void testMappedPorts_mapped_ws_no() {
        _testMappedPorts("config_port_mapped_ws_no.conf", true, false, MATCH_MAP, MATCH_NOTHING);
    }

    @Test
    public void testMappedPorts_missing_ws_mapped() {
        _testMappedPorts("config_port_missing_ws_mapped.conf", false, true, MATCH_USER, MATCH_MAP);
    }

    @Test
    public void testMappedPorts_missing_ws_user() {
        _testMappedPorts("config_port_missing_ws_user.conf", false, false, MATCH_4222, MATCH_USER);
    }

    @Test
    public void testMappedPorts_missing_ws_no() {
        _testMappedPorts("config_port_missing_ws_no.conf", false, false, MATCH_USER, MATCH_NOTHING);
    }

    @Test
    public void testMappedPorts_user_ws_mapped() {
        _testMappedPorts("config_port_user_ws_mapped.conf", false, true, MATCH_USER, MATCH_MAP);
    }

    @Test
    public void testMappedPorts_user_ws_no() {
        _testMappedPorts("config_port_user_ws_no.conf", false, false, MATCH_USER, MATCH_MAP);
    }

    private void _testMappedPorts(String configFile, boolean pMapped, boolean wsMapped, int natsMatch, int wsMatch) {
        try {
            NatsServerRunner.Builder builder = builder()
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
                validateConnection(runner);

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
            builder()
                .configFilePath(SOURCE_CONFIG_FILE_PATH + "config_port_user_ws_user.conf")
                .build());
    }

    @Test
    public void testBuilder() {
        Path p = Paths.get(".");
        NatsServerRunner.Builder builder = builder()
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
            .processStartTries(11)
            .processStartRetryDelay(12L)
            .processAliveCheckTries(13)
            .processAliveCheckWait(14L)
            .connectValidateTimeout(15L)
            ;

        assertNull(builder.output);

        validateOptions(p, false, builder.buildOptions());

        builder.outputLogger(Logger.getLogger("testNatsServerRunnerOptionsImpl"));
        validateOptions(p, true, builder.buildOptions());
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
        NatsServerRunner.Builder builder = builder()
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
        Level initial = getDefaultOutputLevel();
        setDefaultOutputLevel(Level.ALL);
        assertEquals(Level.ALL, getDefaultOutputLevel());

        setDefaultOutputLevel(initial);
        assertEquals(initial, getDefaultOutputLevel());

        Supplier<Output> dflt = getDefaultOutputSupplier();
        Supplier<Output> supplier = ConsoleOutput::new;
        assertEquals(dflt, getDefaultOutputSupplier());
        assertNotEquals(supplier, getDefaultOutputSupplier());

        setDefaultOutputSupplier(supplier);
        assertNotEquals(dflt, getDefaultOutputSupplier());
        assertEquals(supplier, getDefaultOutputSupplier());

        setDefaultOutputSupplier(null);
        assertEquals(DefaultLoggingSupplier, getDefaultOutputSupplier());
    }

    @Test
    public void testShutdownCoverage() throws Exception {
        NatsServerRunner runner = builder().build();
        runner.shutdown(false);
        Thread.sleep(1000);
        runner.shutdown();
    }

    @Test
    public void testBadConfig() throws Exception {
        try (NatsServerRunner runner = builder()
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

        NatsServerRunner.Builder b = builder()
            .debug(false)
            .jetstream(true)
            .configInserts(configInserts)
            .port(4242)
            .connectValidateTries(0)
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

    @Test
    public void testTlsFirst() {
        try (NatsServerRunner runner = builder()
            .configFilePath("src/test/resources/tls_first.conf")
//            .skipConnectValidate()
            .build())
        {
            // just wanted to connect
        }
        catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testJsStoragePathNoConfig() throws Exception {
        try (NatsServerRunner runner = builder().jetstream().build()) {
            List<String> lines = Files.readAllLines(Paths.get(runner.getConfigFile()));
            validateContainsOneInstance(lines, "jetstream {");
            validateContainsOneInstance(lines, "store_dir=");
        }
    }

    @Test
    public void testJsStoragePathConfigFileWithoutBlock() throws Exception {
        try (NatsServerRunner runner = builder().jetstream()
            .configFilePath("src/test/resources/simple.conf")
            .build()) {
            List<String> lines = Files.readAllLines(Paths.get(runner.getConfigFile()));
            validateContainsOneInstance(lines, "jetstream {");
            validateContainsOneInstance(lines, "store_dir=");
        }
    }

    @Test
    public void testJsStoragePathConfigFileWithBlock() throws Exception {
        try (NatsServerRunner runner = builder().jetstream()
            .configFilePath("src/test/resources/simple_with_js.conf")
            .build()) {
            List<String> lines = Files.readAllLines(Paths.get(runner.getConfigFile()));
            validateContainsOneInstance(lines, "jetstream {");
            validateContainsOneInstance(lines, "store_dir=");
        }
    }

    @Test
    public void testJsStoragePathConfigInsert() throws Exception {
        JsStorageDir jsStorageDir = JsStorageDir.temporaryInstance();
        try (NatsServerRunner runner = builder().jetstream()
            .configInserts(jsStorageDir.configInserts)
            .build()) {
            List<String> lines = Files.readAllLines(Paths.get(runner.getConfigFile()));
            validateContainsOneInstance(lines, "jetstream {");
            validateContainsOneInstance(lines, "store_dir=");
        }
    }

    @Test
    public void testJsStoragePathConfigFileAndInsert() throws Exception {
        JsStorageDir jsStorageDir = JsStorageDir.temporaryInstance();
        assertThrows(IOException.class, () -> builder().jetstream()
            .configFilePath("src/test/resources/simple_with_js.conf")
            .configInserts(jsStorageDir.configInserts)
            .build());
    }

    private void validateContainsOneInstance(List<String> lines, String s) {
        int count = 0;
        for (String line : lines) {
            if (line.contains(s)) {
                count++;
            }
        }
        assertEquals(1, count, "Config contains " + count + "instances of " + s);
    }
}
