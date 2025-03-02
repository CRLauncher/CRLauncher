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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.worlds;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.Tab;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.language.LanguageSection;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.OperatingSystem;
import net.lingala.zip4j.ZipFile;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public class WorldsTab extends Tab {
    public WorldsTab(CosmicInstance instance, JDialog dialog) {
        super(CRLauncher.getInstance().getLanguage()
            .getString("gui.instanceSettingsDialog.worldsTab.name"), instance, dialog);

        Language language = CRLauncher.getInstance().getLanguage();
        LanguageSection section = language.getSection("gui.instanceSettingsDialog.worldsTab");

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
        JMenuItem copyItem = new JMenuItem(section.getString("contextMenu.copySeed"),
            new FlatSVGIcon(WorldsTab.class.getResource("/assets/images/" +
                (CRLauncher.getInstance().getSettings().darkTheme ? "copy_dark.svg" : "copy.svg")))
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

        JMenuItem exportAsZipItem = new JMenuItem(section.getString("contextMenu.exportAsZip"),
            new FlatSVGIcon(WorldsTab.class.getResource("/assets/images/" +
                (CRLauncher.getInstance().getSettings().darkTheme ? "export_dark.svg" : "export.svg")))
        );
        exportAsZipItem.addActionListener(e -> {
            int selectedRow = worldsTable.getSelectedRow();
            if (selectedRow == -1) {
                return;
            }

            CosmicWorld world = worldsModel.worldAt(selectedRow);

            new Worker<Void, Void>("exporting world " + world.getWorldDisplayName()) {
                @Override
                protected Void work() throws Exception {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fileChooser.setFileFilter(new FileNameExtensionFilter("Zip files", "zip"));

                    File worldsDir = instance.getWorldsDir().toFile();
                    File saveAs = new File(worldsDir, world.getWorldDisplayName() + ".zip");
                    fileChooser.setSelectedFile(saveAs);

                    int option = fileChooser.showSaveDialog(CRLauncher.frame);
                    if (option != JFileChooser.APPROVE_OPTION) {
                        return null;
                    }

                    saveAs = fileChooser.getSelectedFile();

                    if (saveAs == null) {
                        return null;
                    }

                    try (ZipFile zipFile = new ZipFile(saveAs)) {
                        zipFile.addFolder(world.getWorldDir().toFile());
                    } catch (IOException e) {
                        MessageBox.showErrorMessage(
                            CRLauncher.frame, section.getString("exportWorld.failure")
                                .replace("$$WORLD_NAME$$", world.getWorldDisplayName()) + ": " + e.getMessage()
                        );

                        Log.error("Could not export world " + world.getWorldDisplayName(), e);

                        return null;
                    }

                    MessageBox.showPlainMessage(CRLauncher.frame,
                        section.getString("exportWorld.title"),
                        section.getString("exportWorld.success").replace("$$WORLD_NAME$$", world.getWorldDisplayName())
                    );

                    return null;
                }
            }.execute();
        });

        popupMenu.add(exportAsZipItem);

        popupMenu.addSeparator();

        JMenuItem deleteItem = new JMenuItem(
            section.getString("contextMenu.delete"),
            new FlatSVGIcon(WorldsTab.class.getResource("/assets/images/" +
                (CRLauncher.getInstance().getSettings().darkTheme ? "delete_dark.svg" : "delete.svg")))
        );
        deleteItem.addActionListener(e -> {
            int selectedRow = worldsTable.getSelectedRow();
            if (selectedRow == -1) {
                return;
            }

            CosmicWorld world = worldsModel.worldAt(selectedRow);

            boolean ok = MessageBox.showConfirmMessage(CRLauncher.frame,
                section.getString("deletingWorld"),
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

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton openWorldsDirButton = new JButton(section.getString("openWorldsDirButton"));
        openWorldsDirButton.addActionListener(e -> {
            SwingUtils.startWorker(() -> {
                Path worldsDir = instance.getWorldsDir();

                try {
                    FileUtils.createDirectoryIfNotExists(worldsDir);
                } catch (IOException ex) {
                    Log.error("Could not create worlds folder: " + worldsDir, ex);
                }

                OperatingSystem.open(worldsDir);
            });
        });
        buttonsPanel.add(openWorldsDirButton);

        JPanel root = this.getRoot();
        root.setLayout(new BorderLayout());
        root.add(scrollPane, BorderLayout.CENTER);
        root.add(buttonsPanel, BorderLayout.SOUTH);
    }
}
