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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.vanilla;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.cosmic.mods.jar.JarMod;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class JarModsView extends JPanel {
    private final JarModsTableModel jarModsTableModel;
    private final JTable jarModsTable;
    private final JButton deleteModButton;

    public JarModsView(Instance instance) {
        super(new BorderLayout());

        JButton addJarMod = new JButton("Add jar mod");
        this.add(addJarMod, BorderLayout.NORTH);

        this.jarModsTableModel = new JarModsTableModel(instance);

        this.jarModsTable = new JTable(this.jarModsTableModel);
        this.jarModsTable.getTableHeader().setEnabled(false);
        this.jarModsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.jarModsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = JarModsView.this.jarModsTable.getSelectedRow();
                if (selectedRow == -1) {
                    return;
                }

                JarModsView.this.deleteModButton.setEnabled(true);
            }
        });

        addJarMod.addActionListener(e -> {
            new Worker<Void, Void>("picking jar mod") {
                @Override
                protected Void work() throws Exception {
                    UIManager.put("FileChooser.readOnly", Boolean.TRUE);
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileFilter(new FileNameExtensionFilter("Archives (*.zip, *.jar)", "zip", "jar"));

                    Settings settings = CRLauncher.getInstance().getSettings();
                    if (settings.lastDir != null && !settings.lastDir.isEmpty()) {
                        fileChooser.setCurrentDirectory(new File(settings.lastDir));
                    }

                    int option = fileChooser.showOpenDialog(CRLauncher.frame);
                    if (option == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        if (selectedFile == null) {
                            return null;
                        }

                        settings.lastDir = fileChooser.getCurrentDirectory().getAbsolutePath();

                        List<JarMod> jarMods = instance.getJarMods();

                        Path jarModPath = selectedFile.toPath().toAbsolutePath().normalize();

                        JarMod jarMod = new JarMod(
                                true,
                                jarModPath.toString(),
                                UUID.randomUUID(),
                                jarModPath.getFileName().toString()
                        );
                        jarMods.add(jarMod);

                        JarModsView.this.jarModsTableModel.add(jarMod);
                    }

                    UIManager.put("FileChooser.readOnly", Boolean.FALSE);
                    return null;
                }
            }.execute();
        });

        this.deleteModButton = new JButton("Delete jar mod");

        JScrollPane scrollPane = new JScrollPane(this.jarModsTable);
        scrollPane.setBorder(null);
        this.add(scrollPane, BorderLayout.CENTER);

        this.deleteModButton.setEnabled(false);
        this.deleteModButton.addActionListener(e -> {
            int selectedRow = this.jarModsTable.getSelectedRow();
            if (selectedRow == -1) {
                return;
            }

            JarMod jarMod = this.jarModsTableModel.jarModAt(selectedRow);
            this.jarModsTableModel.removeRow(selectedRow);
            instance.getJarMods().remove(jarMod);
        });

        this.add(this.deleteModButton, BorderLayout.SOUTH);
    }
}
