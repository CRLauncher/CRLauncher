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

public class UpdatedCosmicWorld extends CosmicWorld {
    private String lastSavedVersion;
    private long worldCreatedEpochMillis;
    private long lastPlayedEpochMillis;

    public UpdatedCosmicWorld() {

    }

    public String getLastSavedVersion() {
        return this.lastSavedVersion;
    }

    public void setLastSavedVersion(String lastSavedVersion) {
        this.lastSavedVersion = lastSavedVersion;
    }

    public long getWorldCreatedEpochMillis() {
        return this.worldCreatedEpochMillis;
    }

    public void setWorldCreatedEpochMillis(long worldCreatedEpochMillis) {
        this.worldCreatedEpochMillis = worldCreatedEpochMillis;
    }

    public long getLastPlayedEpochMillis() {
        return this.lastPlayedEpochMillis;
    }

    public void setLastPlayedEpochMillis(long lastPlayedEpochMillis) {
        this.lastPlayedEpochMillis = lastPlayedEpochMillis;
    }
}
