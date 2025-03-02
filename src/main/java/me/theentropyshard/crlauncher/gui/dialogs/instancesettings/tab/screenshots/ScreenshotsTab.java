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

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.Tab;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.OperatingSystem;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;

public class ScreenshotsTab extends Tab {
    private final ScreenshotsPanel screenshotsPanel;

    public ScreenshotsTab(CosmicInstance instance, JDialog dialog) {
        super(CRLauncher.getInstance().getLanguage().getString("gui.instanceSettingsDialog.screenshotsTab.name"), instance, dialog);

        JPanel root = this.getRoot();
        root.setLayout(new BorderLayout());

        this.screenshotsPanel = new ScreenshotsPanel();
        root.add(this.screenshotsPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.setBorder(new MatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));
        root.add(buttonsPanel, BorderLayout.SOUTH);

        JButton openFolder = new JButton(
            CRLauncher.getInstance().getLanguage().getString("gui.instanceSettingsDialog.screenshotsTab.openScreenshotsDirButton")
        );
        openFolder.addActionListener(e -> {
            SwingUtils.startWorker(() -> {
                Path screenshotsDir = instance.getScreenshotsDir();

                try {
                    FileUtils.createDirectoryIfNotExists(screenshotsDir);
                } catch (IOException ex) {
                    Log.error("Could not create screenshots folder: " + screenshotsDir, ex);
                }

                OperatingSystem.open(screenshotsDir);
            });
        });
        buttonsPanel.add(openFolder);

        this.loadScreenshots();
    }

    public void loadScreenshots() {
        new ScreenshotsLoader(this.screenshotsPanel, this.getInstance().getCosmicDir().resolve("screenshots")).execute();
    }
}
