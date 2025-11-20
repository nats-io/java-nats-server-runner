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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Object that represents the lines needed to add to the conf for a custer instance
 */
public class ClusterInsert {
    /**
     * The node
     */
    public final ClusterNode node;

    /**
     * The actual inserts
     */
    public final String[] configInserts;

    /**
     * Construct a ClusterInsert
     * @param node the node
     * @param configInserts the inserts
     */
    public ClusterInsert(@NonNull ClusterNode node, String @Nullable [] configInserts) {
        this.node = node;
        this.configInserts = configInserts == null || configInserts.length == 0 ? null : configInserts;
    }

    @Override
    public String toString() {
        if (configInserts == null) {
            return node.toString();
        }
        StringBuilder sb = new StringBuilder();
        for (String s : configInserts) {
            sb.append(s).append("\r\n");
        }
        return sb.toString();
    }
}
