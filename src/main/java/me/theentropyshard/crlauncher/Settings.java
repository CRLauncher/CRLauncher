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


import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.json.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * I don't usually like making fields public, but ok, those are settings
 */
public class Settings {
    public String language = "English";
    public boolean darkTheme = false;
    public String lastDir = System.getProperty("user.dir");
    public String lastInstanceGroup = "<default>";
    public boolean dialogRelativeParent = true;
    public boolean settingsDialogUpdateToLatest = false;
    public boolean showConsoleAtStartup = true;
    public boolean showAmountOfTime = false;
    public boolean writePrettyJson = false;
    public boolean checkUpdatesStartup = true;
    public int whenCRLaunchesOption = 0;
    public int whenCRExitsOption = 0;
    public boolean consoleScrollDown = true;
    public boolean overrideVersionsPath;
    public String versionsDirPath;
    public boolean overrideInstancesPath;
    public String instancesDirPath;
    public boolean overrideModloadersPath;
    public String modloadersDirPath;
    public boolean appendUsername = true;

    public Settings() {

    }

    public static Settings load(Path file) {
        if (!Files.exists(file)) {
            return new Settings();
        }

        try {
            return Json.parse(FileUtils.readUtf8(file), Settings.class);
        } catch (IOException e) {
            Log.error("Could not load settings from " + file + ", using defaults", e);
        }

        return new Settings();
    }

    public void save(Path file) {
        try {
            FileUtils.writeUtf8(file, this.writePrettyJson ? Json.writePretty(this) : Json.write(this));
        } catch (IOException e) {
            Log.error("Could not save settings to " + file);
        }
    }
}
