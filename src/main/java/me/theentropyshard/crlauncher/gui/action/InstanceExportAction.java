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

package me.theentropyshard.crlauncher.gui.action;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.logging.Log;
import net.lingala.zip4j.ZipFile;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class InstanceExportAction extends AbstractAction {
    private final CosmicInstance instance;

    public InstanceExportAction(CosmicInstance instance) {
        super(CRLauncher.getInstance().getLanguage().getString("gui.instanceSettingsDialog.exportInstance.exportButton"));

        this.instance = instance;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Language language = CRLauncher.getInstance().getLanguage();

        new Worker<Void, Void>("exporting instance") {
            @Override
            protected Void work() {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setFileFilter(new FileNameExtensionFilter(
                    language.getString("gui.instanceSettingsDialog.exportInstance.crliFiles")
                    , "crli"));

                File instanceDir = InstanceExportAction.this.instance.getWorkDir().toFile();
                File saveAs = new File(instanceDir, instanceDir.getName() + ".crli");
                fileChooser.setSelectedFile(saveAs);

                int option = fileChooser.showSaveDialog(CRLauncher.frame);
                if (option != JFileChooser.APPROVE_OPTION) {
                    return null;
                }

                saveAs = fileChooser.getSelectedFile();

                if (saveAs == null) {
                    return null;
                }

                CRLauncher.getInstance().getSettings().lastDir = saveAs.toPath().toAbsolutePath().getParent().toString();

                if (!saveAs.getName().endsWith(".crli")) {
                    MessageBox.showErrorMessage(
                        CRLauncher.frame,
                        language.getString("gui.instanceSettingsDialog.exportInstance.wrongExtension")
                    );
                }

                try (ZipFile zipFile = new ZipFile(saveAs)) {
                    zipFile.addFolder(instanceDir);
                } catch (IOException e) {
                    MessageBox.showErrorMessage(
                        CRLauncher.frame,
                        language.getString("gui.instanceSettingsDialog.exportInstance.failure") +
                            ": " + e.getMessage()
                    );
                    Log.error("Could not export instance " + InstanceExportAction.this.instance.getName(), e);

                    return null;
                }

                MessageBox.showPlainMessage(CRLauncher.frame,
                    language.getString("gui.instanceSettingsDialog.exportInstance.title"),
                    language.getString("gui.instanceSettingsDialog.exportInstance.success")
                );

                return null;
            }
        }.execute();
    }
}
