// Copyright 2023-2025 The NATS Authors
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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.nats.NatsRunnerUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class NatsRunnerUtilsTest extends TestBase {

    @Test
    public void testGetUriForPort() {
        //noinspection deprecation
        assertEquals("nats://localhost:1234", NatsRunnerUtils.getURIForPort(1234));
    }

    @Test
    public void testGetNatsLocalhostUri() {
        assertEquals("nats://localhost:1234", NatsRunnerUtils.getNatsLocalhostUri(1234));
    }

    @Test
    public void testGetNatsUri() {
        assertEquals("nats://host:1234", NatsRunnerUtils.getNatsUri("host", 1234));
    }

    @Test
    public void testGetLocalhostUri() {
        assertEquals("schema://localhost:1234", NatsRunnerUtils.getLocalhostUri("schema", 1234));
    }

    @Test
    public void testGetNatsServerVersionString() {
        String v = NatsRunnerUtils.getNatsServerVersionString();
        assertNotNull(v);
        assertTrue(v.startsWith("nats-server: v"));
    }

    @Test
    public void testGetResolvedServerPath() {
        assertEquals(DEFAULT_NATS_SERVER, getResolvedServerPath());

        //noinspection deprecation
        NatsRunnerUtils.setServerPath("SetNatsServer");
        assertEquals("SetNatsServer", getResolvedServerPath());

        //noinspection deprecation
        NatsRunnerUtils.clearServerPath();
        assertEquals(DEFAULT_NATS_SERVER, getResolvedServerPath());
    }

    @Test
    public void testCreateClusterInserts() throws IOException {
        Path t = Paths.get("path");
        int p = 4220;
        int l = 4230;
        int m = 4280;
        String c = "cluster";
        String s = "server";
        String h = "127.0.0.1";

        validateClusterInserts(3, p, l, c, s, h, null, null, createClusterInserts());
        validateClusterInserts(2, p, l, c, s, h, null, null, createClusterInserts(2));
        validateClusterInserts(3, p, l, c, s, h, null, t, createClusterInserts(t));
        validateClusterInserts(2, p, l, c, s, h, null, t, createClusterInserts(2, t));

        c = "clstr";
        s = "srvr";
        validateClusterInserts(2, p, l, c, s, h, null, null, createClusterInserts(2, c, s));
        validateClusterInserts(2, p, l, c, s, h, null, t, createClusterInserts(2, c, s, t));
        validateClusterInserts(2, p, l, c, s, h, null, null, createClusterInserts(2, c, s, false));
        validateClusterInserts(2, p, l, c, s, h, m, null, createClusterInserts(2, c, s, true));
        validateClusterInserts(2, p, l, c, s, h, null, t, createClusterInserts(2, c, s, false, t));
        validateClusterInserts(2, p, l, c, s, h, m, t, createClusterInserts(2, c, s, true, t));

        p = 5220;
        l = 5230;
        m = 5280;
        h = "host";
        c = "cluster";
        s = "server";

        defaultHost(h);
        defaultPortStart(p);
        defaultListenStart(l);
        defaultMonitorStart(m);

        validateClusterInserts(3, p, l, c, s, h, null, null, createClusterInserts());
        validateClusterInserts(2, p, l, c, s, h, null, null, createClusterInserts(2));
        validateClusterInserts(3, p, l, c, s, h, null, t, createClusterInserts(t));
        validateClusterInserts(2, p, l, c, s, h, null, t, createClusterInserts(2, t));

        c = "clstr";
        s = "srvr";
        validateClusterInserts(2, p, l, c, s, h, null, null, createClusterInserts(2, c, s));
        validateClusterInserts(2, p, l, c, s, h, null, t, createClusterInserts(2, c, s, t));
        validateClusterInserts(2, p, l, c, s, h, null, null, createClusterInserts(2, c, s, false));
        validateClusterInserts(2, p, l, c, s, h, m, null, createClusterInserts(2, c, s, true));
        validateClusterInserts(2, p, l, c, s, h, null, t, createClusterInserts(2, c, s, false, t));
        validateClusterInserts(2, p, l, c, s, h, m, t, createClusterInserts(2, c, s, true, t));
    }

    private static void validateClusterInserts(int count,
                                               int basePort,
                                               int baseListen,
                                               String cluster,
                                               String server,
                                               String host,
                                               Integer baseMonitor,
                                               Path jsStoreDir,
                                               List<ClusterInsert> list) {
        assertEquals(count, list.size());
        for (int x = 0; x < list.size(); x++) {
            int port = basePort + x;
            int listen = baseListen + x;
            ClusterInsert ci = list.get(x);
            assertEquals(cluster, ci.node.clusterName);
            assertEquals(server + x, ci.node.serverName);
            assertEquals(port, ci.node.port);
            assertEquals(listen, ci.node.listen);
            assertEquals(host, ci.node.host);
            if (baseMonitor == null) {
                assertNull(ci.node.monitor);
            }
            else {
                assertEquals(baseMonitor + x, ci.node.monitor);
            }
            if (jsStoreDir == null) {
                assertNull(ci.node.jsStoreDir);
            }
            else {
                assertEquals(jsStoreDir + "\\" + port, ci.node.jsStoreDir.toString());
            }
        }
    }
}