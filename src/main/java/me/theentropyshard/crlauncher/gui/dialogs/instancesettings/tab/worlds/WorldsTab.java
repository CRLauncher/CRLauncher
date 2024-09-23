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

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.Tab;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.OperatingSystem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public class WorldsTab extends Tab {
    public WorldsTab(Instance instance, JDialog dialog) {
        super(CRLauncher.getInstance().getLanguage()
            .getString("gui.instanceSettingsDialog.worldsTab.name"), instance, dialog);

        Language language = CRLauncher.getInstance().getLanguage();

        JTable worldsTable = new JTable();
        worldsTable.getTableHeader().setEnabled(false);
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
        worldsTable.setAutoCreateColumnsFromModel(false);

        worldsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                ListSelectionModel selectionModel = worldsTable.getSelectionModel();

                int rowAtPoint = worldsTable.rowAtPoint(e.getPoint());
                selectionModel.setSelectionInterval(rowAtPoint, rowAtPoint);
            }
        });

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem copyItem = new JMenuItem(
            language.getString("gui.instanceSettingsDialog.worldsTab.contextMenu.copySeed")
        );
        copyItem.addActionListener(e -> {
            int selectedRow = worldsTable.getSelectedRow();
            if (selectedRow == -1) {
                return;
            }

            CosmicWorld world = worldsModel.worldAt(selectedRow);
            OperatingSystem.copyToClipboard(String.valueOf(world.getWorldSeed()));
        });

        popupMenu.add(copyItem);

        JMenuItem deleteItem = new JMenuItem(
            language.getString("gui.instanceSettingsDialog.worldsTab.contextMenu.delete")
        );
        deleteItem.addActionListener(e -> {
            int selectedRow = worldsTable.getSelectedRow();
            if (selectedRow == -1) {
                return;
            }

            CosmicWorld world = worldsModel.worldAt(selectedRow);

            boolean ok = MessageBox.showConfirmMessage(CRLauncher.frame,
                language.getString("gui.instanceSettingsDialog.worldsTab.deletingWorld"),
                language.getString("messages.gui.instanceSettingsDialog.deleteWorldSure")
                    .replace("$$WORLD_NAME$$", world.getWorldDisplayName()));

            if (!ok) {
                return;
            }

            Path worldDir = world.getWorldDir();

            new Worker<Boolean, Void>("deleting world") {
                @Override
                protected Boolean work() throws Exception {
                    try {
                        FileUtils.delete(worldDir);

                        return true;
                    } catch (IOException ex) {
                        Log.error("Could not delete world '" + world.getWorldDisplayName() + "' located at '" +
                            worldDir, ex);

                        MessageBox.showErrorMessage(CRLauncher.frame,
                            language.getString("messages.gui.instanceSettingsDialog.couldNotDeleteWorld")
                                .replace("$$WORLD_NAME$$", world.getWorldDisplayName())
                                .replace("$$WORLD_DIR$$", worldDir.toString()));
                    }

                    return false;
                }

                @Override
                protected void done() {
                    boolean successfullyDeleted;
                    try {
                        successfullyDeleted = this.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        Log.error("Unexpected error", ex);

                        return;
                    }

                    if (successfullyDeleted) {
                        worldsModel.removeRow(selectedRow);
                    }
                }
            }.execute();
        });

        popupMenu.add(deleteItem);

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
