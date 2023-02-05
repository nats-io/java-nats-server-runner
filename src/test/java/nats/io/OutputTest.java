// Copyright 2023 The NATS Authors
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

import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

public class OutputTest extends TestBase {

    @Test
    public void testOutputImplementationsForCoverage() throws Exception {
        coverOutput(new ConsoleOutput());
        coverOutput(new LoggingOutput(Logger.getLogger("OutputTest")));
    }

    private void coverOutput(Output o) {
        Level[] levels = new Level[] {Level.OFF, Level.INFO, Level.WARNING, Level.SEVERE};
        for (Level l : levels) {
            o.setLevel(l);
            o.error("error, string, " + l);
            o.warning("warning, string, " + l);
            o.info("info, string,  " + l);
            o.error(() -> "error, supplier, " + l);
            o.warning(() -> "warning, supplier, " + l);
            o.info(() -> "info, supplier, " + l);
        }
    }
}
