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

import java.util.List;

import static nats.io.NatsRunnerUtils.createClusterInserts;

public class NatsClusterTest extends TestBase {

    @Test
    public void testCreateCluster() throws Exception {
        List<ClusterInsert> clusterInserts = createClusterInserts();
        ClusterInsert ci1 = clusterInserts.get(0);
        ClusterInsert ci2 = clusterInserts.get(1);
        ClusterInsert ci3 = clusterInserts.get(2);

        try (NatsServerRunner runner1 = new NatsServerRunner(ci1.port, false, false, null, ci1.configInserts, null)) {
            try (NatsServerRunner runner2 = new NatsServerRunner(ci2.port, false, false, null, ci2.configInserts, null)) {
                try (NatsServerRunner runner3 = new NatsServerRunner(ci3.port, false, false, null, ci3.configInserts, null)) {

                    Thread.sleep(5000); // give servers time to spin up and be ready

                    validateCommandLine(runner1, false, false);
                    validateHostAndPort(runner1);
                    validateConfigLines(runner1);
                    connect(runner1);

                    validateCommandLine(runner2, false, false);
                    validateHostAndPort(runner2);
                    validateConfigLines(runner2);
                    connect(runner2);

                    validateCommandLine(runner3, false, false);
                    validateHostAndPort(runner3);
                    validateConfigLines(runner3);
                    connect(runner3);
                }
            }
        }
    }
}
