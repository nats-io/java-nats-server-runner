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

package io.nats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Read standard output of process and write lines to given {@link Logger} as INFO;
 * depends on {@link ProcessBuilder#redirectErrorStream(boolean)} being set to {@code true} (since only stdout is
 * read).
 *
 * <p>
 * The use of the input stream is threadsafe since it's used only in a single thread&mdash;the one launched by this
 * code.
 */
final class NatsOutputLogger implements Runnable {
    private final Output output;
    private final BufferedReader reader;
    private final List<String> startupLines;
    private boolean inStartupPhase;

    private NatsOutputLogger(Output output, Process process) {
        this.output = output;
        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        startupLines = new ArrayList<>();
        inStartupPhase = true;
    }

    public void endStartupPhase() {
        inStartupPhase = false;
    }

    public List<String> getStartupLines() {
        return startupLines;
    }

    public void logInfo(String line) {
        output.info(() -> line);
        if (inStartupPhase) {
            startupLines.add(line);
        }
    }

    @Override
    public void run() {
        try {
            try {
                reader.lines().forEach(this::logInfo);
            } catch (final UncheckedIOException e) {
                output.warning(() -> "while reading output " + e);
            }
        } finally {
            try {
                reader.close();
            } catch (final IOException e) {
                output.warning(() -> "caught i/o exception closing reader" + e);
            }
        }
    }

    static NatsOutputLogger logOutput(final Output output, final Process process, final String processName) {
        final String threadName = (isBlank(processName) ? "unknown" : processName) + ":" + processId(process);
        NatsOutputLogger nol = new NatsOutputLogger(output, process);
        final Thread t = new Thread(nol);
        t.setName(threadName);
        t.setDaemon(true);
        t.start();
        return nol;
    }

    private static String processId(Process process) {
        try { // java 9+
            return String.format("pid(%s)", MethodHandles.lookup().findVirtual(Process.class, "pid", MethodType.methodType(long.class)).invoke(process));
        } catch (Throwable ignored) {} // NOPMD since MethodHandles.invoke throws Throwable

        try { // openjdk / oraclejdk 8
            final Field pid = process.getClass().getDeclaredField("pid");
            pid.setAccessible(true);
            return String.format("pid(%s)", pid.getInt(process));
        } catch (Exception ignored) {} // NOPMD

        return String.format("id(%s)", process.hashCode());
    }

    private static boolean isBlank(final CharSequence cs) {
        final int strLen = cs.length();
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
