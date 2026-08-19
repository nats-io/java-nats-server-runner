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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * An object representing the JetStream Storage dir
 */
public class JsConfig {
    public static final String STORE_DIR = "store_dir";
    public static final String INDENT = "  ";

    public final String storeDir;
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

    // Files.createTempDirectory(null).toString()

    private JsConfig(List<String> inLines, @Nullable String inStoreDir) throws IOException {

        String storeDir = "";

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
                throw new IllegalArgumentException("JsConfig[1] Input not recognized as jetstream block");
            }

            if (!s.endsWith("enabled")) {
                s = s.substring(9); // skip past jetstream
                // it must then start with '{' or ':{'
                // it must end with }
                if ((!s.startsWith("{") || !s.startsWith(":{")) && !s.endsWith("}")) {
                    throw new IllegalArgumentException("JsConfig[2] Input not recognized as jetstream block");
                }

                // skip past { and don't include end }
                int at = s.indexOf("{");
                s = s.substring(at + 1, s.length() - 1);
                String[] split = s.split("↩");
                for (String ss : split) {
                    String config = ss.trim();
                    if (!config.isEmpty()) {
                        if (config.contains(STORE_DIR)) {
                            at = config.indexOf(":");
                            if (at == -1) {
                                at = config.indexOf("=");
                                if (at == -1) {
                                    at = config.indexOf(" ");
                                }
                            }
                            if (at != -1) {
                                storeDir = config.substring(at + 1).trim();
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

        if (storeDir.isEmpty()) {
            storeDir = inStoreDir == null
                ? fixDir(Files.createTempDirectory(null).toString())
                : inStoreDir;
        }
        this.storeDir = storeDir;

        configInserts.add(INDENT + STORE_DIR + "=" + this.storeDir);
        configInserts.add("}");
    }

    private static String fixDir(String dir) {
        if (File.separatorChar == '\\') {
            return dir.replace("\\", "\\\\").replace("/", "\\\\");
        }
        return dir.replace("\\", "/");
    }

}
