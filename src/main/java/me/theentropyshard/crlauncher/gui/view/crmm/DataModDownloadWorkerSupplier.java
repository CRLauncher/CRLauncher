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
import me.theentropyshard.crlauncher.cosmic.mods.Mod;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectFile;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectVersion;
import me.theentropyshard.crlauncher.gui.dialogs.ProgressDialog;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTab;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsView;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.network.progress.ProgressNetworkInterceptor;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ZipUtils;
import net.lingala.zip4j.ZipFile;
import okhttp3.OkHttpClient;

import javax.swing.*;
import java.nio.file.Path;
import java.util.List;
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
            protected Mod work() throws Exception {

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

                Path dataModsDir = instance.getDataModsDir();

                Path dest = null;
                try (ZipFile file = new ZipFile(saveAs.toFile())) {
                    String topLevelDirectory = ZipUtils.findTopLevelDirectory(file.getFileHeaders());

                    if (topLevelDirectory == null) {
                        Log.error("Could not find top level directory in " + saveAs);

                        return null;
                    }

                    dest = dataModsDir.resolve(topLevelDirectory);
                    FileUtils.delete(dest);
                    file.extractAll(dataModsDir.toString());

                    FileUtils.delete(saveAs);

                    Mod mod = new Mod();
                    mod.setActive(true);
                    mod.setName(topLevelDirectory.replace("/", ""));
                    mod.setFileName(dest.getFileName().toString());

                    List<Mod> dataMods = instance.getDataMods();
                    dataMods.removeIf(m -> m.getName().equals(mod.getName()));
                    dataMods.add(mod);

                    return mod;
                } catch (Exception e) {
                    Log.error("Could not extract file " + saveAs + " to dir: " + dest, e);
                }

                return null;
            }

            @Override
            protected void done() {
                Mod dataMod;

                try {
                    dataMod = (Mod) this.get();
                } catch (InterruptedException | ExecutionException ex) {
                    Log.error(ex);

                    return;
                }

                if (dataMod == null) {
                    return;
                }

                ModsView modsView = modsTab.getModsView();
                modsView.getModsTableModel().addMod(dataMod);
            }
        };
    }
}
