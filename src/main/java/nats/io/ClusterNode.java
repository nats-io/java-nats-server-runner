// Copyright 2020-2023 The NATS Authors
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

package nats.io;

import java.nio.file.Path;

public class ClusterNode {
    public final String clusterName;
    public final String serverName;
    public final int port;
    public final int listen;
    public final String host;
    public final Integer monitor;
    public final Path jsStoreDir;

    public ClusterNode(String clusterName, String serverName, int port, int listen) {
        this(clusterName, serverName, null, port, listen, null, null);
    }

    public ClusterNode(String clusterName, String serverName, int port, int listen, Integer monitor) {
        this(clusterName, serverName, null, port, listen, monitor, null);
    }

    public ClusterNode(String clusterName, String serverName, int port, int listen, Path jsStoreDir) {
        this(clusterName, serverName, null, port, listen, null, jsStoreDir);
    }

    public ClusterNode(String clusterName, String serverName, String host, int port, int listen, Integer monitor, Path jsStoreDir) {
        this.clusterName = clusterName;
        this.serverName = serverName;
        this.host = host;
        this.port = port;
        this.listen = listen;
        this.monitor = monitor;
        this.jsStoreDir = jsStoreDir;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String clusterName;
        private String serverName;
        private int port;
        private int listen;
        private String host;
        private Integer monitor;
        private Path jsStoreDir;

        public Builder clusterName(String clusterName) {
            this.clusterName = clusterName;
            return this;
        }

        public Builder serverName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder listen(int listen) {
            this.listen = listen;
            return this;
        }

        public Builder monitor(Integer monitor) {
            this.monitor = monitor;
            return this;
        }

        public Builder jsStoreDir(Path jsStoreDir) {
            this.jsStoreDir = jsStoreDir;
            return this;
        }

        public ClusterNode build() {
            return new ClusterNode(clusterName, serverName, host, port, listen, monitor, jsStoreDir);
        }
    }
}
