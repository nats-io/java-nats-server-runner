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
import java.nio.file.Files;
import java.util.Arrays;

import static io.nats.JsConfig.STORE_DIR;
import static org.junit.jupiter.api.Assertions.*;

public class JsConfigTest extends TestBase {

    private static final String TEST_STORE_DIR = "StOrEdIr";
    @Test
    public void testConstruction() throws IOException {
        String expanded = "jetstream {\n" +
            "   store_dir=" + TEST_STORE_DIR + ",\n" +
            "   max_mem_store:1GB,\n" +
            "   max_file_store:2GB,\n" +
            "   domain: DOMAIN\n" +
            "}";
        String expandedAtEnd = "jetstream {\n" +
            "   max_mem_store:1GB,\n" +
            "   max_file_store:2GB,\n" +
            "   domain: DOMAIN,\n" +
            "   store_dir=" + TEST_STORE_DIR + "\n" +
            "}";
        String expandedJustJetStream = "jetstream {\n" +
            "}";

        String inline = "jetstream {store_dir=" + TEST_STORE_DIR + ",max_mem_store:1GB,max_file_store:2GB,domain: DOMAIN}";
        String inlineAtEnd = "jetstream {max_mem_store:1GB,max_file_store:2GB,domain: DOMAIN,store_dir=" + TEST_STORE_DIR + "}";
        String inlineJustJetStream = "jetstream {}";

        String expandedNoValue = expanded.replace(TEST_STORE_DIR, "");
        String expandedAtEndNoValue = expandedAtEnd.replace(TEST_STORE_DIR, "");
        String inlineNoValue = inline.replace(TEST_STORE_DIR, "");
        String inlineAtEndNoValue = inlineAtEnd.replace(TEST_STORE_DIR, "");

        String expandedNoStore = expandedNoValue.replace("store_dir=,", "");
        String inlineNoStore = inlineNoValue.replace("store_dir=,", "");

        validate6(expanded, true);
        validate6(expandedAtEnd, true);
        validate6(inline, true);
        validate6(inlineAtEnd, true);

        validate6(expandedNoValue, false);
        validate6(expandedAtEndNoValue, false);
        validate6(inlineNoValue, false);
        validate6(inlineAtEndNoValue, false);

        validate6(expandedNoStore, false);
        validate6(inlineNoStore, false);

        validate3(new JsConfig(), false);
        validate3(new JsConfig(Files.createTempDirectory(null)), false);

        validate3("jetstream{}", false);
        validate3("jetstream {}", false);
        validate3("jetstream { }", false);
        validate3(expandedJustJetStream, false);
        validate3(inlineJustJetStream, false);

        validate3(toConfig("jetstream: enabled"), false);
        validate3(toConfig("jetstream:enabled"), false);

        assertThrows(IllegalArgumentException.class, () -> toConfig("x"));
        assertThrows(IllegalArgumentException.class, () -> toConfig("jetstream {"));
        assertThrows(IllegalArgumentException.class, () -> toConfig("jetstream { x"));
        assertThrows(IllegalArgumentException.class, () -> toConfig("jetstream: {"));
        assertThrows(IllegalArgumentException.class, () -> toConfig("jetstream: { x"));
        assertThrows(IllegalArgumentException.class, () -> toConfig("jetstream"));
        assertThrows(IllegalArgumentException.class, () -> toConfig("jetstream x"));
        assertThrows(IllegalArgumentException.class, () -> toConfig("jetstream:"));
        assertThrows(IllegalArgumentException.class, () -> toConfig("jetstream: x"));
    }

    private static void validate3(String testString, boolean expectTestStoreDir) throws IOException {
        validate3(toConfig(testString), expectTestStoreDir);
        validate3(toConfig(colonize(testString)), expectTestStoreDir);
        validate3(toConfig(testString.replace("store_dir=", "store_dir:")), expectTestStoreDir);
        validate3(toConfig(testString.replace("store_dir=", "store_dir ")), expectTestStoreDir);
    }

    private static void validate3(JsConfig jsConfig, boolean expectTestStoreDir) {
        assertEquals(3, jsConfig.configInserts.size());
        assertEquals("jetstream {", jsConfig.configInserts.get(0));
        assertEquals("  " + STORE_DIR + "=" + jsConfig.storeDir, jsConfig.configInserts.get(1));
        assertEquals("}", jsConfig.configInserts.get(2));
    }

    private static void validate6(String testString, boolean expectTestStoreDir) throws IOException {
        validate6(toConfig(testString), expectTestStoreDir);
        validate6(toConfig(colonize(testString)), expectTestStoreDir);
        validate6(toConfig(testString.replace("store_dir=", "store_dir:")), expectTestStoreDir);
        validate6(toConfig(testString.replace("store_dir=", "store_dir ")), expectTestStoreDir);
    }

    private static void validate6(JsConfig jsConfig, boolean expectTestStoreDir) {
        assertEquals(6, jsConfig.configInserts.size());
        assertEquals("jetstream {", jsConfig.configInserts.get(0));
        assertEquals("  max_mem_store:1GB,", jsConfig.configInserts.get(1));
        assertEquals("  max_file_store:2GB,", jsConfig.configInserts.get(2));
        assertEquals("  domain: DOMAIN", jsConfig.configInserts.get(3));
        assertEquals("  " + STORE_DIR + "=" + jsConfig.storeDir, jsConfig.configInserts.get(4));
        assertEquals("}", jsConfig.configInserts.get(5));
        if (expectTestStoreDir) {
            assertEquals(TEST_STORE_DIR, jsConfig.storeDir);
            assertTrue(jsConfig.configInserts.get(4).contains(TEST_STORE_DIR));
        }
    }

    private static String colonize(String jsString) {
        return jsString.replace("jetstream", "jetstream:");
    }
    private static JsConfig toConfig(String testString) throws IOException {
        return new JsConfig(Arrays.asList(testString.split("\\n")));
    }
}