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
public class JsStorageDir {
    public final String jsStoreDir;
    public final List<String> configInserts ;

    public static JsStorageDir temporaryInstance() throws IOException {
        return new JsStorageDir(Files.createTempDirectory(null).toString(), false);
    }

    public static JsStorageDir extractedInstance(String extracted) throws IOException {
        int at = extracted.indexOf("=");
        if (at == -1) {
            return new JsStorageDir(extracted.trim(), true);
        }
        return new JsStorageDir(extracted.substring(at + 1).trim(), true);
    }

    public JsStorageDir(Path dirPath) {
        this(dirPath.toString(), false);
    }

    public JsStorageDir(String dir) {
        this(dir, false);
    }

    private JsStorageDir(String dir, boolean dirWasExtractedFromConfig) {

        jsStoreDir = dir;
        String fixedDir;
        if (dirWasExtractedFromConfig) {
            fixedDir = dir;
        }
        else if (File.separatorChar == '\\') {
            fixedDir = dir.replace("\\", "\\\\").replace("/", "\\\\");
        }
        else {
            fixedDir = dir.replace("\\", "/");
        }
        configInserts = new ArrayList<>();
        configInserts.add("jetstream {");
        configInserts.add("    store_dir=" + fixedDir);
        configInserts.add("}");
    }

}
