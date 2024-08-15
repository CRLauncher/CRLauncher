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

package me.theentropyshard.crlauncher;

import me.theentropyshard.crlauncher.cli.Args;
import me.theentropyshard.crlauncher.logging.Log;

public class Main {
    public static void main(String[] args) {
        Args theArgs = Args.parse(args);

        LauncherProperties.LOGS_DIR.install(theArgs.getWorkDir().resolve("logs"));
        Log.start();

        try {
            new CRLauncher(theArgs, args, theArgs.getWorkDir());
        } catch (Throwable t) {
            Log.error("Unable to start the launcher", t);
            System.exit(1);
        }
    }
}
