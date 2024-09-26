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

package me.theentropyshard.crlauncher.gui.view.crmm;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectFile;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectVersion;
import me.theentropyshard.crlauncher.gui.dialogs.ProgressDialog;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTab;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.vanilla.DataModsView;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.network.progress.ProgressNetworkInterceptor;
import me.theentropyshard.crlauncher.utils.FileUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import okhttp3.OkHttpClient;

import javax.swing.*;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public class DataModDownloadWorkerSupplier implements WorkerSupplier {
    public DataModDownloadWorkerSupplier() {

    }

    @Override
    public Worker getWorker(ModVersionsView versionsView, ProjectVersion version, ProjectFile file) {
        ModsTab modsTab = versionsView.getModsTab();
        Instance instance = versionsView.getInstance();

        return new Worker<>("downloading mod " + version.getTitle()) {
            @Override
            protected String work() throws Exception {

                ProgressDialog progressDialog = new ProgressDialog("Downloading " + file.getName());

                OkHttpClient httpClient = CRLauncher.getInstance().getHttpClient().newBuilder()
                    .addNetworkInterceptor(new ProgressNetworkInterceptor(progressDialog))
                    .build();

                Path saveAs = instance.getDataModsDir()
                    .resolve(file.getName());

                HttpDownload download = new HttpDownload.Builder()
                    .url(file.getUrl())
                    .expectedSize(file.getSize())
                    .httpClient(httpClient)
                    .saveAs(saveAs)
                    .build();

                SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
                download.execute();
                SwingUtilities.invokeLater(() -> progressDialog.getDialog().dispose());

                String modName;

                try (ZipFile file = new ZipFile(saveAs.toFile())) {
                    FileHeader firstFileHeader = file.getFileHeaders().get(0);
                    if (firstFileHeader.isDirectory()) {
                        modName = firstFileHeader.getFileName();
                    } else {
                        modName = saveAs.getFileName().toString();
                    }
                    file.extractAll(instance.getDataModsDir().toString());
                }

                FileUtils.delete(saveAs);

                return modName;
            }

            @Override
            protected void done() {
                String modName;
                try {
                    modName = (String) this.get();
                } catch (InterruptedException | ExecutionException ex) {
                    Log.error(ex);

                    return;
                }

                JPanel modsView = modsTab.getModsView();

                if (modsView instanceof DataModsView dataModsView) {
                    dataModsView.getDataModsTableModel().addRow(modName);
                }
            }
        };
    }
}
