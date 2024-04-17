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

import java.time.format.DateTimeFormatter;

public class Version {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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
     * Download url
     */
    private String url;

    /**
     * Version hash
     */
    private String sha256;

    /**
     * File size in bytes
     */
    private long size;

    public Version() {

    }

    @Override
    public String toString() {
        return "Version{" +
                "id='" + this.id + '\'' +
                ", type=" + this.type +
                ", releaseTime='" + this.releaseTime + '\'' +
                ", url='" + this.url + '\'' +
                ", hash='" + this.sha256 + '\'' +
                ", size=" + this.size +
                '}';
    }

    public String getId() {
        return this.id;
    }

    public VersionType getType() {
        return this.type;
    }

    public long getReleaseTime() {
        return this.releaseTime;
    }

    public String getUrl() {
        return this.url;
    }

    public String getSha256() {
        return this.sha256;
    }

    public long getSize() {
        return this.size;
    }
}
