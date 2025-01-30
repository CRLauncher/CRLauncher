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

package me.theentropyshard.crlauncher.instance;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.icon.IconManager;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.json.Json;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Instance {
    private static final String INSTANCE_FILE_NAME = "instance.json";

    private transient Path workDir;

    private String name;
    private String groupName;
    private String iconFileName = "/assets/images/icons/cosmic_logo_x32.png";
    private LocalDateTime lastTimePlayed = LocalDateTime.MIN;
    private long lastPlaytime;
    private long totalPlaytime;
    private Map<String, String> environmentVariables = new LinkedHashMap<>();

    private transient volatile boolean running;

    public Instance() {

    }

    public void save() throws IOException {
        Path file = this.getWorkDir().resolve(Instance.INSTANCE_FILE_NAME);
        boolean pretty = CRLauncher.getInstance().getSettings().writePrettyJson;
        FileUtils.writeUtf8(file, pretty ? Json.writePretty(this) : Json.write(this));
    }

    public void updatePlaytime(long seconds) {
        this.lastPlaytime = seconds;
        this.totalPlaytime += seconds;
    }

    public Icon getIcon() {
        IconManager iconManager = CRLauncher.getInstance().getIconManager();

        Icon icon;

        try {
            icon = iconManager.getIcon(this.iconFileName).icon();
        } catch (Exception e) {
            Log.warn("Could not load icon '" + this.iconFileName + "' for instance '" + this.name + "'");

            String validIconPath = "cosmic_logo_x32.png";
            this.iconFileName = validIconPath;
            icon = iconManager.getIcon(validIconPath).icon();
        }

        return icon;
    }

    public Path getWorkDir() {
        return this.workDir;
    }

    public void setWorkDir(Path workDir) {
        this.workDir = workDir;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getIconFileName() {
        return this.iconFileName;
    }

    public void setIconFileName(String iconFileName) {
        this.iconFileName = iconFileName;
    }

    public LocalDateTime getLastTimePlayed() {
        return this.lastTimePlayed;
    }

    public void setLastTimePlayed(LocalDateTime lastTimePlayed) {
        this.lastTimePlayed = lastTimePlayed;
    }

    public long getLastPlaytime() {
        return this.lastPlaytime;
    }

    public void setLastPlaytime(long lastPlaytime) {
        this.lastPlaytime = lastPlaytime;
    }

    public long getTotalPlaytime() {
        return this.totalPlaytime;
    }

    public void setTotalPlaytime(long totalPlaytime) {
        this.totalPlaytime = totalPlaytime;
    }

    public Map<String, String> getEnvironmentVariables() {
        return this.environmentVariables;
    }

    public void setEnvironmentVariables(LinkedHashMap<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
