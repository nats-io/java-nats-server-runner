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
    public static final String INDENT = "    ";

    public final String storeDir;
    public final List<String> configInserts;

    public JsConfig(Path dirPath) {
        this(dirPath.toString(), null);
    }

    public JsConfig(String dir) {
        this(dir, null);
    }

    public JsConfig() throws IOException {
        this(Files.createTempDirectory(null).toString(), null);
    }

    public JsConfig(List<String> lines) throws IOException {
        this(Files.createTempDirectory(null).toString(), lines);
    }

    private JsConfig(String inputDir, List<String> inputLines)  {
        this.storeDir = STORE_DIR + "=" + fixDir(inputDir);

        configInserts = new ArrayList<>();
        configInserts.add("jetstream {");
        configInserts.add(INDENT + this.storeDir);

        List<String> lines = null;
        boolean parse = true;
        if (inputLines == null || inputLines.isEmpty()) {
            parse = false;
        }
        else {
            lines = new ArrayList<>();
            for (String inputLine : inputLines) {
                if (inputLine.trim().length() > 0) {
                    lines.add(inputLine);
                }
            }
            if (lines.size() == 1) {
                String noSpace = lines.get(0).trim().replaceAll(" ", "");
                if (noSpace.equals("jetstream{}")) {
                    parse = false;
                }
                else if (noSpace.startsWith("jetstream{")) {
                    if (!noSpace.endsWith("}")) {
                        throw new IllegalArgumentException("Input not recognized: " + lines.get(0));
                    }
                }
                else if (noSpace.equals("jetstream:enabled")) {
                    parse = false;
                }
                else {
                    throw new IllegalArgumentException("Input not recognized: " + lines.get(0));
                }
            }
            else if (lines.size() == 2) {
                String jetstream = lines.get(0).trim().replaceAll(" ", "");
                String close = lines.get(1).trim().replaceAll(" ", "");
                if (!jetstream.equals("jetstream{") || !close.equals("}")) {
                    throw new IllegalArgumentException("Input not recognized: " + lines.get(0) + " | " + lines.get(1));
                }
                parse = false;
            }
        }

        if (parse) {
            // combine to make it easier to split apart
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                sb.append(line.trim());
            }
            // remove jetstream {
            int at = sb.indexOf("{");
            if (at != -1) {
                sb.delete(0, at + 1);
            }
            at = sb.indexOf(",");
            while (at != -1) {
                String config = sb.substring(0, at).trim();
                if (!config.contains(STORE_DIR)) {
                    configInserts.add(INDENT + config);
                }
                sb.delete(0, at + 1);
                at = sb.indexOf(",");
            }

            at = sb.indexOf("}");
            String config = sb.substring(0, at).trim();
            if (!config.contains(STORE_DIR)) {
                configInserts.add(INDENT + config);
            }
            for (int i = 1; i < configInserts.size() - 1; i++) {
                configInserts.add(i, configInserts.remove(i) + ",");
            }
        }

        configInserts.add("}");
    }

    private static String fixDir(String dir) {
        if (File.separatorChar == '\\') {
            return dir.replace("\\", "\\\\").replace("/", "\\\\");
        }
        return dir.replace("\\", "/");
    }

}
