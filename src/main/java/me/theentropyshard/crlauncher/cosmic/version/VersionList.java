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

package me.theentropyshard.crlauncher.cosmic.version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class VersionList {
    private final Map<String, Version> versions;

    public VersionList() {
        this.versions = new LinkedHashMap<>();
    }

    public boolean isEmpty() {
        return this.versions.isEmpty();
    }

    public abstract void load() throws IOException;

    public void reload() throws IOException {
        this.clear();
        this.load();
    }

    public void clear() {
        this.versions.clear();
    }

    public void addVersion(Version version) {
        this.versions.put(version.getId(), version);
    }

    public Version getVersionById(String id) {
        return this.versions.get(id);
    }

    public abstract Version getLatestVersion();

    public List<Version> getVersions() {
        return new ArrayList<>(this.versions.values());
    }
}
