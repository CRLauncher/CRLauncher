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

import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.QuiltMod;
import me.theentropyshard.crlauncher.cosmic.mods.fabric.FabricMod;
import me.theentropyshard.crlauncher.cosmic.mods.jar.JarMod;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.json.Json;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Instance {
    private static final String INSTANCE_FILE_NAME = "instance.json";
    private static final String COSMIC_DIR_NAME = "cosmic-reach";
    private static final String JARMODS_DIR_NAME = "jarmods";
    private static final String FABRIC_MODS_DIR_NAME = "fabricmods";
    private static final String DISABLED_FABRIC_MODS_DIR_NAME = "disabledfabricmods";
    private static final String QUILT_MODS_DIR_NAME = "quiltmods";
    private static final String DISABLED_QUILT_MODS_DIR_NAME = "disabledquiltmods";

    private transient Path workDir;

    private String name;
    private String groupName;
    private String cosmicVersion;
    private String javaPath;
    private String iconPath = "/assets/cosmic_logo_x32.png";
    private int cosmicWindowWidth;
    private int cosmicWindowHeight;
    private String customWindowString;
    private int minimumMemoryInMegabytes = 512;
    private int maximumMemoryInMegabytes = 2048;
    private LocalDateTime lastTimePlayed = LocalDateTime.MIN;
    private long lastPlaytime;
    private long totalPlaytime;
    private final List<JarMod> jarMods;
    private final List<FabricMod> fabricMods;
    private final List<QuiltMod> quiltMods;
    private InstanceType type = InstanceType.VANILLA;
    private String fabricVersion;
    private String quiltVersion;

    public Instance() {
        this(null, null, null);
    }

    public Instance(String name, String groupName, String cosmicVersion) {
        this.name = name;
        this.groupName = groupName;
        this.cosmicVersion = cosmicVersion;

        this.jarMods = new ArrayList<>();
        this.fabricMods = new ArrayList<>();
        this.quiltMods = new ArrayList<>();
    }

    public void save() throws IOException {
        FileUtils.writeUtf8(this.getWorkDir().resolve(Instance.INSTANCE_FILE_NAME), Json.write(this));
    }

    public void updatePlaytime(long seconds) {
        this.lastPlaytime = seconds;
        this.totalPlaytime += seconds;
    }

    public List<FabricMod> getFabricMods() {
        return this.fabricMods;
    }

    public List<QuiltMod> getQuiltMods() {
        return this.quiltMods;
    }

    public String getFabricVersion() {
        return this.fabricVersion;
    }

    public void setFabricVersion(String fabricVersion) {
        this.fabricVersion = fabricVersion;
    }

    public String getQuiltVersion() {
        return this.quiltVersion;
    }

    public void setQuiltVersion(String quiltVersion) {
        this.quiltVersion = quiltVersion;
    }

    public InstanceType getType() {
        return this.type;
    }

    public void setType(InstanceType type) {
        this.type = type;
    }

    public Path getWorkDir() {
        return this.workDir;
    }

    public void setWorkDir(Path workDir) {
        this.workDir = workDir;
    }

    public Path getCosmicDir() {
        return this.workDir.resolve(Instance.COSMIC_DIR_NAME);
    }

    public Path getJarModsDir() {
        return this.workDir.resolve(Instance.JARMODS_DIR_NAME);
    }

    public List<JarMod> getJarMods() {
        return this.jarMods;
    }

    public Path getFabricModsDir() {
        return this.workDir.resolve(Instance.FABRIC_MODS_DIR_NAME);
    }

    public Path getDisabledFabricModsDir() {
        return this.workDir.resolve(Instance.DISABLED_FABRIC_MODS_DIR_NAME);
    }

    public Path getQuiltModsDir() {
        return this.workDir.resolve(Instance.QUILT_MODS_DIR_NAME);
    }

    public Path getDisabledQuiltModsDir() {
        return this.workDir.resolve(Instance.DISABLED_QUILT_MODS_DIR_NAME);
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

    public String getIconPath() {
        return this.iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
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
