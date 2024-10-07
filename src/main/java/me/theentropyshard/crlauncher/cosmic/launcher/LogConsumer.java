package me.theentropyshard.crlauncher.cosmic.launcher;

import me.theentropyshard.crlauncher.logging.LogLevel;

public interface LogConsumer {
    void line(LogLevel level, String line);

    default void inLine(String line) {
        line(LogLevel.INFO, line);
    }

    default void errLine(String line) {
        line(LogLevel.ERROR, line);
    }
}
