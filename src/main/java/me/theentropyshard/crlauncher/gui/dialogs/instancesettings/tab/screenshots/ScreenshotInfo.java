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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.screenshots;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

class ScreenshotInfo {
    private final BufferedImage image;
    private final BufferedImage originalImage;
    private final String text;
    private final Path filePath;

    public ScreenshotInfo(BufferedImage image, BufferedImage originalImage, String text, Path filePath) {
        this.image = image;
        this.originalImage = originalImage;
        this.text = text;
        this.filePath = filePath;
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public BufferedImage getOriginalImage() {
        return this.originalImage;
    }

    public String getText() {
        return this.text;
    }

    public Path getFilePath() {
        return this.filePath;
    }
}