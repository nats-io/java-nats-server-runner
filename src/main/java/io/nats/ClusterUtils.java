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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public abstract class ClusterUtils {
    public static final ClusterDefaults DEFAULT_CLUSTER_DEFAULTS = new ClusterDefaults();

    private ClusterUtils() {
    }

    public static List<ClusterInsert> createClusterInserts() {
        return createClusterInserts(createNodes());
    }

    public static List<ClusterInsert> createClusterInserts(Path jsStoreDirBase) {
        return createClusterInserts(createNodes(jsStoreDirBase));
    }

    public static List<ClusterInsert> createClusterInserts(ClusterDefaults cd) {
        return createClusterInserts(createNodes(cd));
    }

    public static List<ClusterInsert> createClusterInserts(ClusterDefaults cd, Path jsStoreDirBase) {
        return createClusterInserts(createNodes(cd, jsStoreDirBase));
    }

    public static List<ClusterNode> createNodes() {
        return createNodes(DEFAULT_CLUSTER_DEFAULTS, null);
    }

    public static List<ClusterNode> createNodes(Path jsStoreDirBase) {
        return createNodes(DEFAULT_CLUSTER_DEFAULTS, jsStoreDirBase);
    }

    public static List<ClusterNode> createNodes(ClusterDefaults cd) {
        return createNodes(cd, null);
    }

    public static List<ClusterNode> createNodes(ClusterDefaults cd, Path jsStoreDirBase) {
        List<ClusterNode> nodes = new ArrayList<>();
        for (int x = 0; x < cd.getCount(); x++) {
            int port = cd.getPortStart() + x;
            int listen = cd.getListenStart() + x;
            String server = cd.getServerNamePrefix() + x;
            Integer monitor = cd.hasMonitor() ? cd.getMonitorStart() + x : null;
            Path jsStoreDir = jsStoreDirBase == null ? null : Paths.get(jsStoreDirBase.toString(), "" + port);
            nodes.add( new ClusterNode(cd.getClusterName(), server, cd.getHost(), port, listen, monitor, jsStoreDir));
        }
        return nodes;
    }

    public static List<ClusterInsert> createClusterInserts(List<ClusterNode> nodes) {
        List<ClusterInsert> inserts = new ArrayList<>();
        for (ClusterNode node : nodes) {
            List<String> lines = new ArrayList<>();
            lines.add("port: " + node.port);
            if (node.monitor != null) {
                lines.add("http: " + node.monitor);
            }
            if (node.jsStoreDir != null) {
                JsStorageDir jssd = new JsStorageDir(node.jsStoreDir);
                lines.addAll(jssd.configInserts);
            }
            lines.add("server_name=" + node.serverName);
            lines.add("cluster {");
            lines.add("  name: " + node.clusterName);
            String host = node.host == null ? DEFAULT_CLUSTER_DEFAULTS.getHost() : node.host;
            lines.add("  listen: " + host + ":" + node.listen);
            lines.add("  routes: [");
            for (ClusterNode routeNode : nodes) {
                if (!routeNode.serverName.equals(node.serverName)) {
                    lines.add("    nats-route://" + host + ":" + routeNode.listen);
                }
            }
            lines.add("  ]");
            lines.add("}");
            inserts.add(new ClusterInsert(node, lines.toArray(new String[0])));
        }
        return inserts;
    }
}
