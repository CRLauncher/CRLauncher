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

import javax.swing.*;
import java.awt.*;

public enum LogLevel {
    INFO, WARN, ERROR, DEBUG;

    public Color color() {
        switch (this) {
            case INFO:
                return UIManager.getColor("LauncherConsole.infoColor");
            case WARN:
                return UIManager.getColor("LauncherConsole.warnColor");
            case ERROR:
                return UIManager.getColor("LauncherConsole.errorColor");
            case DEBUG:
                return UIManager.getColor("LauncherConsole.debugColor");
            default:
                throw new IllegalArgumentException("Unreachable");
        }
    }
}