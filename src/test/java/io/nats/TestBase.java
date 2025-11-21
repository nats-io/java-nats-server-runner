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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;

import static io.nats.NatsRunnerUtils.*;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.commons.util.CollectionUtils.toUnmodifiableList;

public class TestBase {

    protected static final String SOURCE_CONFIG_FILE_PATH = "src/test/resources/";

    static {
        setDefaultOutputLevel(Level.WARNING);
    }

    public static String localHostFromDefaultNoPort(String schema) {
        return schema + "://" + getDefaultLocalhostHost().host;
    }

    public static String localHostFromDefault(String schema, int port) {
        return localHostFromDefaultNoPort(schema) + ":" + port;
    }

    public static String natsLocalHostFromDefaultNoPort() {
        return "nats://" + getDefaultLocalhostHost().host;
    }

    public static String natsLocalHostFromDefault(int port) {
        return natsLocalHostFromDefaultNoPort() + ":" + port;
    }

    protected void validateBasics(NatsServerRunner runner, boolean debug, boolean jetStream) throws IOException {
        validateCommandLine(runner, debug, jetStream);
        validateHostAndPort(runner);
        validateConfigLines(runner);
        validateConnection(runner);
    }

    protected void validateCommandLine(NatsServerRunner runner, boolean debug, boolean jetStream, String... customArgs) {
        assertEquals(debug, runner.getCmdLine().contains(" " + DebugLevel.DEBUG_TRACE.getCmdOption()));
        assertEquals(jetStream, runner.getCmdLine().contains(" " + JETSTREAM_OPTION));
        for (String ca : customArgs) {
            assertTrue(runner.getCmdLine().contains(" " + ca));
        }
    }

    protected void validateHostAndPort(NatsServerRunner server) {
        assertTrue(server.getPort() > 0);
        assertTrue(server.getPort() != 1234);
        assertTrue(server.getNatsLocalhostUri().startsWith(natsLocalHostFromDefaultNoPort()));
    }

    protected void validateConfigLines(NatsServerRunner runner) throws IOException {
        validateConfigLines(runner, null);
    }

    protected void validateConfigLines(NatsServerRunner runner, String configFile, String[] configInserts) throws IOException {
        //noinspection resource
        List<String> expected = Files.lines(new File(SOURCE_CONFIG_FILE_PATH + configFile).toPath())
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .filter(s -> !s.contains("port"))
            .collect(toList());
        Collections.addAll(expected, configInserts);
        validateConfigLines(runner, expected);
    }

    protected void validateConfigLines(NatsServerRunner runner, List<String> expected) throws IOException {
        List<String> lines = getConfigLinesRemoveEmpty(runner);
        if (lines == null) {
            assertTrue(runner.getCmdLine().contains("port " + runner.getPort()));
        }
        else {
            assertTrue(lines.contains("port: " + runner.getPort()));
            if (expected != null) {
                for (String ex : expected) {
                    assertTrue(lines.contains(ex));
                }
            }
        }
    }

    protected static List<String> getConfigLinesRemoveEmpty(NatsServerRunner runner) throws IOException {
        String cfg = runner.getConfigFile();
        if (cfg == null) {
            return null;
        }
        try (Stream<String> stream = Files.lines(new File(cfg).toPath())) {
            return stream.map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(toUnmodifiableList());
        }
    }

    protected void validateConnection(NatsServerRunner runner) {
        try {
            NatsServerRunner.isServerReachable(runner.getNatsPort(), 200);
        }
        catch (IOException e) {
            fail();
        }
    }
}
