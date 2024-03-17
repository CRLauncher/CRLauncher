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

import me.theentropyshard.crlauncher.CRLauncher;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class Instance {
    private String name;
    private String dirName;
    private String groupName;
    private String crVersion;
    private String javaPath;
    private int crWindowWidth;
    private int crWindowHeight;
    private String customWindowString;
    private int minimumMemoryInMegabytes = 512;
    private int maximumMemoryInMegabytes = 2048;
    private LocalDateTime lastTimePlayed = LocalDateTime.now();
    private long lastPlayedForSeconds;
    private long totalPlayedForSeconds;
    private List<JarMod> jarMods;
    private List<FabricMod> fabricMods;

    public Instance() {

    }

    public Instance(String name, String groupName, String crVersion) {
        this.name = name;
        this.groupName = groupName;
        this.crVersion = crVersion;
    }

    public void save() throws IOException {
        CRLauncher.getInstance().getInstanceManager().save(this);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirName() {
        return this.dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getCrVersion() {
        return this.crVersion;
    }

    public void setCrVersion(String crVersion) {
        this.crVersion = crVersion;
    }

    public String getJavaPath() {
        return this.javaPath;
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    public int getCrWindowWidth() {
        return this.crWindowWidth;
    }

    public void setCrWindowWidth(int crWindowWidth) {
        this.crWindowWidth = crWindowWidth;
    }

    public int getCrWindowHeight() {
        return this.crWindowHeight;
    }

    public void setCrWindowHeight(int crWindowHeight) {
        this.crWindowHeight = crWindowHeight;
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

    public long getLastPlayedForSeconds() {
        return this.lastPlayedForSeconds;
    }

    public void setLastPlayedForSeconds(long lastPlayedForSeconds) {
        this.lastPlayedForSeconds = lastPlayedForSeconds;
    }

    public long getTotalPlayedForSeconds() {
        return this.totalPlayedForSeconds;
    }

    public void setTotalPlayedForSeconds(long totalPlayedForSeconds) {
        this.totalPlayedForSeconds = totalPlayedForSeconds;
    }

    public List<JarMod> getJarMods() {
        return this.jarMods;
    }

    public void setJarMods(List<JarMod> jarMods) {
        this.jarMods = jarMods;
    }

    public List<FabricMod> getFabricMods() {
        return this.fabricMods;
    }

    public void setFabricMods(List<FabricMod> fabricMods) {
        this.fabricMods = fabricMods;
    }
}
