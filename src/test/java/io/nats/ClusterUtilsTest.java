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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.nats.ClusterUtils.DEFAULT_CLUSTER_DEFAULTS;
import static io.nats.ClusterUtils.createClusterInserts;
import static org.junit.jupiter.api.Assertions.*;

public class ClusterUtilsTest extends TestBase {

    @Test
    public void testCreateClusterInserts() {
        Path jsStoreDirBase = Paths.get("path");
        int p = DEFAULT_CLUSTER_DEFAULTS.getPortStart();
        int l = DEFAULT_CLUSTER_DEFAULTS.getListenStart();
        int m = DEFAULT_CLUSTER_DEFAULTS.getMonitorStart();
        String c = DEFAULT_CLUSTER_DEFAULTS.getClusterName();
        String s = DEFAULT_CLUSTER_DEFAULTS.getServerNamePrefix();
        String h = DEFAULT_CLUSTER_DEFAULTS.getHost();

        validateClusterInserts(3, p, l, c, s, h, false, m, null, createClusterInserts());
        validateClusterInserts(3, p, l, c, s, h, false, m, jsStoreDirBase, createClusterInserts(jsStoreDirBase));

        ClusterDefaults cd = new ClusterDefaults()
            .count(3)
            .portStart(p)
            .listenStart(l)
            .monitorStart(m)
            .clusterName(c)
            .serverNamePrefix(s)
            .host(h);
        validateClusterInserts(3, p, l, c, s, h, true, m, null, createClusterInserts(cd));
        validateClusterInserts(3, p, l, c, s, h, true, m, jsStoreDirBase, createClusterInserts(cd, jsStoreDirBase));

        p = 5220;
        l = 5230;
        m = 5280;
        c = "clstr";
        s = "srvr";
        cd = new ClusterDefaults()
            .count(2)
            .portStart(p)
            .listenStart(l)
            .monitorStart(m)
            .clusterName(c)
            .serverNamePrefix(s)
            .host(h)
            .monitorStart(m);
        validateClusterInserts(2, p, l, c, s, h, true, m, null, createClusterInserts(cd));
        validateClusterInserts(2, p, l, c, s, h, true, m, jsStoreDirBase, createClusterInserts(cd, jsStoreDirBase));
    }

    private static void validateClusterInserts(int count,
                                               int basePort,
                                               int baseListen,
                                               String cluster,
                                               String server,
                                               String host,
                                               boolean hasMonitor,
                                               int baseMonitor,
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
            if (hasMonitor) {
                assertEquals(baseMonitor + x, ci.node.monitor);
            }
            else {
                assertNull(ci.node.monitor);
            }
            if (jsStoreDir == null) {
                assertNull(ci.node.jsStoreDir);
            }
            else {
                assertNotNull(ci.node.jsStoreDir);
                if (File.separatorChar == '\\') {
                    assertEquals(jsStoreDir + "\\" + port, ci.node.jsStoreDir.toString());
                }
                else {
                    assertEquals(jsStoreDir + "/" + port, ci.node.jsStoreDir.toString());
                }
            }
        }
    }
}