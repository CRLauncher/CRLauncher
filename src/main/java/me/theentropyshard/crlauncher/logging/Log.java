/*
 * CRLauncher - https://github.com/CRLauncher/CRLauncher
 * Copyright (C) 2024-2025 CRLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.theentropyshard.crlauncher.logging;

import me.theentropyshard.crlauncher.cosmic.AnsiCosmicLogEvent;
import me.theentropyshard.crlauncher.cosmic.TimeCosmicLogEvent;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;

public final class Log {
    private static final BlockingQueue<LogEvent> EVENT_QUEUE = new ArrayBlockingQueue<>(128);
    private static final boolean WRAP_ERR = true;

    // https://github.com/MultiMC/Launcher/blob/bb04cb09a37e4396ba09068e7d8691ceb6e3cfdc/launcher/minecraft/MinecraftInstance.cpp#L773
    private static final String JAVA_SYMBOL = "([a-zA-Z_$][a-zA-Z\\d_$]*\\.)+[a-zA-Z_$][a-zA-Z\\d_$]*";
    private static final Pattern EXCEPTION_AT_PATTERN = Pattern.compile("\\s+at " + Log.JAVA_SYMBOL);
    private static final Pattern CAUSED_BY_PATTERN = Pattern.compile("Caused by: " + Log.JAVA_SYMBOL);
    private static final Pattern EXCEPTION_PATTERN = Pattern.compile("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)+[a-zA-Z_$]?[a-zA-Z\\d_$]*(Exception|Error|Throwable)");
    private static final Pattern MORE_PATTERN = Pattern.compile("\\.\\.\\. \\d+ more$");

    private static boolean started;

    public static void start() {
        if (Log.started) {
            return;
        }

        new LogQueueProcessor(Log.EVENT_QUEUE).start();

        //System.setOut(new SystemOutInterceptor(System.out, LogLevel.DEBUG));

        if (Log.WRAP_ERR) {
            System.setErr(new SystemOutInterceptor(System.err, LogLevel.ERROR));
        }

        Log.started = true;
    }

    public static void info(String message) {
        Log.EVENT_QUEUE.offer(new LogEvent(LogLevel.INFO, message));
    }

    public static void warn(String message) {
        Log.EVENT_QUEUE.offer(new LogEvent(LogLevel.WARN, message));
    }

    public static void error(String message) {
        Log.EVENT_QUEUE.offer(new LogEvent(LogLevel.ERROR, message));
    }

    public static void debug(String message) {
        Log.EVENT_QUEUE.offer(new LogEvent(LogLevel.DEBUG, message));
    }

    public static void error(String message, Throwable t) {
        Log.error(message);
        Log.error(t);
    }

    public static void error(Throwable t) {
        if (Log.WRAP_ERR) {
            t.printStackTrace();
        } else {
            CharArrayWriter writer = new CharArrayWriter();
            t.printStackTrace(new PrintWriter(writer));
            Log.error(writer.toString());
        }
    }

    public static void cosmicReachModded(String line) {
        Log.EVENT_QUEUE.offer(new AnsiCosmicLogEvent(line));
    }

    public static void cosmicReachVanilla(String line) {
        LogLevel level = null;

        if (line.startsWith("[INFO]")) {
            level = LogLevel.INFO;
        } else if (line.startsWith("[WARNING]")) {
            level = LogLevel.WARN;
        } else if (line.startsWith("[ERROR]")) {
            level = LogLevel.ERROR;
        } else if (line.startsWith("[DEBUG]")) {
            level = LogLevel.DEBUG;
        }

        // https://github.com/MultiMC/Launcher/blob/bb04cb09a37e4396ba09068e7d8691ceb6e3cfdc/launcher/minecraft/MinecraftInstance.cpp#L773
        if (level == null) {
            if (
                line.contains("Exception in thread") ||
                    Log.EXCEPTION_AT_PATTERN.matcher(line).find() ||
                    Log.CAUSED_BY_PATTERN.matcher(line).find() ||
                    Log.EXCEPTION_PATTERN.matcher(line).find() ||
                    Log.MORE_PATTERN.matcher(line).find()
            ) {
                level = LogLevel.ERROR;
            }
        }

        if (level == null) {
            level = LogLevel.INFO;
        }

        Log.EVENT_QUEUE.offer(new TimeCosmicLogEvent(level, line));
    }
}