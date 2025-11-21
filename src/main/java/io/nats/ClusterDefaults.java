// Copyright 2025 The NATS Authors
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

public class ClusterDefaults {
    private String host;
    private int portStart;
    private int listenStart;
    private boolean hasMonitor;
    private int monitorStart;

    private int count;
    private String clusterName;
    private String serverNamePrefix;

    @Override
    public String toString() {
        return "ClusterDefaults{" +
            "h='" + host + '\'' +
            ", p=" + portStart +
            ", l=" + listenStart +
            ", mB=" + hasMonitor +
            ", mS=" + monitorStart +
            ", c=" + count +
            ", n='" + clusterName + '\'' +
            ", s='" + serverNamePrefix + '\'' +
            '}';
    }

    public ClusterDefaults() {
        host = NatsRunnerUtils.getDefaultLocalhostHost().host;
        portStart = 4220;
        listenStart = 4230;
        hasMonitor = false;
        monitorStart = 4280;

        count = 3;
        clusterName = "cluster";
        serverNamePrefix = "server";
    }

    public ClusterDefaults host(String host) {
        this.host = host;
        return this;
    }

    public ClusterDefaults portStart(int portStart) {
        this.portStart = portStart;
        return this;
    }

    public ClusterDefaults listenStart(int listenStart) {
        this.listenStart = listenStart;
        return this;
    }

    public ClusterDefaults monitor(boolean monitor) {
        this.hasMonitor = monitor;
        return this;
    }

    public ClusterDefaults monitorStart(int monitorStart) {
        if (monitorStart < 1) {
            hasMonitor = false;
        }
        else {
            this.monitorStart = monitorStart;
            hasMonitor = true;
        }
        return this;
    }

    public ClusterDefaults count(int count) {
        this.count = count;
        return this;
    }

    public ClusterDefaults clusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }

    public ClusterDefaults serverNamePrefix(String serverNamePrefix) {
        this.serverNamePrefix = serverNamePrefix;
        return this;
    }

    public String getHost() {
        return host;
    }

    public int getPortStart() {
        return portStart;
    }

    public int getListenStart() {
        return listenStart;
    }

    public boolean hasMonitor() {
        return hasMonitor;
    }

    public int getMonitorStart() {
        return monitorStart;
    }

    public int getCount() {
        return count;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getServerNamePrefix() {
        return serverNamePrefix;
    }
}
