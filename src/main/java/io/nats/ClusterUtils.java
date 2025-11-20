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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public abstract class ClusterUtils {
    private static String DEFAULT_CLUSTER_HOST = "127.0.0.1";
    private static int DEFAULT_CLUSTER_PORT_START = 4220;
    private static int DEFAULT_CLUSTER_LISTEN_START = 4230;
    private static int DEFAULT_CLUSTER_MONITOR_START = 4280;

    private static int DEFAULT_CLUSTER_COUNT = 3;
    private static String DEFAULT_CLUSTER_NAME = "cluster";
    private static String DEFAULT_SERVER_NAME_PREFIX = "server";

    public static void setDefaultClusterHost(String defaultHost) {
        DEFAULT_CLUSTER_HOST = defaultHost;
    }

    public static void setDefaultClusterPortStart(int defaultPortStart) {
        DEFAULT_CLUSTER_PORT_START = defaultPortStart;
    }

    public static void setDefaultClusterListenStart(int defaultListenStart) {
        DEFAULT_CLUSTER_LISTEN_START = defaultListenStart;
    }

    public static void setDefaultClusterMonitorStart(int defaultMonitorStart) {
        DEFAULT_CLUSTER_MONITOR_START = defaultMonitorStart;
    }

    public static void setDefaultClusterCount(int defaultClusterCount) {
        DEFAULT_CLUSTER_COUNT = defaultClusterCount;
    }

    public static void setDefaultClusterName(String defaultClusterName) {
        DEFAULT_CLUSTER_NAME = defaultClusterName;
    }

    public static void setDefaultServerNamePrefix(String defaultServerNamePrefix) {
        DEFAULT_SERVER_NAME_PREFIX = defaultServerNamePrefix;
    }

    public static String getDefaultClusterHost() {
        return DEFAULT_CLUSTER_HOST;
    }

    public static int getDefaultClusterPortStart() {
        return DEFAULT_CLUSTER_PORT_START;
    }

    public static int getDefaultClusterListenStart() {
        return DEFAULT_CLUSTER_LISTEN_START;
    }

    public static int getDefaultClusterMonitorStart() {
        return DEFAULT_CLUSTER_MONITOR_START;
    }

    public static int getDefaultClusterCount() {
        return DEFAULT_CLUSTER_COUNT;
    }

    public static String getDefaultClusterName() {
        return DEFAULT_CLUSTER_NAME;
    }

    public static String getDefaultServerNamePrefix() {
        return DEFAULT_SERVER_NAME_PREFIX;
    }

    private ClusterUtils() {}

    public static List<ClusterInsert> createClusterInserts() {
        return createClusterInserts(DEFAULT_CLUSTER_COUNT, DEFAULT_CLUSTER_NAME, DEFAULT_SERVER_NAME_PREFIX, false, null);
    }

    public static List<ClusterInsert> createClusterInserts(Path jsStoreDirBase) {
        return createClusterInserts(DEFAULT_CLUSTER_COUNT, DEFAULT_CLUSTER_NAME, DEFAULT_SERVER_NAME_PREFIX, false, jsStoreDirBase);
    }

    public static List<ClusterInsert> createClusterInserts(int count) {
        return createClusterInserts(count, DEFAULT_CLUSTER_NAME, DEFAULT_SERVER_NAME_PREFIX, false, null);
    }

    public static List<ClusterInsert> createClusterInserts(int count, Path jsStoreDirBase) {
        return createClusterInserts(count, DEFAULT_CLUSTER_NAME, DEFAULT_SERVER_NAME_PREFIX, false, jsStoreDirBase);
    }

    public static List<ClusterInsert> createClusterInserts(int count, String clusterName, String serverNamePrefix) {
        return createClusterInserts(createNodes(count, clusterName, serverNamePrefix, false, null));
    }

    public static List<ClusterInsert> createClusterInserts(int count, String clusterName, String serverNamePrefix, Path jsStoreDirBase) {
        return createClusterInserts(createNodes(count, clusterName, serverNamePrefix, false, jsStoreDirBase));
    }

    public static List<ClusterInsert> createClusterInserts(int count, String clusterName, String serverNamePrefix, boolean monitor) {
        return createClusterInserts(createNodes(count, clusterName, serverNamePrefix, monitor, null));
    }

    public static List<ClusterInsert> createClusterInserts(int count, String clusterName, String serverNamePrefix, boolean monitor, Path jsStoreDirBase) {
        return createClusterInserts(createNodes(count, clusterName, serverNamePrefix, monitor, jsStoreDirBase));
    }

    public static List<ClusterNode> createNodes(int count, String clusterName, String serverNamePrefix, boolean monitor, Path jsStoreDirBase) {
        return createNodes(count, clusterName, serverNamePrefix, jsStoreDirBase,
            DEFAULT_CLUSTER_HOST, DEFAULT_CLUSTER_PORT_START, DEFAULT_CLUSTER_LISTEN_START,
            monitor ? DEFAULT_CLUSTER_MONITOR_START : null);
    }

    public static List<ClusterNode> createNodes(int count, String clusterName, String serverNamePrefix, Path jsStoreDirBase,
                                                String host, int portStart, int listenStart, Integer monitorStart) {
        List<ClusterNode> nodes = new ArrayList<>();
        for (int x = 0; x < count; x++) {
            int port = portStart + x;
            int listen = listenStart + x;
            Integer monitor = monitorStart == null ? null : monitorStart + x;
            Path jsStoreDir = jsStoreDirBase == null ? null : Paths.get(jsStoreDirBase.toString(), "" + port);
            nodes.add( new ClusterNode(clusterName, serverNamePrefix + x, host, port, listen, monitor, jsStoreDir));
        }
        return nodes;
    }

    public static Path createTemporaryJetStreamStoreDirBase() throws IOException {
        return Files.createTempDirectory(null);
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
                String dir = node.jsStoreDir.toString();
                if (File.separatorChar == '\\') {
                    dir = dir.replace("\\", "\\\\").replace("/", "\\\\");
                }
                else {
                    dir = dir.replace("\\", "/");
                }
                lines.add("jetstream {");
                lines.add("    store_dir=" + dir);
                lines.add("}");
            }
            lines.add("server_name=" + node.serverName);
            lines.add("cluster {");
            lines.add("  name: " + node.clusterName);
            String host = node.host == null ? DEFAULT_CLUSTER_HOST : node.host;
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
