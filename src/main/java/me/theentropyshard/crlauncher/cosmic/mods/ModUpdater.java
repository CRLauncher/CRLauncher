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

package me.theentropyshard.crlauncher.cosmic.mods;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.QuiltMod;
import me.theentropyshard.crlauncher.cosmic.mods.puzzle.PuzzleMod;
import me.theentropyshard.crlauncher.crmm.CrmmApi;
import me.theentropyshard.crlauncher.crmm.HashesBody;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectFile;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectVersion;
import me.theentropyshard.crlauncher.gui.dialogs.ProgressDialog;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModInstaller;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTableModel;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.network.progress.ProgressNetworkInterceptor;
import me.theentropyshard.crlauncher.utils.*;
import me.theentropyshard.crlauncher.utils.json.Json;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import okhttp3.OkHttpClient;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ModUpdater extends Worker<Void, Pair<Mod, ModLoader>> {
    private final CosmicInstance instance;
    private final ModsTableModel tableModel;
    private final ProgressDialog progressDialog;

    private boolean sync;

    public ModUpdater(CosmicInstance instance, ModsTableModel tableModel) {
        super("updating mods");

        this.instance = instance;
        this.tableModel = tableModel;
        this.progressDialog = new ProgressDialog("Updating mods - " + instance.getName());
    }

    public void runSync() throws Exception {
        this.sync = true;

        this.work();
    }

    @Override
    protected Void work() throws Exception {
        if (this.instance.getModLoader() == ModLoader.VANILLA || this.instance.getModLoader() == ModLoader.FABRIC) {
            return null;
        }

        SwingUtilities.invokeLater(() -> this.progressDialog.setVisible(true));

        CrmmApi crmmApi = CRLauncher.getInstance().getCrmmApi();

        List<Mod> mods = this.instance.getCurrentMods();

        Map<String, Mod> data = new HashMap<>();

        for (Mod mod : mods) {
            Path modPath = this.instance.getModPath(mod, this.instance.getModLoader());

            String modHash = HashUtils.sha512(modPath);

            data.put(modHash, mod);
        }

        Set<String> keys = data.keySet();

        HashesBody body = new HashesBody("sha512", this.instance.getModLoader() == ModLoader.QUILT ? "quilt" : "puzzle",
            new ArrayList<>(keys));

        Map<String, ProjectVersion> dt = crmmApi.getLatestVersions(body);

        for (Map.Entry<String, Mod> entry : data.entrySet()) {
            String hash = entry.getKey();
            Mod mod = entry.getValue();

            ProjectVersion projectVersion = dt.get(hash);

            if (projectVersion == null || projectVersion.getVersionNumber().equals(mod.getVersion())) {
                continue;
            }

            this.downloadMod(projectVersion.getPrimaryFile());
        }

        if (this.sync) {
            this.done();
        }

        return null;
    }

    private void downloadMod(ProjectFile file) throws IOException {
        Path tmpDir = CRLauncher.getInstance().getWorkDir().resolve("tmp");
        FileUtils.createDirectoryIfNotExists(tmpDir);

        SwingUtilities.invokeLater(() -> {
            this.progressDialog.setStage("Downloading " + file.getName());
            this.progressDialog.update(0, 0, 0, false);
        });

        OkHttpClient httpClient = CRLauncher.getInstance().getHttpClient().newBuilder()
            .addNetworkInterceptor(new ProgressNetworkInterceptor(this.progressDialog))
            .build();

        Path saveAs = tmpDir.resolve(file.getName());
        HttpDownload download = new HttpDownload.Builder()
            .url(file.getUrl())
            .expectedSize(file.getSize())
            .httpClient(httpClient)
            .saveAs(saveAs)
            .build();

        download.execute();

        this.publishJavaMod(this.instance.getModLoader(), saveAs);
    }

    private void publishJavaMod(ModLoader loader, Path saveAs) throws IOException {
        Path modFolder = this.instance.getModsDir(loader);

        Mod mod;

        try (ZipFile zipFile = new ZipFile(saveAs.toFile())) {
            FileHeader fileHeader = zipFile.getFileHeader(ModInstaller.getModInfoFile(loader));

            String json = StreamUtils.readToString(zipFile.getInputStream(fileHeader));

            if (loader == ModLoader.QUILT) {
                mod = Json.parse(json, QuiltMod.class).toMod();
            } else if (loader == ModLoader.PUZZLE) {
                mod = Json.parse(json, PuzzleMod.class).toMod();
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

    @Override
    protected void process(List<Pair<Mod, ModLoader>> pairs) {
        for (Pair<Mod, ModLoader> pair : pairs) {
            Mod mod = pair.getLeft();
            ModLoader loader = pair.getRight();

            if (mod == null) {
                return;
            }

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
        }
    }

    @Override
    protected void done() {
        if (this.tableModel != null) {
            this.tableModel.fireTableDataChanged();
        }

        this.progressDialog.getDialog().dispose();
    }
}
