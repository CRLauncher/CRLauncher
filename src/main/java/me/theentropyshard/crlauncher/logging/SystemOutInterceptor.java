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

import java.io.OutputStream;
import java.io.PrintStream;

public class SystemOutInterceptor extends PrintStream {
    private final LogLevel level;

    public SystemOutInterceptor(OutputStream out, LogLevel level) {
        super(out, true);

        this.level = level;
    }

    @Override
    public void print(String s) {
        super.print(s);

        if (this.level == LogLevel.ERROR) {
            Log.error(s);
        } else {
            Log.debug(s);
        }
    }
}
