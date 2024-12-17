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

public class CosmicArchiveVersion implements Version {
    /**
     * Version id
     */
    private String id;

    /**
     * Type of the version
     */
    private VersionType type;

    /**
     * Time, when version was released, unix timestamp
     */
    private long releaseTime;

    /**
     * Client artifact
     */
    private Artifact client;

    /**
     * Server artifact
     */
    private Artifact server;

    public CosmicArchiveVersion() {

    }

    @Override
    public String toString() {
        return "Version{" +
            "id='" + this.id + '\'' +
            ", type=" + this.type +
            ", releaseTime=" + this.releaseTime +
            ", client=" + this.client +
            ", server=" + this.server +
            '}';
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public VersionType getType() {
        return this.type;
    }

    @Override
    public long getReleaseTime() {
        return this.releaseTime;
    }

    public Artifact getClient() {
        return this.client;
    }

    public Artifact getServer() {
        return this.server;
    }
}
