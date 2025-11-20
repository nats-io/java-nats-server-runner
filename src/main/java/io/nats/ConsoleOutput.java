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

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

public class ConsoleOutput implements Output {
    private Level level = INFO;

    protected String format(Level targetLevel, String msg) {
        return targetLevel + ": " + msg;
    }

    @Override
    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public void error(Supplier<String> msgSupplier) {
        if (shouldShow(SEVERE)) {
            System.err.println(format(SEVERE, msgSupplier.get()));
        }
    }

    @Override
    public void error(String msg) {
        if (shouldShow(SEVERE)) {
            System.err.println(format(SEVERE, msg));
        }
    }

    @Override
    public void warning(Supplier<String> msgSupplier) {
        if (shouldShow(WARNING)) {
            System.out.println(format(WARNING, msgSupplier.get()));
        }
    }

    @Override
    public void warning(String msg) {
        if (shouldShow(WARNING)) {
            System.out.println(format(WARNING, msg));
        }
    }

    @Override
    public void info(Supplier<String> msgSupplier) {
        if (shouldShow(INFO)) {
            System.out.println(format(INFO, msgSupplier.get()));
        }
    }

    @Override
    public void info(String msg) {
        if (shouldShow(Level.INFO)) {
            System.out.println(format(INFO, msg));
        }
    }

    @Override
    public boolean isConsole() {
        return true;
    }

    @Override
    public boolean isLogger() {
        return false;
    }

    @Override
    public Logger getLogger() {
        return null;
    }

    protected boolean shouldShow(Level testLevel) {
        return level.intValue() <= testLevel.intValue() && testLevel != OFF;
    }
}
