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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.quilt;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.QuiltMod;
import me.theentropyshard.crlauncher.gui.Gui;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.StreamUtils;
import me.theentropyshard.crlauncher.utils.json.Json;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class QuiltModsView extends JPanel {
    private static final Logger LOG = LogManager.getLogger(QuiltModsView.class);

    private final QuiltModsTableModel quiltModsModel;
    private final JButton deleteModButton;

    public QuiltModsView(Instance instance) {
        super(new BorderLayout());

        JButton addJarMod = new JButton("Add Quilt mod");
        this.add(addJarMod, BorderLayout.NORTH);

        this.quiltModsModel = new QuiltModsTableModel(instance);

        addJarMod.addActionListener(e -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
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

                        List<QuiltMod> fabricMods = instance.getQuiltMods();

                        Path jarModPath = selectedFile.toPath().toAbsolutePath().normalize();

                        QuiltMod mod;
                        try (ZipFile file = new ZipFile(jarModPath.toFile())) {
                            FileHeader fileHeader = file.getFileHeader("quilt.mod.json");
                            if (fileHeader == null) {
                                LOG.warn("{} does not contain 'quilt.mod.json'", jarModPath);
                                MessageBox.showErrorMessage(CRLauncher.frame, jarModPath + " is not a valid Quilt mod");
                                return null;
                            }

                            String json = StreamUtils.readToString(file.getInputStream(fileHeader));
                            mod = Json.parse(json, QuiltMod.class);

                            if (fabricMods.stream().anyMatch(fabricMod -> fabricMod.getId().equals(mod.getId()))) {
                                MessageBox.showErrorMessage(CRLauncher.frame, "Mod with id '" + mod.getId() + "' already added!");
                                return null;
                            }

                            mod.setActive(true);
                            fabricMods.add(mod);

                            QuiltModsView.this.quiltModsModel.add(mod);
                        }

                        Path quiltModsDir = instance.getQuiltModsDir();
                        FileUtils.createDirectoryIfNotExists(quiltModsDir);

                        Path modPathInFolder = quiltModsDir.resolve(jarModPath.getFileName());

                        if (Files.exists(modPathInFolder)) {
                            FileUtils.delete(modPathInFolder);
                        }

                        mod.setFilePath(modPathInFolder.toString());

                        Files.copy(jarModPath, modPathInFolder);
                    }

                    UIManager.put("FileChooser.readOnly", Boolean.FALSE);
                    return null;
                }
            }.execute();
        });

        this.deleteModButton = new JButton("Delete Quilt mod");

        JTable fabricModsTable = new JTable(this.quiltModsModel);
        fabricModsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = fabricModsTable.getSelectedRow();
                if (selectedRow == -1) {
                    return;
                }

                QuiltModsView.this.deleteModButton.setEnabled(true);
            }
        });

        JScrollPane scrollPane = new JScrollPane(fabricModsTable);
        scrollPane.setBorder(null);
        this.add(scrollPane, BorderLayout.CENTER);

        this.deleteModButton.setEnabled(false);
        this.deleteModButton.addActionListener(e -> {
            int selectedRow = fabricModsTable.getSelectedRow();
            if (selectedRow == -1) {
                return;
            }

            QuiltMod fabricMod = this.quiltModsModel.quiltModAt(selectedRow);
            this.quiltModsModel.removeRow(selectedRow);
            instance.getQuiltMods().remove(fabricMod);

            Path modFile = Paths.get(fabricMod.getFilePath());

            if (Files.exists(modFile)) {
                try {
                    FileUtils.delete(modFile);
                } catch (IOException ex) {
                    LOG.error("Exception while trying to delete Quilt Mod", ex);
                }
            }
        });

        this.add(this.deleteModButton, BorderLayout.SOUTH);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<QuiltMod> fabricMods = instance.getQuiltMods();

                Path fabricModsDir = instance.getQuiltModsDir();

                for (Path modFile : FileUtils.list(fabricModsDir)) {

                    try (ZipFile file = new ZipFile(modFile.toFile())) {
                        FileHeader fileHeader = file.getFileHeader("quilt.mod.json");
                        if (fileHeader == null) {
                            LOG.warn("{} does not contain 'quilt.mod.json'", modFile);
                            continue;
                        }

                        String json = StreamUtils.readToString(file.getInputStream(fileHeader));
                        QuiltMod mod = Json.parse(json, QuiltMod.class);

                        if (fabricMods.stream().anyMatch(fabricMod -> fabricMod.getId().equals(mod.getId()))) {
                            continue;
                        }

                        fabricMods.add(mod);
                        mod.setFilePath(modFile.toString());

                        mod.setActive(true);
                        QuiltModsView.this.quiltModsModel.add(mod);
                    }
                }

                return null;
            }

            @Override
            protected void done() {

            }
        }.execute();
    }
}
