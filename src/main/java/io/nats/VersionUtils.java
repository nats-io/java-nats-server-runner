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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static io.nats.NatsRunnerUtils.getResolvedServerPath;

public abstract class VersionUtils {
    public static final String VERSION_OPTION = "--version";

    private VersionUtils() {}

    /**
     * Get the version string from the nats server i.e. nats-server: v2.2.2
     * Using the server resolved by {@link NatsRunnerUtils#getResolvedServerPath}
     * @return the version string
     */
    public static String getNatsServerVersionString() {
        return getNatsServerVersionString(getResolvedServerPath());
    }


    /**
     * Get the version string from the nats server i.e. nats-server: v2.2.2
     * @param serverPath the specific server path to check
     * @return the version string
     */
    public static String getNatsServerVersionString(String serverPath) {
        ArrayList<String> cmd = new ArrayList<>();

        // order of precedence is environment, value set statically, default

        cmd.add(serverPath);
        cmd.add(VERSION_OPTION);

        try {
            //noinspection ExtractMethodRecommender
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process process = pb.start();
            if (0 != process.waitFor()) {
                throw new IllegalStateException(String.format("Process %s failed", pb.command()));
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            ArrayList<String> lines = new ArrayList<>();
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }

            //noinspection SizeReplaceableByIsEmpty
            if (lines.size() > 0) {
                return lines.get(0);
            }

            return null;
        }
        catch (Exception exp) {
            return null;
        }
    }
}
