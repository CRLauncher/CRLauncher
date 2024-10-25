package me.theentropyshard.crlauncher.gui.view.crmm;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.mods.Mod;
import me.theentropyshard.crlauncher.cosmic.mods.ModLoader;
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.QuiltMod;
import me.theentropyshard.crlauncher.cosmic.mods.puzzle.PuzzleMod;
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
import me.theentropyshard.crlauncher.utils.ListUtils;
import me.theentropyshard.crlauncher.utils.StreamUtils;
import me.theentropyshard.crlauncher.utils.json.Json;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import okhttp3.OkHttpClient;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("rawtypes")
public class ModDownloadWorkerSupplier implements WorkerSupplier {
    public ModDownloadWorkerSupplier() {

    }

    @Override
    public Worker getWorker(ModVersionsView versionsView, ProjectVersion version, ProjectFile file) {
        Instance instance = versionsView.getInstance();
        ModsTab modsTab = versionsView.getModsTab();

        return new Worker<>("downloading mod " + version.getTitle()) {
            private final AtomicBoolean modExists = new AtomicBoolean(false);

            @Override
            protected Mod work() throws Exception {
                ProgressDialog progressDialog = new ProgressDialog("Downloading " + file.getName());

                OkHttpClient httpClient = CRLauncher.getInstance().getHttpClient().newBuilder()
                    .addNetworkInterceptor(new ProgressNetworkInterceptor(progressDialog))
                    .build();

                Path tmpDir = CRLauncher.getInstance().getWorkDir().resolve("tmp");
                FileUtils.createDirectoryIfNotExists(tmpDir);

                Path saveAs = tmpDir.resolve(file.getName());

                Path modFolder = switch (instance.getModLoader()) {
                    case VANILLA, FABRIC -> null;
                    case QUILT -> instance.getQuiltModsDir();
                    case PUZZLE -> instance.getPuzzleModsDir();
                };

                if (modFolder == null) {
                    return null;
                }

                HttpDownload download = new HttpDownload.Builder()
                    .url(file.getUrl())
                    .expectedSize(file.getSize())
                    .httpClient(httpClient)
                    .saveAs(saveAs)
                    .build();

                SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
                download.execute();
                SwingUtilities.invokeLater(() -> progressDialog.getDialog().dispose());

                Mod mod;

                try (ZipFile zipFile = new ZipFile(saveAs.toFile())) {
                    FileHeader fileHeader = zipFile.getFileHeader(
                        instance.getModLoader() == ModLoader.QUILT ? "quilt.mod.json" : "puzzle.mod.json"
                    );

                    String json = StreamUtils.readToString(zipFile.getInputStream(fileHeader));

                    if (instance.getModLoader() == ModLoader.QUILT) {
                        mod = Json.parse(json, QuiltMod.class).toMod();

                        if (instance.getQuiltMods().stream().anyMatch(m -> m.getId().equals(mod.getId()))) {
                            this.modExists.set(true);
                        }
                    } else if (instance.getModLoader() == ModLoader.PUZZLE) {
                        mod = Json.parse(json, PuzzleMod.class).toMod();

                        if (instance.getPuzzleMods().stream().anyMatch(m -> m.getId().equals(mod.getId()))) {
                            this.modExists.set(true);
                        }
                    } else {
                        mod = null;
                    }
                }

                if (mod == null) {
                    return null;
                }

                FileUtils.createDirectoryIfNotExists(modFolder);

                Path modPath = Files.move(
                    saveAs, modFolder.resolve(saveAs.getFileName()), StandardCopyOption.REPLACE_EXISTING
                );

                mod.setFileName(modPath.getFileName().toString());
                mod.setActive(true);

                return mod;
            }

            @Override
            protected void done() {
                Mod mod;
                try {
                    mod = (Mod) this.get();
                } catch (InterruptedException | ExecutionException ex) {
                    Log.error(ex);

                    return;
                }

                if (mod == null) {
                    return;
                }

                if (this.modExists.get()) {
                    if (instance.getModLoader() == ModLoader.QUILT) {
                        Mod foundMod = ListUtils.search(instance.getQuiltMods(), m -> m.getId().equals(mod.getId()));

                        if (foundMod == null) {
                            return;
                        }

                        if (foundMod.getVersion().equals(mod.getVersion())) {
                            return;
                        }

                        try {
                            FileUtils.delete(instance.getModPath(foundMod, ModLoader.QUILT));
                        } catch (IOException e) {
                            Log.error("Could not delete " + foundMod.getFileName());
                        }

                        foundMod.setVersion(mod.getVersion());
                        foundMod.setFileName(mod.getFileName());
                    } else if (instance.getModLoader() == ModLoader.PUZZLE) {
                        Mod foundMod = ListUtils.search(instance.getPuzzleMods(), m -> m.getId().equals(mod.getId()));

                        if (foundMod == null) {
                            return;
                        }

                        if (foundMod.getVersion().equals(mod.getVersion())) {
                            return;
                        }

                        try {
                            FileUtils.delete(instance.getModPath(foundMod, ModLoader.PUZZLE));
                        } catch (IOException e) {
                            Log.error("Could not delete " + foundMod.getFileName());
                        }

                        foundMod.setVersion(mod.getVersion());
                        foundMod.setFileName(mod.getFileName());
                    }
                } else {
                    ModsView modsView = modsTab.getModsView();
                    modsView.getModsTableModel().addMod(mod);

                    if (instance.getModLoader() == ModLoader.QUILT) {
                        instance.getQuiltMods().add(mod);
                    } else if (instance.getModLoader() == ModLoader.PUZZLE) {
                        instance.getPuzzleMods().add(mod);
                    }
                }
            }
        };
    }
}
