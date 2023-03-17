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

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static nats.io.NatsRunnerUtils.DEFAULT_CLUSTER_NAME;
import static nats.io.NatsRunnerUtils.createClusterInserts;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NatsClusterTest extends TestBase {

    @Test
    public void testCreateCluster() throws Exception {
        _testCreateCluster(createClusterInserts());
        _testCreateCluster(createClusterInserts(3));
        List<ClusterInsert> clusterInserts = createClusterInserts(3);

        List<ClusterNode> nodes = new ArrayList<>();
        for (ClusterInsert ci : clusterInserts) {
            nodes.add(new ClusterNode(ci.node.clusterName, ci.node.serverName, ci.node.port, ci.node.listen));
        }
        _testCreateCluster(NatsRunnerUtils.createClusterInserts(nodes));

        EqualsVerifier.simple().forClass(ClusterInsert.class).verify();

        ClusterInsert ci = clusterInserts.get(0);
        String s = ci.toString();
        assertTrue(s.contains("port:" + ci.node.port));
        assertTrue(s.contains("listen:" + ci.node.host + ":" + ci.node.listen));
        assertTrue(s.contains("name: " + DEFAULT_CLUSTER_NAME));
    }

    private void _testCreateCluster(List<ClusterInsert> clusterInserts) throws Exception {
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

                    connect(runner0);

                    validateCommandLine(runner1, false, false);
                    validateHostAndPort(runner1);
                    validateConfigLines(runner1, Collections.singletonList("name: cluster"));
                    connect(runner1);

                    validateCommandLine(runner2, false, false);
                    validateHostAndPort(runner2);
                    validateConfigLines(runner2, Collections.singletonList("name: cluster"));
                    connect(runner2);
                }
            }
        }
    }
}
