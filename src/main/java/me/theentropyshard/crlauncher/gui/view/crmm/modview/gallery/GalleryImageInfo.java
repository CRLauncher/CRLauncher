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

package me.theentropyshard.crlauncher.gui.view.crmm.modview.gallery;

import java.awt.image.BufferedImage;

public class GalleryImageInfo {
    private final BufferedImage thumbnail;
    private final String title;
    private final boolean featured;
    private final String dateCreated;
    private final String imageUrl;

    public GalleryImageInfo(BufferedImage thumbnail, String title, boolean featured, String dateCreated, String imageUrl) {
        this.thumbnail = thumbnail;
        this.title = title;
        this.featured = featured;
        this.dateCreated = dateCreated;
        this.imageUrl = imageUrl;
    }

    public BufferedImage getThumbnail() {
        return this.thumbnail;
    }

    public String getTitle() {
        return this.title;
    }

    public boolean isFeatured() {
        return this.featured;
    }

    public String getDateCreated() {
        return this.dateCreated;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }
}
