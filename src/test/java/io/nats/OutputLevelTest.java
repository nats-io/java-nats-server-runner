// Copyright 2022-2025 The NATS Authors
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

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OutputLevelTest extends TestBase {

    // RUN THESE TESTS MANUALLY TO SEE THE PROPER SERVER OUTPUT
    @Test
    public void testOutputLevelInConsole() {
        _test("Console", ConsoleOutput::new);
    }

    @Test
    public void testOutputLevelInLogger() {
        _test("Logger", () -> new LoggingOutput(Logger.getLogger(OutputLevelTest.class.getName())));
    }

    private void _test(String label, Supplier<Output> supplier) {
        NatsServerRunner.setDefaultOutputSupplier(supplier);
        System.out.println("\n==============================================================================");
        System.out.println("Test " + label);
        _test(1, "Info",    false, Level.INFO);
        _test(2, "Nothing", false, Level.SEVERE);
        _test(3, "Nothing", false, Level.OFF);
        _test(4, "Severe",   true,  Level.INFO);
        _test(5, "Severe",   true,  Level.SEVERE);
        _test(6, "Nothing", true,  Level.OFF);
        System.out.println("------------------------------------------------------------------------------");
    }

    private void _test(int id, String show, boolean makeError, Level targetOutputLevel) {
        try { Thread.sleep(100); } catch(Exception e) { /* ignored */ }
        String ol = "Level." + targetOutputLevel;
        String me = makeError ? "Yes Error" : "No Error";
        System.out.println("------------------------------------------------------------------------------");
        System.out.println(id + ". Show " + show + " | " + ol + " | " + me);
        System.out.println("------------------------------------------------------------------------------");

        NatsServerRunner.Builder builder = NatsServerRunner.builder();
        if (targetOutputLevel != null) {
            builder.outputLevel = targetOutputLevel;
        }
        if (makeError) {
            builder.configFilePath("not-a-real-path");
        }

        try (NatsServerRunner runner = builder.build()) {
            connect(runner);
        }
        catch (Exception ignore) {}
    }
}
