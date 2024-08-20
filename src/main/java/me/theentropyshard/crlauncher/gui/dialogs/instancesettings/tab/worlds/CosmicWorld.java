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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.worlds;

import java.nio.file.Path;
import java.time.OffsetDateTime;

public class CosmicWorld {
    private int latestRegionFileVersion;
    private String defaultZoneId;
    private String worldDisplayName;
    private long worldSeed;

    /**
     * This is not present in worldInfo.json. This field is set to the last modified time of localPlayer.json for now
     */
    private transient OffsetDateTime lastPlayed;

    /**
     * This is not present in worldInfo.json
     */
    private transient Path worldDir;

    public CosmicWorld() {

    }

    public Path getWorldDir() {
        return this.worldDir;
    }

    public void setWorldDir(Path worldDir) {
        this.worldDir = worldDir;
    }

    public int getLatestRegionFileVersion() {
        return this.latestRegionFileVersion;
    }

    public String getDefaultZoneId() {
        return this.defaultZoneId;
    }

    public String getWorldDisplayName() {
        return this.worldDisplayName;
    }

    public void setWorldDisplayName(String worldDisplayName) {
        this.worldDisplayName = worldDisplayName;
    }

    public long getWorldSeed() {
        return this.worldSeed;
    }

    public OffsetDateTime getLastPlayed() {
        return this.lastPlayed;
    }

    public void setLastPlayed(OffsetDateTime lastPlayed) {
        this.lastPlayed = lastPlayed;
    }
}
