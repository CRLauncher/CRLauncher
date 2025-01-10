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

package me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.maven;

public record MavenArtifact(String groupId, String artifactId, String version) {
    public String jar() {
        return this.artifactId + "-" + this.version + ".jar";
    }

    public String pom() {
        return this.artifactId + "-" + this.version + ".pom";
    }

    public String url() {
        return this.groupId.replace(".", "/") + "/" + this.artifactId + "/" + this.version + "/" + this.jar();
    }
}
