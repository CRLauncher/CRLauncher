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

package me.theentropyshard.crlauncher.gui.action;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.instance.InstanceManager;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class InstanceImportAction extends AbstractAction {
    private final JDialog dialog;

    public InstanceImportAction(JDialog dialog) {
        super(CRLauncher.getInstance().getLanguage().getString("gui.addInstanceDialog.importInstance.importButton"));

        this.dialog = dialog;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Language language = CRLauncher.getInstance().getLanguage();

        new Worker<InstanceManager.InstanceImportResult, Void>("importing instance") {
            @Override
            protected InstanceManager.InstanceImportResult work() {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setFileFilter(new FileNameExtensionFilter(
                    language.getString("gui.addInstanceDialog.importInstance.crliFiles"),
                    "crli"
                ));

                Settings settings = CRLauncher.getInstance().getSettings();
                if (settings.lastDir != null && !settings.lastDir.isEmpty()) {
                    fileChooser.setCurrentDirectory(new File(settings.lastDir));
                }

                int option = fileChooser.showOpenDialog(CRLauncher.frame);
                if (option != JFileChooser.APPROVE_OPTION) {
                    return null;
                }

                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile == null) {
                    return null;
                }

                settings.lastDir = selectedFile.toPath().toAbsolutePath().getParent().toString();

                if (!selectedFile.getName().endsWith(".crli")) {
                    MessageBox.showErrorMessage(
                        CRLauncher.frame,
                        language.getString("gui.addInstanceDialog.importInstance.wrongExtension")
                    );

                    return null;
                }

                InstanceManager instanceManager = CRLauncher.getInstance().getInstanceManager();
                try {
                    InstanceManager.InstanceImportResult result = instanceManager.importInstance(selectedFile.toPath());

                    switch (result.getStatus()) {
                        case SUCCESS -> MessageBox.showPlainMessage(
                            CRLauncher.frame,
                            language.getString("gui.addInstanceDialog.importInstance.title"),
                            language.getString("gui.addInstanceDialog.importInstance.success")
                        );
                        case BAD_FILE -> MessageBox.showErrorMessage(
                            CRLauncher.frame,
                            language.getString("gui.addInstanceDialog.importInstance.badFile") +
                                ": " + result.getMessage()
                        );
                        case INSTANCE_EXISTS -> MessageBox.showErrorMessage(
                            CRLauncher.frame,
                            language.getString("gui.addInstanceDialog.importInstance.duplicate")
                                .replace("$$INSTANCE_FOLDER$$", String.valueOf(result.getMessage()))
                        );
                    }

                    return result;
                } catch (IOException ex) {
                    MessageBox.showErrorMessage(
                        CRLauncher.frame,
                        language.getString("gui.addInstanceDialog.importInstance.failure")
                    );

                    Log.error("Could not import instance", ex);

                    return null;
                }
            }

            @Override
            protected void done() {
                InstanceImportAction.this.dialog.dispose();

                InstanceManager.InstanceImportResult result;
                try {
                    result = this.get();
                } catch (InterruptedException | ExecutionException ex) {
                    Log.error(ex);

                    return;
                }

                if (result == null || result.getStatus() != InstanceManager.InstanceImportStatus.SUCCESS) {
                    return;
                }

                CRLauncher.getInstance().getGui().getPlayView().loadInstance(
                    ((CosmicInstance) result.getMessage()), true
                );
            }
        }.execute();
    }
}
