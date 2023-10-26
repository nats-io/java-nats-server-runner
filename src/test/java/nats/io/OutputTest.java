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

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.*;
import static org.junit.jupiter.api.Assertions.*;

public class OutputTest extends TestBase {
    private static final Level[] COVERAGE_LEVELS = new Level[] {INFO, WARNING, SEVERE};
    private static final Level[] SHOULD_SHOW_LEVELS = new Level[] {OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL};

    @Test
    public void testConsoleOutputShouldShow() throws Exception {
        TestShouldShow tss = new TestShouldShow();

        for (int x = 0; x < SHOULD_SHOW_LEVELS.length; x++) {
            for (int y = x + 1; y < SHOULD_SHOW_LEVELS.length; y++) {
                tss.setLevel(SHOULD_SHOW_LEVELS[x]);
                assertFalse(tss.shouldShow(SHOULD_SHOW_LEVELS[y]));
                tss.setLevel(SHOULD_SHOW_LEVELS[y]);
                if (x == 0) {
                    assertFalse(tss.shouldShow(SHOULD_SHOW_LEVELS[x]));
                }
                else {
                    assertTrue(tss.shouldShow(SHOULD_SHOW_LEVELS[x]));
                }
            }
        }
    }

    static class TestShouldShow extends ConsoleOutput {
        @Override
        public boolean shouldShow(Level testLevel) {
            return super.shouldShow(testLevel);
        }
    }

    @Test
    public void testOutputImplementations() throws Exception {
        coverOutput(new ConsoleOutput(), false);
        coverOutput(new LoggingOutput(Logger.getLogger("OutputTest")), true);

        OutputTestOutput oto = new OutputTestOutput();
        coverOutput(oto, false);
        assertEquals(COVERAGE_LEVELS.length, oto.setLevelCount);
        assertEquals(COVERAGE_LEVELS.length, oto.errorCount);
        assertEquals(COVERAGE_LEVELS.length, oto.errorCountS);
        assertEquals(COVERAGE_LEVELS.length, oto.warningCount);
        assertEquals(COVERAGE_LEVELS.length, oto.warningCountS);
        assertEquals(COVERAGE_LEVELS.length, oto.infoCount);
        assertEquals(COVERAGE_LEVELS.length, oto.infoCountS);
    }

    private void coverOutput(Output o, boolean isLogger) {
        for (Level l : COVERAGE_LEVELS) {
            o.setLevel(l);
            o.error("error, string, " + l);
            o.warning("warning, string, " + l);
            o.info("info, string,  " + l);
            o.error(() -> "error, supplier, " + l);
            o.warning(() -> "warning, supplier, " + l);
            o.info(() -> "info, supplier, " + l);
            if (isLogger) {
                assertTrue(o.isLogger());
                assertNotNull(o.getLogger());
            }
            else {
                assertFalse(o.isLogger());
            }
        }
    }

    static class OutputTestOutput implements Output {
        public int setLevelCount = 0;
        public int errorCount = 0;
        public int errorCountS = 0;
        public int warningCount = 0;
        public int warningCountS = 0;
        public int infoCount = 0;
        public int infoCountS = 0;

        @Override
        public void setLevel(Level level) {
            setLevelCount++;
        }

        @Override
        public void error(Supplier<String> msgSupplier) {
            errorCountS++;
        }

        @Override
        public void error(String msg) {
            errorCount++;
        }

        @Override
        public void warning(Supplier<String> msgSupplier) {
            warningCountS++;
        }

        @Override
        public void warning(String msg) {
            warningCount++;
        }

        @Override
        public void info(Supplier<String> msgSupplier) {
            infoCountS++;
        }

        @Override
        public void info(String msg) {
            infoCount++;
        }

        @Override
        public boolean isLogger() {
            return false;
        }

        @Override
        public Logger getLogger() {
            return null;
        }
    }
}
