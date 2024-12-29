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
import me.theentropyshard.crlauncher.cosmic.mods.ModLoader;
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.QuiltMod;
import me.theentropyshard.crlauncher.cosmic.mods.puzzle.PuzzleMod;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectFile;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectVersion;
import me.theentropyshard.crlauncher.gui.dialogs.ProgressDialog;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModInstaller;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTableModel;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.network.progress.ProgressNetworkInterceptor;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ListUtils;
import me.theentropyshard.crlauncher.utils.Pair;
import me.theentropyshard.crlauncher.utils.StreamUtils;
import me.theentropyshard.crlauncher.utils.json.Json;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import okhttp3.OkHttpClient;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class ModDownloadWorker extends Worker<Void, Pair<Mod, ModLoader>> {
    private final Instance instance;
    private final ModsTableModel tableModel;
    private final ProjectVersion version;
    private final ProjectFile file;

    public ModDownloadWorker(Instance instance, ModsTableModel tableModel, ProjectVersion version, ProjectFile file) {
        super("downloading mod " + version.getTitle());

        this.instance = instance;
        this.tableModel = tableModel;
        this.version = version;
        this.file = file;
    }

    private final AtomicBoolean modExists = new AtomicBoolean(false);

    @Override
    protected Void work() throws Exception {
        List<String> loaders = this.version.getLoaders();

        if (loaders.size() > 1) {
            SelectLoadersDialog loadersDialog = new SelectLoadersDialog();
            loaders = loadersDialog.getLoaders();
        }

        ProgressDialog progressDialog = new ProgressDialog("Downloading " + this.file.getName());
        progressDialog.setStage("Downloading mod...");

        OkHttpClient httpClient = CRLauncher.getInstance().getHttpClient().newBuilder()
            .addNetworkInterceptor(new ProgressNetworkInterceptor(progressDialog))
            .build();

        Path tmpDir = CRLauncher.getInstance().getWorkDir().resolve("tmp");
        FileUtils.createDirectoryIfNotExists(tmpDir);

        Path saveAs = tmpDir.resolve(this.file.getName());

        HttpDownload download = new HttpDownload.Builder()
            .url(this.file.getUrl())
            .expectedSize(this.file.getSize())
            .httpClient(httpClient)
            .saveAs(saveAs)
            .build();

        SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
        download.execute();
        SwingUtilities.invokeLater(() -> progressDialog.getDialog().dispose());

        if (loaders.size() == 0) {
            this.publishDataMod(saveAs, this.instance.getDataModsDir());
        } else {
            for (String crmmLoader : loaders) {
                this.publishJavaMod(ModLoader.getFromCrmm(crmmLoader), saveAs);
            }
        }

        FileUtils.delete(saveAs);

        return null;
    }

    private void publishJavaMod(ModLoader loader, Path saveAs) throws IOException {
        Path modFolder = this.instance.getModsDir(loader);

        Mod mod;

        try (ZipFile zipFile = new ZipFile(saveAs.toFile())) {
            FileHeader fileHeader = zipFile.getFileHeader(ModInstaller.getModInfoFile(loader));

            String json = StreamUtils.readToString(zipFile.getInputStream(fileHeader));

            if (loader == ModLoader.QUILT) {
                mod = Json.parse(json, QuiltMod.class).toMod();

                if (this.instance.getQuiltMods().stream().anyMatch(m -> m.getId().equals(mod.getId()))) {
                    this.modExists.set(true);
                }
            } else if (loader == ModLoader.PUZZLE) {
                mod = Json.parse(json, PuzzleMod.class).toMod();

                if (this.instance.getPuzzleMods().stream().anyMatch(m -> m.getId().equals(mod.getId()))) {
                    this.modExists.set(true);
                }
            } else {
                return;
            }
        }

        if (mod == null) {
            return;
        }

        FileUtils.createDirectoryIfNotExists(modFolder);

        Path modPath = Files.copy(
            saveAs, modFolder.resolve(saveAs.getFileName()), StandardCopyOption.REPLACE_EXISTING
        );

        mod.setFileName(modPath.getFileName().toString());
        mod.setActive(true);

        this.publish(new Pair<>(mod, loader));
    }

    private void publishDataMod(Path saveAs, Path modsDir) throws IOException {
        List<Mod> dataMods = this.instance.getDataMods();

        try (FileSystem fileSystem = FileSystems.newFileSystem(saveAs, Map.of("create", "false"))) {
            Path root = fileSystem.getPath("/");

            List<Path> folders = FileUtils.list(root);

            for (Path folder : folders) {
                String fileName = folder.getFileName().toString();

                Mod mod = new Mod();
                mod.setActive(true);
                mod.setName(fileName);
                mod.setFileName(fileName);

                Mod foundMod = ListUtils.search(dataMods, m -> m.getName().equals(mod.getName()));

                if (foundMod == null) {
                    continue;
                }

                this.modExists.set(true);
                FileUtils.delete(this.instance.getModPath(foundMod, ModLoader.VANILLA));
                this.instance.getDataMods().remove(foundMod);
            }

            for (Path folder : folders) {
                String fileName = folder.getFileName().toString();

                Mod mod = new Mod();
                mod.setActive(true);
                mod.setName(fileName);
                mod.setFileName(fileName);

                try (Stream<Path> stream = Files.walk(folder)) {
                    stream.forEach(zipFile -> {
                        Path targetFile = modsDir.resolve(zipFile.toString().substring(1));

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
                this.publish(new Pair<>(mod, ModLoader.VANILLA));
            }
        }

        FileUtils.delete(saveAs);
    }

    @Override
    protected void process(List<Pair<Mod, ModLoader>> pairs) {
        for (Pair<Mod, ModLoader> pair : pairs) {
            Mod mod = pair.getLeft();
            ModLoader loader = pair.getRight();

            if (mod == null) {
                return;
            }

            if (this.modExists.get()) {
                if (loader == ModLoader.QUILT) {
                    Mod foundMod = ListUtils.search(this.instance.getQuiltMods(), m -> m.getId().equals(mod.getId()));

                    if (foundMod == null) {
                        return;
                    }

                    if (foundMod.getVersion().equals(mod.getVersion())) {
                        return;
                    }

                    try {
                        FileUtils.delete(this.instance.getModPath(foundMod, ModLoader.QUILT));
                    } catch (IOException e) {
                        Log.error("Could not delete " + foundMod.getFileName());
                    }

                    foundMod.setVersion(mod.getVersion());
                    foundMod.setFileName(mod.getFileName());
                } else if (loader == ModLoader.PUZZLE) {
                    Mod foundMod = ListUtils.search(this.instance.getPuzzleMods(), m -> m.getId().equals(mod.getId()));

                    if (foundMod == null) {
                        return;
                    }

                    if (foundMod.getVersion().equals(mod.getVersion())) {
                        return;
                    }

                    try {
                        FileUtils.delete(this.instance.getModPath(foundMod, ModLoader.PUZZLE));
                    } catch (IOException e) {
                        Log.error("Could not delete " + foundMod.getFileName());
                    }

                    foundMod.setVersion(mod.getVersion());
                    foundMod.setFileName(mod.getFileName());
                }
            } else {
                if (this.instance.getModLoader() == loader) {
                    this.tableModel.addMod(mod);
                }

                this.instance.getMods(loader).add(mod);
            }

            this.modExists.set(false);
        }
    }
}
