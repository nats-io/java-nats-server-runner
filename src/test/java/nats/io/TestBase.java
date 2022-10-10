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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static nats.io.NatsRunnerUtils.DEBUG_OPTION;
import static nats.io.NatsRunnerUtils.JETSTREAM_OPTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.commons.util.CollectionUtils.toUnmodifiableList;

class TestBase {

    protected static final String SOURCE_CONFIG_FILE_PATH = "src/test/resources/";
    
    protected static final byte[] CONNECT_BYTES = "CONNECT {\"lang\":\"java\",\"version\":\"9.99.9\",\"protocol\":1,\"verbose\":false,\"pedantic\":false,\"tls_required\":false,\"echo\":true,\"headers\":true,\"no_responders\":true}\r\n".getBytes();

    protected void validateCommandLine(NatsServerRunner runner, boolean debug, boolean jetStream, String... customArgs) {
        assertEquals(debug, runner.getCmdLine().contains(" " + DEBUG_OPTION));
        assertEquals(jetStream, runner.getCmdLine().contains(" " + JETSTREAM_OPTION));
        for (String ca : customArgs) {
            assertTrue(runner.getCmdLine().contains(" " + ca));
        }
    }

    protected void validateHostAndPort(NatsServerRunner server) {
        assertTrue(server.getPort() > 0);
        assertTrue(server.getPort() != 1234);
        assertTrue(server.getURI().startsWith("nats://localhost"));
    }

    protected void validateConfigLines(NatsServerRunner runner) throws IOException {
        validateConfigLines(runner, null);
    }

    protected void validateConfigLines(NatsServerRunner runner, String configFile, String[] configInserts) throws IOException {
        List<String> expected = Files.lines(new File(SOURCE_CONFIG_FILE_PATH + configFile).toPath())
            .map(String::trim)
            .filter(s -> s.length() > 0)
            .filter(s -> !s.contains("port"))
            .collect(toList());
        Collections.addAll(expected, configInserts);
        validateConfigLines(runner, expected);
    }

    protected void validateConfigLines(NatsServerRunner runner, List<String> expected) throws IOException {
        List<String> lines = Files.lines(new File(runner.getConfigFile()).toPath())
            .map(String::trim)
            .filter(s -> s.length() > 0)
            .collect(toUnmodifiableList());

        assertTrue(lines.contains("port: " + runner.getPort()));
        if (expected != null) {
            for (String ex : expected) {
                assertTrue(lines.contains(ex));
            }
        }
    }

    protected void connect(NatsServerRunner runner) throws IOException {
        Socket socket = new Socket();
        SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", runner.getPort());
        socket.connect(socketAddress);
        assertEquals(runner.getPort(), socket.getPort());

        socket.getOutputStream().write(CONNECT_BYTES);
        socket.getOutputStream().flush();

        InputStream in = socket.getInputStream();
        // give the server time to respond or this flaps
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }

        StringBuilder sb = new StringBuilder();
        int cr = 0;
        int i = in.read();
        while (i != -1) {
            sb.append((char)i);
            if (i == 13) {
                cr++;
            }
            i = (cr > 1) ? -1 : in.read();
        }
        in.close();

        String sbs = sb.toString().trim();
        assertTrue(sbs.startsWith("INFO"));
        assertTrue(sbs.contains("\"port\":" + runner.getPort()));
    }
}
