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

public enum DebugLevel {
    DEBUG("-D"),                 // Enable debugging output
    TRACE("-V"),                 // Trace the raw protocol
    VERBOSE_TRACE("-VV"),        // Verbose trace (traces system account as well)
    DEBUG_TRACE("-DV"),          // Debug and trace
    DEBUG_VERBOSE_TRACE("-DVV"); // Debug and verbose trace (traces system account as well)

    private final String cmdOption;

    DebugLevel(String cmdOption) {
        this.cmdOption = cmdOption;
    }

    public String getCmdOption() {
        return cmdOption;
    }
}
