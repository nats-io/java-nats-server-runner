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

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

/**
 * An object representing a single server node
 */
@NullMarked
public class ClusterNode {
    /** The name of the cluster */
    public final String clusterName;
    /** The name of the server */
    public final String serverName;
    /** The port */
    public final int port;
    /** The listen port */
    public final int listen;
    /** The host */
    @Nullable public final String host;
    /** The monitor port, may be null */
    @Nullable public final Integer monitor;
    /** A custom path to use as the JetStream storage directory */
    @Nullable public final Path jsStoreDir;

    /**
     * Construct a ClusterNode
     * @param clusterName the cluster name
     * @param serverName the server name
     * @param port the port
     * @param listen the listen port
     */
    public ClusterNode(String clusterName, String serverName, int port, int listen) {
        this(clusterName, serverName, null, port, listen, null, null);
    }

    /**
     * Construct a ClusterNode
     * @param clusterName the cluster name
     * @param serverName the server name
     * @param port the port
     * @param listen the listen port
     */
    public ClusterNode(String clusterName, String serverName, int port, int listen, @Nullable Integer monitor) {
        this(clusterName, serverName, null, port, listen, monitor, null);
    }

    public ClusterNode(String clusterName, String serverName, int port, int listen, @Nullable Path jsStoreDir) {
        this(clusterName, serverName, null, port, listen, null, jsStoreDir);
    }

    public ClusterNode(String clusterName, String serverName, @Nullable String host, int port, int listen, @Nullable Integer monitor, @Nullable Path jsStoreDir) {
        this.clusterName = clusterName;
        this.serverName = serverName;
        this.host = host;
        this.port = port;
        this.listen = listen;
        this.monitor = monitor;
        this.jsStoreDir = jsStoreDir;
    }

    @Override
    public String toString() {
        return "ClusterNode{" +
            "clusterName='" + clusterName + '\'' +
            ", serverName='" + serverName + '\'' +
            ", port=" + port +
            ", listen=" + listen +
            ", host='" + host + '\'' +
            ", monitor=" + monitor +
            ", jsStoreDir=" + jsStoreDir +
            '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    @NullUnmarked
    public static class Builder {
        private String clusterName;
        private String serverName;
        private int port = -1;
        private int listen = -1;
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
