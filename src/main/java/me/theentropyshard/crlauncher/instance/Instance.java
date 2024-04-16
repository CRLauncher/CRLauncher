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

package me.theentropyshard.crlauncher.instance;

import me.theentropyshard.crlauncher.cosmic.mods.jar.JarMod;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.Json;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Instance {
    private transient Path workDir;

    private String name;
    private String groupName;
    private String cosmicVersion;
    private String javaPath;
    private int cosmicWindowWidth;
    private int cosmicWindowHeight;
    private String customWindowString;
    private int minimumMemoryInMegabytes = 512;
    private int maximumMemoryInMegabytes = 2048;
    private LocalDateTime lastTimePlayed = LocalDateTime.MIN;
    private long lastPlaytime;
    private long totalPlaytime;
    private List<JarMod> jarMods;

    public Instance() {

    }

    public Instance(String name, String groupName, String cosmicVersion) {
        this.name = name;
        this.groupName = groupName;
        this.cosmicVersion = cosmicVersion;
    }

    public void save() throws IOException {
        FileUtils.writeUtf8(this.getWorkDir().resolve("instance.json"), Json.write(this));
    }

    public void updatePlaytime(long seconds) {
        this.lastPlaytime = seconds;
        this.totalPlaytime += seconds;
    }

    public Path getWorkDir() {
        return this.workDir;
    }

    public void setWorkDir(Path workDir) {
        this.workDir = workDir;
    }

    public Path getCosmicDir() {
        return this.workDir.resolve("cosmic-reach");
    }

    public Path getJarModsDir() {
        return this.workDir.resolve("jarmods");
    }

    public Path getFabricModsDir() {
        return this.workDir.resolve("fabricmods");
    }

    public List<JarMod> getJarMods() {
        return this.jarMods;
    }

    public void setJarMods(List<JarMod> jarMods) {
        this.jarMods = jarMods;
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

    public String getCosmicVersion() {
        return this.cosmicVersion;
    }

    public void setCosmicVersion(String cosmicVersion) {
        this.cosmicVersion = cosmicVersion;
    }

    public String getJavaPath() {
        return this.javaPath;
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    public int getCosmicWindowWidth() {
        return this.cosmicWindowWidth;
    }

    public void setCosmicWindowWidth(int cosmicWindowWidth) {
        this.cosmicWindowWidth = cosmicWindowWidth;
    }

    public int getCosmicWindowHeight() {
        return this.cosmicWindowHeight;
    }

    public void setCosmicWindowHeight(int cosmicWindowHeight) {
        this.cosmicWindowHeight = cosmicWindowHeight;
    }

    public String getCustomWindowString() {
        return this.customWindowString;
    }

    public void setCustomWindowString(String customWindowString) {
        this.customWindowString = customWindowString;
    }

    public int getMinimumMemoryInMegabytes() {
        return this.minimumMemoryInMegabytes;
    }

    public void setMinimumMemoryInMegabytes(int minimumMemoryInMegabytes) {
        this.minimumMemoryInMegabytes = minimumMemoryInMegabytes;
    }

    public int getMaximumMemoryInMegabytes() {
        return this.maximumMemoryInMegabytes;
    }

    public void setMaximumMemoryInMegabytes(int maximumMemoryInMegabytes) {
        this.maximumMemoryInMegabytes = maximumMemoryInMegabytes;
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
}
