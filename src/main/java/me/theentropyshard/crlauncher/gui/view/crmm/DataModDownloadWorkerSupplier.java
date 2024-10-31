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
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsView;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.network.progress.ProgressNetworkInterceptor;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ListUtils;
import okhttp3.OkHttpClient;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DataModDownloadWorkerSupplier implements WorkerSupplier {
    public DataModDownloadWorkerSupplier() {

    }

    @Override
    public Worker getWorker(ModVersionsView versionsView, ProjectVersion version, ProjectFile file) {
        ModsView modsView = versionsView.getModsTab().getModsView();
        Instance instance = versionsView.getInstance();

        return new Worker<Void, Mod>("downloading mod " + version.getTitle()) {
            @Override
            protected Void work() throws Exception {
                ProgressDialog progressDialog = new ProgressDialog("Downloading " + file.getName());

                OkHttpClient httpClient = CRLauncher.getInstance().getHttpClient().newBuilder()
                    .addNetworkInterceptor(new ProgressNetworkInterceptor(progressDialog))
                    .build();

                Path dataModPath = instance.getDataModsDir().resolve(file.getName());

                HttpDownload download = new HttpDownload.Builder()
                    .url(file.getUrl())
                    .expectedSize(file.getSize())
                    .httpClient(httpClient)
                    .saveAs(dataModPath)
                    .build();

                SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
                download.execute();
                SwingUtilities.invokeLater(() -> progressDialog.getDialog().dispose());

                Path dataModsDir = instance.getDataModsDir();
                List<Mod> dataMods = instance.getDataMods();

                try (FileSystem fileSystem = FileSystems.newFileSystem(dataModPath, Map.of("create", "false"))) {
                    Path root = fileSystem.getPath("/");

                    List<Path> folders = FileUtils.list(root);

                    for (Path folder : folders) {
                        String fileName = folder.getFileName().toString();

                        Mod mod = new Mod();
                        mod.setActive(true);
                        mod.setName(fileName);
                        mod.setFileName(fileName);

                        if (ListUtils.search(dataMods, m -> m.getName().equals(mod.getName())) != null) {
                            return null;
                        }
                    }

                    for (Path folder : folders) {
                        String fileName = folder.getFileName().toString();

                        Mod mod = new Mod();
                        mod.setActive(true);
                        mod.setName(fileName);
                        mod.setFileName(fileName);

                        try (Stream<Path> stream = Files.walk(folder)) {
                            stream.forEach(zipFile -> {
                                Path targetFile = dataModsDir.resolve(zipFile.toString().substring(1));

                                try {
                                    if (Files.isDirectory(zipFile)) {
                                        FileUtils.createDirectoryIfNotExists(targetFile);
                                    } else {
                                        Files.copy(zipFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                                    }
                                } catch (IOException e) {
                                    Log.error("Could not extract file: " + targetFile, e);
                                }
                            });
                        }

                        dataMods.add(mod);
                        this.publish(mod);
                    }
                }

                FileUtils.delete(dataModPath);

                return null;
            }

            @Override
            protected void process(List<Mod> mods) {
                for (Mod mod : mods) {
                    modsView.getModsTableModel().addMod(mod);
                }
            }
        };
    }
}
