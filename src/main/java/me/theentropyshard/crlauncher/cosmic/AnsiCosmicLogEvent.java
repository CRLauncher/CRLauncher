/*
 * CRLauncher - https://github.com/CRLauncher/CRLauncher
 * Copyright (C) 2024 CRLauncher
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

package me.theentropyshard.crlauncher.cosmic;

import me.theentropyshard.crlauncher.gui.console.LauncherConsole;
import me.theentropyshard.crlauncher.logging.LogEvent;
import me.theentropyshard.crlauncher.logging.LogLevel;
import me.theentropyshard.crlauncher.utils.ansi.AnsiColor;
import me.theentropyshard.crlauncher.utils.ansi.AnsiParser;
import me.theentropyshard.crlauncher.utils.ansi.AnsiPart;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.List;

public class AnsiCosmicLogEvent extends LogEvent {
    public AnsiCosmicLogEvent(String message) {
        super(LogLevel.INFO, message);
    }

    public AnsiCosmicLogEvent(LogLevel level, String message) {
        super(level, message);
    }

    @Override
    public void appConsole(String message) {
        if (LauncherConsole.instance == null) {
            return;
        }

        LauncherConsole c = LauncherConsole.instance;
        List<AnsiPart> ansiParts = AnsiParser.parseAnsiString(message);
        for (AnsiPart part : ansiParts) {
            Color color = part.getColor().getColor();
            if (color == null) {
                color = this.getLevel().color();
            }
            c.setColor(color).write(AnsiColor.stripAnsiCodes(part.getText()));
        }
    }

    @Override
    public void fileLog4j(Logger log, String message) {
        super.fileLog4j(log, AnsiColor.stripAnsiCodes(message));
    }
}
