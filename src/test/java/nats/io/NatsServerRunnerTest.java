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
import java.util.Arrays;
import java.util.stream.Stream;

public class NatsServerRunnerTest extends TestBase {

    @Test
    public void testWithoutConfigDefault() throws Exception {
        try (NatsServerRunner runner = new NatsServerRunner()) {
            validateBasics(runner, false, false);
        }
    }

    @Test
    public void testWithoutConfigDefaultBuilder() throws Exception {
        try (NatsServerRunner runner = NatsServerRunner.builder().build()) {
            validateBasics(runner, false, false);
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
    public void testWithoutDebugAndJetStream(boolean debug, boolean jetStream) throws Exception {
        try (NatsServerRunner runner = new NatsServerRunner(debug, jetStream)) {
            validateBasics(runner, debug, jetStream);
        }
    }

    @ParameterizedTest
    @MethodSource("withoutDebugAndJetStreamArgs")
    public void testWithoutDebugAndJetStreamBuilder(boolean debug, boolean jetStream) throws Exception {
        try (NatsServerRunner runner = NatsServerRunner.builder()
            .debug(debug)
            .jetstream(jetStream)
            .build())
        {
            validateBasics(runner, debug, jetStream);
        }
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
}
