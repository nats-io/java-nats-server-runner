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

import java.util.Arrays;
import java.util.List;

public class ClusterInsert {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClusterInsert that = (ClusterInsert) o;

        if (id != that.id) return false;
        if (port != that.port) return false;
        if (listen != that.listen) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(configInserts, that.configInserts);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + port;
        result = 31 * result + listen;
        result = 31 * result + Arrays.hashCode(configInserts);
        return result;
    }
}
