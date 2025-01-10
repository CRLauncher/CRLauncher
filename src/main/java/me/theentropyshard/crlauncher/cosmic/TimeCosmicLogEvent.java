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

package me.theentropyshard.crlauncher.cosmic;

import me.theentropyshard.crlauncher.gui.console.LauncherConsole;
import me.theentropyshard.crlauncher.logging.LogEvent;
import me.theentropyshard.crlauncher.logging.LogLevel;

public class TimeCosmicLogEvent extends AnsiCosmicLogEvent {
    public TimeCosmicLogEvent(String message) {
        super(message);
    }

    public TimeCosmicLogEvent(LogLevel level, String message) {
        super(level, message);
    }

    @Override
    public void appConsole(String message) {
        if (LauncherConsole.instance == null) {
            return;
        }

        LauncherConsole c = LauncherConsole.instance;
        c.setColor(this.getLevel().color()).setBold(true).write("[" + LogEvent.currentTime() + "]: ");
        c.setBold(false);

        super.appConsole(message);
    }
}
