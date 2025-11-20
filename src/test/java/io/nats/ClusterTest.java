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

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static io.nats.ClusterUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@Isolated
public class ClusterTest extends TestBase {

    @Test
    public void testCreateCluster() throws Exception {
        _testCreateCluster(createClusterInserts(), false);
        _testCreateCluster(createClusterInserts(3), false);
        _testCreateCluster(createClusterInserts(createTemporaryJetStreamStoreDirBase()), true);
        _testCreateCluster(createClusterInserts(3, createTemporaryJetStreamStoreDirBase()), true);
    }

    private void _testCreateCluster(List<ClusterInsert> clusterInserts, boolean js) throws Exception {
        for (ClusterInsert ci : clusterInserts) {
            String s = ci.toString();
            assertTrue(s.contains("port: " + ci.node.port));
            assertTrue(s.contains("server_name=" + getDefaultServerNamePrefix()));
            assertTrue(s.contains("listen: " + getDefaultClusterHost() + ":" + ci.node.listen));
            assertTrue(s.contains("name: " + getDefaultClusterName()));
            if (js) {
                assertTrue(s.contains("jetstream"));
                assertTrue(s.contains("store_dir="));
            }
        }

        ClusterInsert ci0 = clusterInserts.get(0);
        ClusterInsert ci1 = clusterInserts.get(1);
        ClusterInsert ci2 = clusterInserts.get(2);

        try (NatsServerRunner runner0 = new NatsServerRunner(ci0.node.port, false, false, null, ci0.configInserts, null)) {
            try (NatsServerRunner runner1 = new NatsServerRunner(ci1.node.port, false, false, null, ci1.configInserts, null)) {
                try (NatsServerRunner runner2 = new NatsServerRunner(ci2.node.port, false, false, null, ci2.configInserts, null)) {

                    Thread.sleep(5000); // give servers time to spin up and be ready

                    validateCommandLine(runner0, false, false);
                    validateHostAndPort(runner0);
                    validateConfigLines(runner0, Collections.singletonList("name: cluster"));
                    validateConnection(runner0);

                    validateCommandLine(runner1, false, false);
                    validateHostAndPort(runner1);
                    validateConfigLines(runner1, Collections.singletonList("name: cluster"));
                    validateConnection(runner1);

                    validateCommandLine(runner2, false, false);
                    validateHostAndPort(runner2);
                    validateConfigLines(runner2, Collections.singletonList("name: cluster"));
                    validateConnection(runner2);
                }
            }
        }
    }

    @Test
    public void testClusterNodeConstruction() {
        ClusterNode cn = new ClusterNode("name", "server", 1234, 5678);
        assertEquals("name", cn.clusterName);
        assertEquals("server", cn.serverName);
        assertNull(cn.host);
        assertEquals(1234, cn.port);
        assertEquals(5678, cn.listen);
        assertNull(cn.monitor);
        assertNull(cn.jsStoreDir);

        cn = new ClusterNode("name", "server", 1234, 5678, 9999);
        assertEquals("name", cn.clusterName);
        assertEquals("server", cn.serverName);
        assertNull(cn.host);
        assertEquals(1234, cn.port);
        assertEquals(5678, cn.listen);
        assertNotNull(cn.monitor);
        assertEquals(9999, cn.monitor);
        assertNull(cn.jsStoreDir);

        cn = new ClusterNode("name", "server", 1234, 5678, Paths.get("path"));
        assertEquals("name", cn.clusterName);
        assertEquals("server", cn.serverName);
        assertNull(cn.host);
        assertEquals(1234, cn.port);
        assertEquals(5678, cn.listen);
        assertNull(cn.monitor);
        assertNotNull(cn.jsStoreDir);
        assertEquals("path", cn.jsStoreDir.toString());

        cn = new ClusterNode("name", "server", "host", 1234, 5678, 9999, Paths.get("path"));
        assertEquals("name", cn.clusterName);
        assertEquals("server", cn.serverName);
        assertEquals("host", cn.host);
        assertEquals(1234, cn.port);
        assertEquals(5678, cn.listen);
        assertNotNull(cn.monitor);
        assertEquals(9999, cn.monitor);
        assertNotNull(cn.jsStoreDir);
        assertEquals("path", cn.jsStoreDir.toString());

        cn = ClusterNode.builder()
            .clusterName("name")
            .serverName("server")
            .host("host")
            .port(1234)
            .listen(5678)
            .monitor(9999)
            .jsStoreDir(Paths.get("path"))
            .build()
        ;
        assertEquals("name", cn.clusterName);
        assertEquals("server", cn.serverName);
        assertEquals("host", cn.host);
        assertEquals(1234, cn.port);
        assertEquals(5678, cn.listen);
        assertNotNull(cn.monitor);
        assertEquals(9999, cn.monitor);
        assertNotNull(cn.jsStoreDir);
        assertEquals("path", cn.jsStoreDir.toString());
    }

    @Test
    public void testClusterInsertCoverage() {

        ClusterNode cn = ClusterNode.builder()
            .clusterName("name")
            .serverName("server")
            .host("host")
            .port(1234)
            .listen(5678)
            .monitor(9999)
            .jsStoreDir(Paths.get("path"))
            .build()
        ;

        ClusterInsert ci = new ClusterInsert(cn, null);
        assertEquals("name", ci.node.clusterName);
        assertEquals("server", ci.node.serverName);
        assertEquals("host", ci.node.host);
        assertEquals(1234, ci.node.port);
        assertEquals(5678, ci.node.listen);
        assertNotNull(ci.node.monitor);
        assertEquals(9999, ci.node.monitor);
        assertNotNull(ci.node.jsStoreDir);
        assertEquals("path", ci.node.jsStoreDir.toString());

        assertNull(ci.configInserts);

        ci = new ClusterInsert(cn, new String[0]);
        assertNull(ci.configInserts);

        ci = new ClusterInsert(cn, new String[]{"insert"});
        assertNotNull(ci.configInserts);
        assertEquals("insert", ci.configInserts[0]);
    }
}
