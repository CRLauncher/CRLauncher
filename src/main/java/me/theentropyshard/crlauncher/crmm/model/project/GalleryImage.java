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

package me.theentropyshard.crlauncher.crmm.model.project;

public class GalleryImage {
    private String id;
    private String name;
    private String description;
    private String image;
    private String imageThumbnail;
    private boolean featured;
    private String dateCreated;
    private int orderIndex;

    public GalleryImage() {

    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getImage() {
        return this.image;
    }

    public String getImageThumbnail() {
        return this.imageThumbnail;
    }

    public boolean isFeatured() {
        return this.featured;
    }

    public String getDateCreated() {
        return this.dateCreated;
    }

    public int getOrderIndex() {
        return this.orderIndex;
    }
}
