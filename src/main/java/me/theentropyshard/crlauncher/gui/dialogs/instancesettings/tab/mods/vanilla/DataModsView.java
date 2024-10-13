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
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.cosmic.mods.vanilla.DataMod;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ZipUtils;
import net.lingala.zip4j.ZipFile;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DataModsView extends JPanel {
    private final DataModsTableModel dataModsTableModel;
    private final JTable dataModsTable;
    private final JButton deleteDataModButton;

    public DataModsView(Instance instance) {
        super(new BorderLayout());

        Language language = CRLauncher.getInstance().getLanguage();

        JButton addDataModButton = new JButton(language.getString("gui.instanceSettingsDialog.modsTab.modsTable.vanilla.addModButton"));
        this.add(addDataModButton, BorderLayout.NORTH);

        this.dataModsTableModel = new DataModsTableModel(instance);

        addDataModButton.addActionListener(e -> {
            UIManager.put("FileChooser.readOnly", Boolean.TRUE);

            new Worker<DataMod, Void>("picking data mod") {
                @Override
                protected DataMod work() throws Exception {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    fileChooser.setFileFilter(new FileNameExtensionFilter(
                        language.getString("gui.general.archives") + " (*.zip)", "zip"));

                    Settings settings = CRLauncher.getInstance().getSettings();

                    if (settings.lastDir != null && !settings.lastDir.isEmpty()) {
                        fileChooser.setCurrentDirectory(new File(settings.lastDir));
                    }

                    if (fileChooser.showOpenDialog(CRLauncher.frame) != JFileChooser.APPROVE_OPTION) {
                        return null;
                    }

                    File selectedFile = fileChooser.getSelectedFile();

                    if (selectedFile == null) {
                        return null;
                    }

                    settings.lastDir = fileChooser.getCurrentDirectory().getAbsolutePath();
                    Path dataModPath = selectedFile.toPath();

                    Path dataModsDir = instance.getDataModsDir();

                    if (Files.isDirectory(dataModPath)) {
                        Path dest = dataModsDir.resolve(dataModPath.getFileName());
                        FileUtils.delete(dest);
                        FileUtils.copyDirectory(dataModPath, dest);

                        return new DataMod(dest.getFileName().toString(), true);
                    } else {
                        Path dest = null;
                        try (ZipFile file = new ZipFile(dataModPath.toFile())) {
                            String topLevelDirectory = ZipUtils.findTopLevelDirectory(file.getFileHeaders());
                            dest = dataModsDir.resolve(topLevelDirectory);
                            FileUtils.delete(dest);
                            file.extractAll(dataModsDir.toString());

                            return new DataMod(topLevelDirectory.replace("/", ""), true);
                        } catch (Exception e) {
                            Log.error("Could not extract file " + dataModPath + " to dir: " + dest, e);
                        }
                    }

                    return null;
                }

                @Override
                protected void done() {
                    UIManager.put("FileChooser.readOnly", Boolean.FALSE);

                    DataMod dataMod;

                    try {
                        dataMod = this.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        Log.error(ex);

                        return;
                    }

                    if (dataMod == null) {
                        return;
                    }

                    DataModsView.this.dataModsTableModel.addDataMod(dataMod);
                }
            }.execute();
        });

        this.dataModsTable = new JTable(this.dataModsTableModel);
        this.dataModsTable.getTableHeader().setEnabled(false);
        this.dataModsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dataModsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = DataModsView.this.dataModsTable.getSelectedRow();

                if (selectedRow == -1) {
                    return;
                }

                DataModsView.this.deleteDataModButton.setEnabled(true);
            }
        });

        Path dataModsDir = instance.getDataModsDir();
        Path disabledDataModsDir = instance.getDisabledDataModsDir();

        new Worker<Void, DataMod>("loading data mods") {
            @Override
            protected Void work() throws Exception {
                FileUtils.createDirectoryIfNotExists(dataModsDir);
                FileUtils.createDirectoryIfNotExists(disabledDataModsDir);

                for (Path dataModDir : FileUtils.list(dataModsDir)) {
                    if (!Files.isDirectory(dataModDir)) {
                        continue;
                    }

                    String dirName = dataModDir.getFileName().toString();
                    DataMod dataMod = new DataMod(dirName, true);

                    this.publish(dataMod);
                }

                for (Path dataModDir : FileUtils.list(disabledDataModsDir)) {
                    if (!Files.isDirectory(dataModDir)) {
                        continue;
                    }

                    String dirName = dataModDir.getFileName().toString();
                    DataMod dataMod = new DataMod(dirName, false);

                    this.publish(dataMod);
                }

                return null;
            }

            @Override
            protected void process(List<DataMod> chunks) {
                for (DataMod dataMod : chunks) {
                    DataModsView.this.dataModsTableModel.addDataMod(dataMod);
                }
            }
        }.execute();

        JScrollPane scrollPane = new JScrollPane(this.dataModsTable);
        scrollPane.setBorder(null);
        this.add(scrollPane, BorderLayout.CENTER);

        this.deleteDataModButton = new JButton(
            language.getString("gui.instanceSettingsDialog.modsTab.modsTable.vanilla.deleteModButton")
        );
        this.deleteDataModButton.addActionListener(e -> {
            int selectedRow = this.dataModsTable.getSelectedRow();
            if (selectedRow == -1) {
                return;
            }

            DataMod dataMod = this.dataModsTableModel.dataModAt(selectedRow);
            this.dataModsTableModel.removeRow(selectedRow);

            new Worker<Void, Void>("deleting data mod") {
                @Override
                protected Void work() throws Exception {
                    FileUtils.delete(instance.getModPath(dataMod));

                    return null;
                }
            }.execute();
        });

        this.add(this.deleteDataModButton, BorderLayout.SOUTH);
    }

    public DataModsTableModel getDataModsTableModel() {
        return this.dataModsTableModel;
    }
}
