// Copyright 2023 The NATS Authors
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

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static io.nats.NatsRunnerUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class NatsRunnerUtilsTest extends TestBase {

    @Test
    public void testGetUriForPort() throws Exception {
        //noinspection deprecation
        assertEquals("nats://localhost:1234", NatsRunnerUtils.getURIForPort(1234));
    }

    @Test
    public void testGetNatsServerVersionString() throws Exception {
        String v = NatsRunnerUtils.getNatsServerVersionString();
        assertNotNull(v);
        assertTrue(v.startsWith("nats-server: v"));
    }

    @Test
    public void testGetResolvedServerPath() throws Exception {
        withEnvironmentVariable(NATS_SERVER_PATH_ENV, "TestNatsServer")
            .execute(() -> assertEquals("TestNatsServer", getResolvedServerPath()));

        assertEquals(DEFAULT_NATS_SERVER, getResolvedServerPath());

        //noinspection deprecation
        NatsRunnerUtils.setServerPath("SetNatsServer");
        assertEquals("SetNatsServer", getResolvedServerPath());

        //noinspection deprecation
        NatsRunnerUtils.clearServerPath();
        assertEquals(DEFAULT_NATS_SERVER, getResolvedServerPath());
    }
}