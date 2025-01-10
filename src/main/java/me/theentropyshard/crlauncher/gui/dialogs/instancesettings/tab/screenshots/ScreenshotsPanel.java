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

import me.theentropyshard.crlauncher.gui.FlatSmoothScrollPaneUI;
import me.theentropyshard.crlauncher.gui.layouts.WrapLayout;

import javax.swing.*;
import java.awt.*;

public class ScreenshotsPanel extends JPanel {
    private final JPanel screenshotsPanel;

    public ScreenshotsPanel() {
        super(new BorderLayout());

        this.screenshotsPanel = new JPanel(new WrapLayout(WrapLayout.LEFT, 8, 8));

        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.add(this.screenshotsPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(
            borderPanel,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setUI(new FlatSmoothScrollPaneUI());

        this.add(scrollPane, BorderLayout.CENTER);
    }

    public void addScreenshot(ScreenshotItem item) {
        this.screenshotsPanel.add(item);

        this.revalidate();
    }

    public void removeScreenshot(ScreenshotItem item) {
        this.screenshotsPanel.remove(item);
    }
}
