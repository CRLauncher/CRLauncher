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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.puzzle;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.cosmic.mods.puzzle.PuzzleMod;
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
import java.util.UUID;

public class PuzzleModsView extends JPanel {
    private final PuzzleModsTableModel puzzleModsModel;
    private final JButton deleteModButton;

    public PuzzleModsView(Instance instance) {
        super(new BorderLayout());

        JButton addJarMod = new JButton("Add Puzzle mod");
        this.add(addJarMod, BorderLayout.NORTH);

        this.puzzleModsModel = new PuzzleModsTableModel(instance);

        addJarMod.addActionListener(e -> {
            new Worker<Void, Void>("picking Puzzle mod") {
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

                        List<PuzzleMod> puzzleMods = instance.getPuzzleMods();

                        Path jarModPath = selectedFile.toPath().toAbsolutePath().normalize();

                        PuzzleMod mod;
                        try (ZipFile file = new ZipFile(jarModPath.toFile())) {
                            FileHeader fileHeader = file.getFileHeader("puzzle.mod.json");
                            if (fileHeader == null) {
                                Log.warn(jarModPath + " does not contain 'puzzle.mod.json'");

                                mod = new PuzzleMod();

                                mod.setId(UUID.randomUUID().toString());
                                mod.setFilePath(jarModPath.toString());
                                mod.setActive(true);
                                mod.setName(jarModPath.getFileName().toString());
                                mod.setVersion("<unknown>");
                                mod.setDescription("<unknown>");

                                if (puzzleMods.stream().anyMatch(puzzleMod -> puzzleMod.getName()
                                        .equals(mod.getName()))) {
                                    MessageBox.showErrorMessage(CRLauncher.frame, "Mod with name '" +
                                            mod.getName() + "' already added!");
                                    return null;
                                }

                            } else {
                                String json = StreamUtils.readToString(file.getInputStream(fileHeader));
                                mod = Json.parse(json, PuzzleMod.class);

                                if (puzzleMods.stream().anyMatch(puzzleMod -> puzzleMod.getId().equals(mod.getId()))) {
                                    MessageBox.showErrorMessage(CRLauncher.frame, "Mod with id '" + mod.getId() + "' already added!");
                                    return null;
                                }

                                mod.setActive(true);
                            }
                            puzzleMods.add(mod);
                            PuzzleModsView.this.puzzleModsModel.add(mod);
                        } catch (Exception e) {
                            Log.error("Unexpected error", e);
                            return null;
                        }

                        Path puzzleModsDir = instance.getPuzzleModsDir();
                        FileUtils.createDirectoryIfNotExists(puzzleModsDir);

                        Path modPathInFolder = puzzleModsDir.resolve(jarModPath.getFileName());

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

        this.deleteModButton = new JButton("Delete Puzzle mod");

        JTable puzzleModsTable = new JTable(this.puzzleModsModel);
        puzzleModsTable.getTableHeader().setEnabled(false);
        puzzleModsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        puzzleModsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = puzzleModsTable.getSelectedRow();
                if (selectedRow == -1) {
                    return;
                }

                PuzzleModsView.this.deleteModButton.setEnabled(true);
            }
        });

        JScrollPane scrollPane = new JScrollPane(puzzleModsTable);
        scrollPane.setBorder(null);
        this.add(scrollPane, BorderLayout.CENTER);

        this.deleteModButton.setEnabled(false);
        this.deleteModButton.addActionListener(e -> {
            int selectedRow = puzzleModsTable.getSelectedRow();
            if (selectedRow == -1) {
                return;
            }

            PuzzleMod puzzleMod = this.puzzleModsModel.puzzleModAt(selectedRow);
            this.puzzleModsModel.removeRow(selectedRow);
            instance.getPuzzleMods().remove(puzzleMod);

            Path modFile = Paths.get(puzzleMod.getFilePath());

            if (Files.exists(modFile)) {
                try {
                    FileUtils.delete(modFile);
                } catch (IOException ex) {
                    Log.error("Exception while trying to delete Puzzle Mod", ex);
                }
            }
        });

        this.add(this.deleteModButton, BorderLayout.SOUTH);

        new Worker<Void, Void>("loading Puzzle mods") {
            @Override
            protected Void work() throws Exception {
                List<PuzzleMod> puzzleMods = instance.getPuzzleMods();

                Path puzzleModsDir = instance.getPuzzleModsDir();

                if (!Files.exists(puzzleModsDir)) {
                    return null;
                }

                for (Path modFile : FileUtils.list(puzzleModsDir)) {
                    try (ZipFile file = new ZipFile(modFile.toFile())) {
                        FileHeader fileHeader = file.getFileHeader("puzzle.mod.json");
                        if (fileHeader == null) {
                            Log.warn(modFile + " does not contain 'puzzle.mod.json'");

                            PuzzleMod mod = new PuzzleMod();
                            mod.setId(UUID.randomUUID().toString());
                            mod.setFilePath(modFile.toString());
                            mod.setActive(true);
                            mod.setName(modFile.getFileName().toString());
                            mod.setVersion("<unknown>");
                            mod.setDescription("<unknown>");

                            if (puzzleMods.stream().anyMatch(puzzleMod -> puzzleMod.getName().equals(mod.getName()))) {
                                continue;
                            }

                            puzzleMods.add(mod);
                            PuzzleModsView.this.puzzleModsModel.add(mod);
                        } else {
                            String json = StreamUtils.readToString(file.getInputStream(fileHeader));
                            PuzzleMod mod = Json.parse(json, PuzzleMod.class);

                            if (puzzleMods.stream().anyMatch(puzzleMod -> puzzleMod.getId().equals(mod.getId()))) {
                                continue;
                            }

                            puzzleMods.add(mod);
                            mod.setFilePath(modFile.toString());
                            mod.setActive(true);

                            PuzzleModsView.this.puzzleModsModel.add(mod);
                        }
                    }
                }

                return null;
            }
        }.execute();
    }

    public PuzzleModsTableModel getPuzzleModsModel() {
        return this.puzzleModsModel;
    }
}
