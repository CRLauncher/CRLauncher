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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.worlds;

import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.Tab;
import me.theentropyshard.crlauncher.instance.Instance;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class WorldsTab extends Tab {
    public WorldsTab(Instance instance, JDialog dialog) {
        super("Worlds", instance, dialog);

        JTable worldsTable = new JTable(new WorldsTableModel(instance));

        JScrollPane scrollPane = new JScrollPane(
                worldsTable,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        JPanel root = this.getRoot();
        root.setLayout(new BorderLayout());
        root.add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void save() throws IOException {

    }
}
