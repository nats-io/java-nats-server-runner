// Copyright 2022 The NATS Authors
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

import java.util.List;

class ClusterInsert {
    public int id;
    public int port;
    public int listen;
    public String[] configInserts;

    public void setInsert(List<String> configInsertsList) {
        int lines = configInsertsList.size();
        this.configInserts = new String[lines];
        for (int x = 0; x < lines; x++) {
            configInserts[x] = configInsertsList.get(x);
        }
    }

    @Override
    public String toString() {
        return "ClusterInsert" +
            "\n  id=" + id +
            "\n  port=" + port +
            "\n  listen=" + listen +
            "\n  configInserts:" + insertsString();
    }

    private String insertsString() {
        StringBuilder sb = new StringBuilder();
        for (String s : configInserts) {
            sb.append("\n    ");
            sb.append(s);
        }
        return sb.toString();
    }
}
