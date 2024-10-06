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

public class Artifact {
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

    public Artifact() {

    }

    @Override
    public String toString() {
        return "Artifact{" +
            "url='" + this.url + '\'' +
            ", sha256='" + this.sha256 + '\'' +
            ", size=" + this.size +
            '}';
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
