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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.fabric;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.cosmic.mods.fabric.FabricMod;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.StreamUtils;
import me.theentropyshard.crlauncher.utils.json.Json;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

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
import java.util.List;

public class FabricModsView extends JPanel {


    private final FabricModsTableModel fabricModsModel;
    private final JButton deleteModButton;

    public FabricModsView(Instance instance) {
        super(new BorderLayout());

        Language language = CRLauncher.getInstance().getLanguage();

        JButton addJarMod = new JButton(language.getString("gui.instanceSettingsDialog.modsTab.modsTable.fabric.addModButton"));
        this.add(addJarMod, BorderLayout.NORTH);

        this.fabricModsModel = new FabricModsTableModel(instance);

        addJarMod.addActionListener(e -> {
            new Worker<Void, Void>("picking Fabric mod") {
                @Override
                protected Void work() throws Exception {
                    UIManager.put("FileChooser.readOnly", Boolean.TRUE);
                    JFileChooser fileChooser = new JFileChooser();
                    String archives = language.getString("gui.general.archives");
                    fileChooser.setFileFilter(new FileNameExtensionFilter(archives + " (*.zip, *.jar)", "zip", "jar"));

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

                        List<FabricMod> fabricMods = instance.getFabricMods();

                        Path jarModPath = selectedFile.toPath().toAbsolutePath().normalize();

                        FabricMod mod;
                        try (ZipFile file = new ZipFile(jarModPath.toFile())) {
                            FileHeader fileHeader = file.getFileHeader("fabric.mod.json");
                            if (fileHeader == null) {
                                Log.warn(jarModPath + " does not contain 'fabric.mod.json'");
                                MessageBox.showErrorMessage(CRLauncher.frame, jarModPath + " is not a valid Fabric mod");
                                return null;
                            }

                            String json = StreamUtils.readToString(file.getInputStream(fileHeader));
                            mod = Json.parse(json, FabricMod.class);

                            if (fabricMods.stream().anyMatch(fabricMod -> fabricMod.getId().equals(mod.getId()))) {
                                MessageBox.showErrorMessage(CRLauncher.frame, "Mod with id '" + mod.getId() + "' already added!");
                                return null;
                            }

                            mod.setActive(true);
                            fabricMods.add(mod);

                            FabricModsView.this.fabricModsModel.add(mod);
                        }

                        Path fabricModsDir = instance.getFabricModsDir();
                        FileUtils.createDirectoryIfNotExists(fabricModsDir);

                        Path modPathInFolder = fabricModsDir.resolve(jarModPath.getFileName());

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

        this.deleteModButton = new JButton(language.getString("gui.instanceSettingsDialog.modsTab.modsTable.puzzle.deleteModButton"));

        JTable fabricModsTable = new JTable(this.fabricModsModel);
        fabricModsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = fabricModsTable.getSelectedRow();
                if (selectedRow == -1) {
                    return;
                }

                FabricModsView.this.deleteModButton.setEnabled(true);
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

            FabricMod fabricMod = this.fabricModsModel.fabricModAt(selectedRow);
            this.fabricModsModel.removeRow(selectedRow);
            instance.getFabricMods().remove(fabricMod);

            Path modFile = Paths.get(fabricMod.getFilePath());

            if (Files.exists(modFile)) {
                try {
                    FileUtils.delete(modFile);
                } catch (IOException ex) {
                    Log.error("Exception while trying to delete Fabric Mod", ex);
                }
            }
        });

        this.add(this.deleteModButton, BorderLayout.SOUTH);

        new Worker<Void, Void>("loading fabric mods") {
            @Override
            protected Void work() throws Exception {
                List<FabricMod> fabricMods = instance.getFabricMods();

                Path fabricModsDir = instance.getFabricModsDir();

                if (!Files.exists(fabricModsDir)) {
                    return null;
                }

                for (Path modFile : FileUtils.list(fabricModsDir)) {

                    try (ZipFile file = new ZipFile(modFile.toFile())) {
                        FileHeader fileHeader = file.getFileHeader("fabric.mod.json");
                        if (fileHeader == null) {
                            Log.warn(modFile + " does not contain 'fabric.mod.json'");
                            continue;
                        }

                        String json = StreamUtils.readToString(file.getInputStream(fileHeader));
                        FabricMod mod = Json.parse(json, FabricMod.class);

                        if (fabricMods.stream().anyMatch(fabricMod -> fabricMod.getId().equals(mod.getId()))) {
                            continue;
                        }

                        fabricMods.add(mod);
                        mod.setFilePath(modFile.toString());

                        mod.setActive(true);
                        FabricModsView.this.fabricModsModel.add(mod);
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
