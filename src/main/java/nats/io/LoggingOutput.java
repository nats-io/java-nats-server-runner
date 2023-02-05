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

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingOutput implements Output{
    private final Logger logger;

    public LoggingOutput(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void setLevel(Level level) {
        logger.setLevel(level);
    }

    @Override
    public void error(Supplier<String> msgSupplier) {
        logger.severe(msgSupplier);
    }

    @Override
    public void error(String msg) {
        logger.severe(msg);
    }

    @Override
    public void warning(Supplier<String> msgSupplier) {
        logger.warning(msgSupplier);
    }

    @Override
    public void warning(String msg) {
        logger.warning(msg);
    }

    @Override
    public void info(Supplier<String> msgSupplier) {
        logger.info(msgSupplier);
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public boolean isLogger() {
        return true;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
