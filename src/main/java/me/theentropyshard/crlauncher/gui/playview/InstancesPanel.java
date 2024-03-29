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

package me.theentropyshard.crlauncher.gui.playview;

import me.theentropyshard.crlauncher.gui.View;
import me.theentropyshard.crlauncher.gui.components.AddInstanceItem;
import me.theentropyshard.crlauncher.gui.components.InstanceItem;
import me.theentropyshard.crlauncher.gui.layouts.WrapLayout;

import javax.swing.*;
import java.awt.*;

public class InstancesPanel extends View {
    private final AddInstanceItem addInstanceItem;
    private final JPanel instancesPanel;
    private final JScrollPane scrollPane;

    public InstancesPanel(AddInstanceItem addInstanceItem) {
        JPanel root = this.getRoot();

        this.addInstanceItem = addInstanceItem;
        this.instancesPanel = new JPanel(new WrapLayout(WrapLayout.LEFT, 8, 8));
        this.instancesPanel.add(this.addInstanceItem);

        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.add(this.instancesPanel, BorderLayout.CENTER);

        this.scrollPane = new JScrollPane(
                borderPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        this.scrollPane.setBorder(null);
        this.scrollPane.getVerticalScrollBar().setUnitIncrement(8);

        root.add(this.scrollPane, BorderLayout.CENTER);
    }

    public void addInstanceItem(InstanceItem item) {
        if (item instanceof AddInstanceItem) {
            throw new IllegalArgumentException("Adding AddInstanceItem is not allowed");
        }

        int count = this.instancesPanel.getComponentCount();
        this.instancesPanel.add(item, count - 1);
    }

    public AddInstanceItem getAddInstanceItem() {
        return this.addInstanceItem;
    }

    public JScrollPane getScrollPane() {
        return this.scrollPane;
    }

    public JPanel getInstancesPanel() {
        return this.instancesPanel;
    }
}
