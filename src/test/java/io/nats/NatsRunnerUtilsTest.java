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

import static io.nats.NatsRunnerUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class NatsRunnerUtilsTest extends TestBase {
    @Test
    public void testGetNatsLocalhostUri() {
        assertEquals(natsLocalHostFromDefault(1234), getNatsLocalhostUri(1234));
    }

    @Test
    public void testGetNatsUri() {
        assertEquals("nats://host:1234", getNatsUri("host", 1234));
    }

    @Test
    public void testGetLocalhostUri() {
        assertEquals(localHostFromDefault("schema", 1234), getLocalhostUri("schema", 1234));
    }

    @Test
    public void testGetNatsServerVersionString() {
        String v = VersionUtils.getNatsServerVersionString();
        assertNotNull(v);
        assertTrue(v.startsWith("nats-server: v"));
    }

    @Test
    public void testGetResolvedServerPath() {
        assertEquals(DEFAULT_NATS_SERVER, getResolvedServerPath());
    }
}