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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.screenshots;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.Tab;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ScreenshotsTab extends Tab {
    private final ScreenshotsPanel screenshotsPanel;

    public ScreenshotsTab(Instance instance, JDialog dialog) {
        super(CRLauncher.getInstance().getLanguage()
            .getString("gui.instanceSettingsDialog.screenshotsTab.name"), instance, dialog);

        JPanel root = this.getRoot();
        root.setLayout(new BorderLayout());

        this.screenshotsPanel = new ScreenshotsPanel();
        root.add(this.screenshotsPanel, BorderLayout.CENTER);

        this.loadScreenshots();
    }

    public void loadScreenshots() {
        new Worker<Void, ScreenshotInfo>("loading screenshots") {
            @Override
            protected Void work() throws Exception {
                Path screenshotsDir = ScreenshotsTab.this.getInstance().getCosmicDir().resolve("screenshots");

                if (!Files.exists(screenshotsDir) || FileUtils.countFiles(screenshotsDir) == 0) {
                    return null;
                }

                List<Path> screenshotFiles = FileUtils.list(screenshotsDir);

                for (Path screenshotFile : screenshotFiles) {
                    String fileName = screenshotFile.getFileName().toString();

                    BufferedImage image = SwingUtils.loadImageFromFile(screenshotFile);
                    this.publish(new ScreenshotInfo(
                        ImageUtils.fitImageAndResize(image,
                            192, 108),
                        image, fileName.substring(0, fileName.lastIndexOf(".")),
                        screenshotFile));
                }

                return null;
            }

            @Override
            protected void process(List<ScreenshotInfo> chunks) {
                for (ScreenshotInfo info : chunks) {
                    ScreenshotsTab.this.screenshotsPanel.addScreenshot(new ScreenshotItem(
                        ScreenshotsTab.this.screenshotsPanel, info.getImage(), info.getOriginalImage(), info.getText(), info.getFilePath()
                    ));
                    ScreenshotsTab.this.screenshotsPanel.revalidate();
                }
            }
        }.execute();
    }

    @Override
    public void save() throws IOException {

    }

    private static final class ScreenshotInfo {
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
}
