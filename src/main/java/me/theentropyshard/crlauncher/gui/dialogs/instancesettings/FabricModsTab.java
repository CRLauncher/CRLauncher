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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.gui.Gui;
import me.theentropyshard.crlauncher.cosmic.mods.fabric.FabricMod;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.instance.InstanceManager;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.Json;
import me.theentropyshard.crlauncher.utils.StreamUtils;
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

public class FabricModsTab extends Tab {
    private static final Logger LOG = LogManager.getLogger(FabricModsTab.class);

    private final FabricModsTableModel fabricModsModel;
    private final JButton deleteModButton;

    public FabricModsTab(Instance instance, JDialog dialog) {
        super("Fabric Mods", instance, dialog);

        JPanel root = this.getRoot();
        root.setLayout(new BorderLayout());

        JButton addJarMod = new JButton("Add Fabric mod");
        root.add(addJarMod, BorderLayout.NORTH);

        this.fabricModsModel = new FabricModsTableModel(instance);

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

                    int option = fileChooser.showOpenDialog(CRLauncher.window.getFrame());
                    if (option == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        if (selectedFile == null) {
                            return null;
                        }

                        settings.lastDir = fileChooser.getCurrentDirectory().getAbsolutePath();

                        List<FabricMod> fabricMods = instance.getFabricMods();
                        if (fabricMods == null) {
                            fabricMods = new ArrayList<>();
                            instance.setFabricMods(fabricMods);
                        }

                        Path jarModPath = selectedFile.toPath().toAbsolutePath().normalize();

                        FabricMod mod;
                        try (ZipFile file = new ZipFile(jarModPath.toFile())) {
                            FileHeader fileHeader = file.getFileHeader("fabric.mod.json");
                            if (fileHeader == null) {
                                LOG.warn("{} does not contain 'fabric.mod.json'", jarModPath);
                                Gui.showErrorDialog(jarModPath + " is not a valid Fabric mod");
                                return null;
                            }

                            String json = StreamUtils.readToString(file.getInputStream(fileHeader));
                            mod = Json.parse(json, FabricMod.class);

                            if (fabricMods.stream().anyMatch(fabricMod -> fabricMod.getId().equals(mod.getId()))) {
                                Gui.showErrorDialog("Mod with id '" + mod.getId() + "' already added!");
                                return null;
                            }

                            mod.setActive(true);
                            fabricMods.add(mod);

                            FabricModsTab.this.fabricModsModel.add(mod);
                        }

                        InstanceManager instanceManager = CRLauncher.getInstance().getInstanceManager();
                        Path fabricModsDir = instanceManager.getFabricModsDir(instance);
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

        this.deleteModButton = new JButton("Delete Fabric mod");

        JTable fabricModsTable = new JTable(this.fabricModsModel);
        fabricModsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = fabricModsTable.getSelectedRow();
                if (selectedRow == -1) {
                    return;
                }

                FabricModsTab.this.deleteModButton.setEnabled(true);
            }
        });

        JScrollPane scrollPane = new JScrollPane(fabricModsTable);
        scrollPane.setBorder(null);
        root.add(scrollPane, BorderLayout.CENTER);

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
                    LOG.error("Exception while trying to delete Fabric Mod", ex);
                }
            }
        });

        root.add(this.deleteModButton, BorderLayout.SOUTH);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<FabricMod> fabricMods = instance.getFabricMods();
                if (fabricMods == null) {
                    fabricMods = new ArrayList<>();
                    instance.setFabricMods(fabricMods);
                }

                InstanceManager manager = CRLauncher.getInstance().getInstanceManager();
                Path fabricModsDir = manager.getFabricModsDir(instance);

                for (Path modFile : FileUtils.list(fabricModsDir)) {

                    try (ZipFile file = new ZipFile(modFile.toFile())) {
                        FileHeader fileHeader = file.getFileHeader("fabric.mod.json");
                        if (fileHeader == null) {
                            LOG.warn("{} does not contain 'fabric.mod.json'", modFile);
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
                        FabricModsTab.this.fabricModsModel.add(mod);
                    }
                }

                return null;
            }

            @Override
            protected void done() {

            }
        }.execute();
    }

    @Override
    public void save() throws IOException {

    }
}
