// Copyright 2020-2025 The NATS Authors
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

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.nats.NatsRunnerUtils.cleanDir;
import static io.nats.NatsRunnerUtils.fixDir;

/**
 * An object representing the JetStream Storage dir
 */
public class JsConfig {
    public static final String STORE_DIR = "store_dir";
    public static final String INDENT = "  ";

    public final String storeDir;
    public final String fixedDir;

    /**
     * The store dir exactly as it was supplied, before any cleaning, or null if none was.
     * Null for the no argument constructor, whose directory this object chose itself, and
     * for a JetStream block that carried no store_dir. Reference only - use storeDir for
     * the directory that will actually be used and fixedDir for the config file.
     */
    @Nullable public final String inStoreDir;

    public final List<String> configInserts;

    public JsConfig() throws IOException {
        this(null, null);
    }

    public JsConfig(Path dirPath) throws IOException {
        this(null, dirPath.toString());
    }

    public JsConfig(List<String> lines) throws IOException {
        this(lines, null);
    }

    private JsConfig(List<String> inLines, @Nullable String inStoreDir) throws IOException {

        String tempStoreDir = "";

        configInserts = new ArrayList<>();
        configInserts.add("jetstream {");

        if (inLines != null && !inLines.isEmpty()) {
            StringBuilder sbx = new StringBuilder();
            for (String line : inLines) {
                String trim = line.trim();
                String[] split = trim.split(",");
                for (String s : split) {
                    sbx.append(s).append("↩");
                }
            }
            String s = sbx.substring(0, sbx.length() - 1);
            if (!s.startsWith("jetstream")) {
                throw new IllegalArgumentException("JsConfig[1] Input not recognized as JetStream block");
            }

            if (!s.endsWith("enabled")) {
                s = s.substring(9); // skip past jetstream
                // it must then start with '{' or ':{'
                // it must end with }
                if ((!s.startsWith("{") || !s.startsWith(":{")) && !s.endsWith("}")) {
                    throw new IllegalArgumentException("JsConfig[2] Input not recognized as JetStream block");
                }

                // skip past { and don't include end }
                int at = s.indexOf("{");
                s = s.substring(at + 1, s.length() - 1);
                String[] split = s.split("↩");
                for (String ss : split) {
                    String config = ss.trim();
                    if (!config.isEmpty()) {
                        if (config.contains(STORE_DIR)) {
                            // the separator is the first :, = or space after the key.
                            // Searching the whole line finds the colon in a windows
                            // drive letter instead and eats "store_dir=C".
                            at = -1;
                            for (int x = config.indexOf(STORE_DIR) + STORE_DIR.length(); x < config.length(); x++) {
                                char c = config.charAt(x);
                                if (c == ':' || c == '=' || c == ' ') {
                                    at = x;
                                    break;
                                }
                            }
                            if (at != -1) {
                                tempStoreDir = config.substring(at + 1).trim();
                            }
                        }
                        else {
                            configInserts.add(INDENT + config + ",");
                        }
                    }
                }
                int lastI = configInserts.size() - 1;
                if (lastI > 0) {
                    String last = configInserts.get(lastI);
                    last = last.substring(0, last.length() - 1); // remove the comma
                    configInserts.set(lastI, last);
                }
            }
        }

        // whatever the caller handed over, untouched, from either route. Not the
        // generated temp directory, which nobody supplied.
        this.inStoreDir = tempStoreDir.isEmpty() ? inStoreDir : tempStoreDir;

        if (tempStoreDir.isEmpty()) {
            // the generated dir goes through cleanDir as well, both so storeDir means the
            // same thing on every branch and so the space check covers a temp directory we
            // did not choose. On windows that is under C:\Users\<user name>\AppData.
            this.storeDir = cleanDir(inStoreDir == null
                ? Files.createTempDirectory(null).toString()
                : inStoreDir);
        }
        else {
            this.storeDir = cleanDir(tempStoreDir);
        }
        fixedDir = fixDir(this.storeDir);

        configInserts.add(INDENT + STORE_DIR + "=" + fixedDir);
        configInserts.add("}");
    }
}
