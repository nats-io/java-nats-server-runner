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

public interface Output {
    void setLevel(Level level);
    void error(Supplier<String> msgSupplier);
    void error(String msg);
    void warning(Supplier<String> msgSupplier);
    void warning(String msg);
    void info(Supplier<String> msgSupplier);
    void info(String msg);
    boolean isLogger();
    Logger getLogger();
}
