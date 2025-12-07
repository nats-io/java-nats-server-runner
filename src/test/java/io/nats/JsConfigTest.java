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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsConfigTest extends TestBase {

    @Test
    public void testConstruction() throws IOException {
        String expanded = "jetstream {\n" +
            "   store_dir=blah,\n" +
            "   max_mem_store:1GB,\n" +
            "   max_file_store:2GB\n" +
            "}";
        String expandedAtEnd = "jetstream {\n" +
            "   max_mem_store:1GB,\n" +
            "   max_file_store:2GB,\n" +
            "   store_dir=blah\n" +
            "}";
        String expandedJustJetStream = "jetstream {\n" +
            "}";

        String inline = "jetstream {store_dir=blah,max_mem_store:1GB,max_file_store:2GB}";
        String inlineAtEnd = "jetstream {max_mem_store:1GB,max_file_store:2GB,store_dir=blah}";
        String inlineJustJetStream = "jetstream {}";

        String expandedNoValue = expanded.replace("blah", "");
        String expandedAtEndNoValue = expandedAtEnd.replace("blah", "");
        String inlineNoValue = inline.replace("blah", "");
        String inlineAtEndNoValue = inlineAtEnd.replace("blah", "");

        String expandedNoStore = expandedNoValue.replace("store_dir=,", "");
        String inlineNoStore = inlineNoValue.replace("store_dir=,", "");

        validate5(expanded);
        validate5(expandedAtEnd);
        validate5(inline);
        validate5(inlineAtEnd);

        validate5(expandedNoValue);
        validate5(expandedAtEndNoValue);
        validate5(inlineNoValue);
        validate5(inlineAtEndNoValue);

        validate5(expandedNoStore);
        validate5(inlineNoStore);

        validate3(new JsConfig());
        validate3(new JsConfig(Files.createTempDirectory(null)));

        validate3("jetstream{}");
        validate3("jetstream {}");
        validate3("jetstream { }");
        validate3(expandedJustJetStream);
        validate3(inlineJustJetStream);

        validate3(toConfig("jetstream: enabled"));
        validate3(toConfig("jetstream:enabled"));

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

    private static void validate3(String testString) throws IOException {
        validate3(toConfig(testString));
        validate3(toConfig(colonize(testString)));
    }

    private static void validate3(JsConfig jsConfig) {
        assertEquals(3, jsConfig.configInserts.size());
        assertEquals("jetstream {", jsConfig.configInserts.get(0));
        assertEquals("    " + jsConfig.storeDir, jsConfig.configInserts.get(1));
        assertEquals("}", jsConfig.configInserts.get(2));
    }

    private static void validate5(String testString) throws IOException {
        validate5(toConfig(testString));
        validate5(toConfig(colonize(testString)));
    }

    private static void validate5(JsConfig jsConfig) {
        assertEquals(5, jsConfig.configInserts.size());
        assertEquals("jetstream {", jsConfig.configInserts.get(0));
        assertEquals("    max_mem_store:1GB,", jsConfig.configInserts.get(1));
        assertEquals("    max_file_store:2GB,", jsConfig.configInserts.get(2));
        assertEquals("    " + jsConfig.storeDir, jsConfig.configInserts.get(3));
        assertEquals("}", jsConfig.configInserts.get(4));
    }

    private static String colonize(String jsString) {
        return jsString.replace("jetstream", "jetstream:");
    }
    private static JsConfig toConfig(String testString) throws IOException {
        return new JsConfig(Arrays.asList(testString.split("\\n")));
    }
}