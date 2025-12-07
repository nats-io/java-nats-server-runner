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

    public JsConfig() throws IOException {
        this(Files.createTempDirectory(null).toString(), null);
    }

    public JsConfig(Path dirPath) {
        this(dirPath.toString(), null);
    }

    public JsConfig(List<String> lines) throws IOException {
        this(Files.createTempDirectory(null).toString(), lines);
    }

    private JsConfig(String inputDir, List<String> inputLines)  {
        this.storeDir = STORE_DIR + "=" + fixDir(inputDir);

        configInserts = new ArrayList<>();
        configInserts.add("jetstream {");

        List<String> lines = null;
        if (inputLines != null && !inputLines.isEmpty()) {
            // combine and remove spaces to make it easier to parse
            StringBuilder sbx = new StringBuilder();
            for (String line : inputLines) {
                sbx.append(line.trim().replaceAll(" ", ""));
            }
            String s = sbx.toString();
            if (!s.startsWith("jetstream")) {
                throw new IllegalArgumentException("Input not recognized as jetstream block");
            }

            if (!s.endsWith("enabled")) {
                s = s.substring(9); // skip past jetstream
                // it must then start with '{' or ':{'
                // it must end with }
                if ((!s.startsWith("{") || !s.startsWith(":{")) && !s.endsWith("}")) {
                    throw new IllegalArgumentException("Input not recognized as jetstream block");
                }

                // skip past { and don't include end }
                int at = s.indexOf("{");
                s = s.substring(at + 1, s.length() - 1);
                String[] split = s.split(",");
                for (String config : split) {
                    if (!config.isEmpty() && !config.contains(STORE_DIR)) {
                        configInserts.add(INDENT + config + ",");
                    }
                }
            }
        }
        configInserts.add(INDENT + this.storeDir);
        configInserts.add("}");
    }

    private static String fixDir(String dir) {
        if (File.separatorChar == '\\') {
            return dir.replace("\\", "\\\\").replace("/", "\\\\");
        }
        return dir.replace("\\", "/");
    }

}
