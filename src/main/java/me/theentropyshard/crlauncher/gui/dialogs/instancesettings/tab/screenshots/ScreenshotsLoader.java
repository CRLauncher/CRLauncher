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

import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ScreenshotsLoader extends Worker<Void, ScreenshotInfo> {
    private final ScreenshotsPanel screenshotsPanel;
    private final Path screenshotsDir;

    public ScreenshotsLoader(ScreenshotsPanel screenshotsPanel, Path screenshotsDir) {
        super("loading screenshots");

        this.screenshotsPanel = screenshotsPanel;
        this.screenshotsDir = screenshotsDir;
    }

    @Override
    protected Void work() throws Exception {
        if (!Files.exists(this.screenshotsDir) || FileUtils.countFiles(this.screenshotsDir) == 0) {
            return null;
        }

        List<Path> screenshotFiles = FileUtils.list(this.screenshotsDir);

        for (Path screenshotFile : screenshotFiles) {
            String fileName = screenshotFile.getFileName().toString();
            BufferedImage image = SwingUtils.loadImageFromFile(screenshotFile);

            this.publish(new ScreenshotInfo(
                ImageUtils.fitImageAndResize(image, 192, 108),
                image,
                fileName,
                screenshotFile
            ));
        }

        return null;
    }

    @Override
    protected void process(List<ScreenshotInfo> chunks) {
        for (ScreenshotInfo info : chunks) {
            this.screenshotsPanel.addScreenshot(new ScreenshotItem(info, this.screenshotsPanel));
        }
    }
}
