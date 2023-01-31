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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.logging.Level;

public class DisplayLevelTest extends TestBase {

    // RUN THIS TEST MANUALLY TO SEE THE PROPER SERVER OUTPUT
    @Test
    public void testDisplayOutLevel() throws IOException, InterruptedException {
        System.out.println("------------------------------------------------------------------------------");
        System.out.println("THIS SHOULD SHOW EVERYTHING. Level:ALL (Default), No Error.");
        System.out.println("------------------------------------------------------------------------------");
        try (NatsServerRunner runner = NatsServerRunner.builder()
            .build())
        {
            connect(runner);
        }
        catch (Exception ignore) {}

        System.out.println("------------------------------------------------------------------------------");
        System.out.println("THIS SHOULD ALSO SHOW EVERYTHING. Level:INFO, No Error.");
        System.out.println("------------------------------------------------------------------------------");
        try (NatsServerRunner runner = NatsServerRunner.builder()
            .displayOutLevel(Level.INFO)
            .build())
        {
            connect(runner);
        }
        catch (Exception ignore) {}

        Thread.sleep(100);
        System.out.println("------------------------------------------------------------------------------");
        System.out.println("THIS SHOULD SHOW THE ERROR ONLY. Level:SEVERE, Yes Error.");
        System.out.println("------------------------------------------------------------------------------");
        try (NatsServerRunner runner = NatsServerRunner.builder()
            .configFilePath("not-a-real-path")
            .displayOutLevel(Level.SEVERE)
            .build())
        {
            connect(runner);
        }
        catch (Exception ignore) {}

        Thread.sleep(100);
        System.out.println("------------------------------------------------------------------------------");
        System.out.println("THIS SHOULD ALSO SHOW THE ERROR ONLY. Level:INFO, Yes Error.");
        System.out.println("------------------------------------------------------------------------------");
        try (NatsServerRunner runner = NatsServerRunner.builder()
            .configFilePath("not-a-real-path")
            .displayOutLevel(Level.INFO)
            .build())
        {
            connect(runner);
        }
        catch (Exception ignore) {}

        Thread.sleep(100);
        System.out.println("------------------------------------------------------------------------------");
        System.out.println("THIS SHOULD SHOW NOTHING. Level:OFF, Yes Error.");
        System.out.println("------------------------------------------------------------------------------");
        try (NatsServerRunner runner = NatsServerRunner.builder()
            .configFilePath("not-a-real-path")
            .displayOutLevel(Level.OFF)
            .build())
        {
            connect(runner);
        }
        catch (Exception ignore) {}

        Thread.sleep(100);
        System.out.println("------------------------------------------------------------------------------");
        System.out.println("THIS SHOULD ALSO SHOW NOTHING. Level:SEVERE, No Error.");
        System.out.println("------------------------------------------------------------------------------");
        try (NatsServerRunner runner = NatsServerRunner.builder()
            .displayOutLevel(Level.SEVERE)
            .build())
        {
            connect(runner);
        }
        catch (Exception ignore) {}

        System.out.println("------------------------------------------------------------------------------");
    }
}
