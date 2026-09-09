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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static io.nats.JsConfig.STORE_DIR;
import static io.nats.NatsRunnerUtils.cleanDir;
import static io.nats.NatsRunnerUtils.fixDir;
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

    @Test
    public void testStoreDirEscaping() throws IOException {
        // TEST_STORE_DIR has no separators, so these use a real path to exercise the escaping
        JsConfig fromPath = new JsConfig(Files.createTempDirectory(null));
        validateStoreDir(fromPath, fromPath.configInserts.get(1));

        JsConfig noArg = new JsConfig();
        validateStoreDir(noArg, noArg.configInserts.get(1));

        if (File.separatorChar == '\\') {
            assertEquals("C:\\temp\\x", cleanDir("C:\\temp\\x"));
            assertEquals("C:\\temp\\x", cleanDir("C:\\\\temp\\\\x"));
            assertEquals("C:\\\\temp\\\\x", fixDir("C:\\temp\\x"));
            assertEquals("C:\\\\temp\\\\x", fixDir("C:/temp/x"));

            // a leading \\ is a UNC prefix, not an escaped separator
            assertEquals("\\\\server\\share", cleanDir("\\\\server\\share"));
            assertEquals("\\\\server\\share", cleanDir("\\\\\\\\server\\\\share"));
            assertEquals("\\\\\\\\server\\\\share", fixDir("\\\\server\\share"));
        }
        else {
            assertEquals("/tmp/x", cleanDir("/tmp/x"));
            assertEquals("/tmp/x", fixDir("/tmp/x"));

            // a backslash on non-windows is normalized, not escaped, so the two fields
            // never end up naming different directories
            assertEquals("/tmp/a/b", cleanDir("/tmp/a\\b"));
            assertEquals("/tmp/a/b", fixDir("/tmp/a\\b"));
            JsConfig backslash = new JsConfig(Paths.get("/tmp/a\\b"));
            assertEquals("/tmp/a/b", backslash.storeDir);
            assertEquals(backslash.storeDir, backslash.fixedDir);
            validateStoreDir(backslash, backslash.configInserts.get(1));
        }
    }

    @Test
    public void testStoreDirParsing() throws IOException {
        // all three separator forms, unix style
        assertEquals("/tmp/x", storeDirOf("store_dir=/tmp/x"));
        assertEquals("/tmp/x", storeDirOf("store_dir:/tmp/x"));
        assertEquals("/tmp/x", storeDirOf("store_dir /tmp/x"));

        // the drive letter has to survive. The separator is the first :, = or space
        // after the key, otherwise the colon in "C:" gets taken as the separator
        String[] lines = {"store_dir=C:\\temp\\x", "store_dir:C:\\temp\\x", "store_dir C:\\temp\\x"};
        for (String line : lines) {
            String storeDir = storeDirOf(line);
            assertTrue(storeDir.startsWith("C:"), line + " gave " + storeDir);
            if (File.separatorChar == '\\') {
                assertEquals("C:\\temp\\x", storeDir);
            }
            else {
                assertEquals("C:/temp/x", storeDir);
            }
        }

        // an already escaped windows path parses back to the same place
        String escaped = storeDirOf("store_dir=C:\\\\temp\\\\x");
        assertTrue(escaped.startsWith("C:"), "escaped gave " + escaped);
        assertEquals(File.separatorChar == '\\' ? "C:\\temp\\x" : "C:/temp/x", escaped);

        // the point of cleanDir: a caller who escaped the dir themselves before handing
        // it over lands in the same place as one who did not. No double escaping either way.
        assertEquals(storeDirOf("store_dir=C:\\temp\\x"), storeDirOf("store_dir=C:\\\\temp\\\\x"));
        assertEquals(insertOf("store_dir=C:\\temp\\x"), insertOf("store_dir=C:\\\\temp\\\\x"));
        if (File.separatorChar == '\\') {
            assertEquals("  " + STORE_DIR + "=C:\\\\temp\\\\x", insertOf("store_dir=C:\\\\temp\\\\x"));
        }
    }

    private static String insertOf(String storeDirLine) throws IOException {
        JsConfig jsConfig = new JsConfig(Arrays.asList("jetstream {", "  " + storeDirLine, "}"));
        return jsConfig.configInserts.get(1);
    }

    @Test
    public void testStoreDirWithSpaceRejected() {
        // the conf parser ends an unquoted value at a space, so this can never start a
        // server. Rejected up front rather than as a parse error from nats-server.
        String dir = File.separatorChar == '\\' ? "C:\\my dir\\x" : "/tmp/my dir/x";

        assertThrows(IllegalArgumentException.class, () -> cleanDir(dir));
        assertThrows(IllegalArgumentException.class, () -> new JsConfig(Paths.get(dir)));
        assertThrows(IllegalArgumentException.class, () -> storeDirOf("store_dir=" + dir));

        // a space anywhere counts, including one the caller could not see coming
        assertThrows(IllegalArgumentException.class, () -> cleanDir("/tmp/trailing "));
        assertThrows(IllegalArgumentException.class, () -> cleanDir(" /tmp/leading"));

        // and the message names the offending path
        IllegalArgumentException e =
            assertThrows(IllegalArgumentException.class, () -> cleanDir(dir));
        assertTrue(e.getMessage().contains(dir), "message was " + e.getMessage());

        // no space, no complaint
        assertDoesNotThrow(() -> cleanDir(File.separatorChar == '\\' ? "C:\\nospace\\x" : "/tmp/nospace/x"));
    }

    @Test
    public void testInStoreDirIsUntouched() throws IOException {
        // nothing was supplied, so there is nothing to reference
        assertNull(new JsConfig().inStoreDir);
        assertNull(new JsConfig(Arrays.asList("jetstream {", "}")).inStoreDir);

        // supplied as a path
        String dir = File.separatorChar == '\\' ? "C:\\temp\\x" : "/tmp/x";
        assertEquals(dir, new JsConfig(Paths.get(dir)).inStoreDir);

        // supplied inside a block, kept exactly as written even where cleaning changes it
        String raw = File.separatorChar == '\\' ? "C:\\\\temp\\\\x" : "/tmp/a\\b";
        JsConfig jsConfig = new JsConfig(Arrays.asList("jetstream {", "  store_dir=" + raw, "}"));
        assertEquals(raw, jsConfig.inStoreDir);
        assertNotEquals(jsConfig.inStoreDir, jsConfig.storeDir);
        assertEquals(cleanDir(raw), jsConfig.storeDir);
    }

    private static String storeDirOf(String storeDirLine) throws IOException {
        return new JsConfig(Arrays.asList("jetstream {", "  " + storeDirLine, "}")).storeDir;
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
        validateStoreDir(jsConfig, jsConfig.configInserts.get(1));
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
        validateStoreDir(jsConfig, jsConfig.configInserts.get(4));
        assertEquals("}", jsConfig.configInserts.get(5));
        if (expectTestStoreDir) {
            assertEquals(TEST_STORE_DIR, jsConfig.storeDir);
            assertTrue(jsConfig.configInserts.get(4).contains(TEST_STORE_DIR));
        }
    }

    private static void validateStoreDir(JsConfig jsConfig, String insert) {
        // storeDir is the real path, fixedDir is what the conf file gets
        assertEquals(fixDir(jsConfig.storeDir), jsConfig.fixedDir);
        assertEquals(cleanDir(jsConfig.storeDir), jsConfig.storeDir);
        assertEquals("  " + STORE_DIR + "=" + jsConfig.fixedDir, insert);
    }

    private static String colonize(String jsString) {
        return jsString.replace("jetstream", "jetstream:");
    }
    private static JsConfig toConfig(String testString) throws IOException {
        return new JsConfig(Arrays.asList(testString.split("\\n")));
    }
}