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
import me.theentropyshard.crlauncher.utils.OperatingSystem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class WorldsTab extends Tab {
    public WorldsTab(Instance instance, JDialog dialog) {
        super("Worlds", instance, dialog);

        JTable worldsTable = new JTable();
        WorldsTableModel worldsModel = new WorldsTableModel(worldsTable, instance);
        worldsTable.setModel(worldsModel);
        worldsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (c instanceof JLabel label) {
                    if (column == 0) {
                        label.setHorizontalAlignment(JLabel.LEFT);
                    } else {
                        label.setHorizontalAlignment(JLabel.CENTER);
                    }
                }

                return c;
            }
        });

        worldsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                ListSelectionModel selectionModel = worldsTable.getSelectionModel();

                int rowAtPoint = worldsTable.rowAtPoint(e.getPoint());
                selectionModel.setSelectionInterval(rowAtPoint, rowAtPoint);
            }
        });

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem copyItem = new JMenuItem("Copy seed");
        copyItem.addActionListener(e -> {
            int selectedRow = worldsTable.getSelectedRow();
            if (selectedRow == -1) {
                return;
            }

            CosmicWorld world = worldsModel.worldAt(selectedRow);
            OperatingSystem.copyToClipboard(String.valueOf(world.getWorldSeed()));
        });

        popupMenu.add(copyItem);
        worldsTable.setComponentPopupMenu(popupMenu);

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
