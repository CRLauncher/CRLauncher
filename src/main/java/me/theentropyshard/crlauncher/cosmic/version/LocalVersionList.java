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

package me.theentropyshard.crlauncher.cosmic.version;

import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.json.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LocalVersionList extends VersionList {
    private final Path workDir;

    public LocalVersionList(Path workDir) {
        this.workDir = workDir;
    }

    @Override
    public void load() throws IOException {
        List<Path> dirs = FileUtils.list(this.workDir, Files::isDirectory);

        for (Path dir : dirs) {
            Path clientDir = dir.resolve("client");

            if (!Files.exists(clientDir)) {
                continue;
            }

            Path clientJson = clientDir.resolve(dir.getFileName() + ".json");

            if (!Files.exists(clientJson) || Files.size(clientJson) == 0L) {
                return;
            }

            this.addVersion(Json.parse(FileUtils.readUtf8(clientJson), Version.class));
        }
    }

    @Override
    public Version getLatestVersion() {
        List<Version> versions = this.getVersions();

        if (versions.size() > 0) {
            return versions.get(0);
        }

        return null;
    }
}
