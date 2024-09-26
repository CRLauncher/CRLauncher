package me.theentropyshard.crlauncher.gui.view.crmm;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.mods.Mod;
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.QuiltMod;
import me.theentropyshard.crlauncher.cosmic.mods.puzzle.PuzzleMod;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectFile;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectVersion;
import me.theentropyshard.crlauncher.gui.dialogs.ProgressDialog;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTab;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.puzzle.PuzzleModsView;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.quilt.QuiltModsView;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.instance.InstanceType;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.network.progress.ProgressNetworkInterceptor;
import me.theentropyshard.crlauncher.utils.StreamUtils;
import me.theentropyshard.crlauncher.utils.json.Json;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import okhttp3.OkHttpClient;

import javax.swing.*;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("rawtypes")
public class ModDownloadWorkerSupplier implements WorkerSupplier {
    public ModDownloadWorkerSupplier() {

    }

    @Override
    public Worker getWorker(ModVersionsView versionsView, ProjectVersion version, ProjectFile file) {
        Instance instance = versionsView.getInstance();
        ModsTab modsTab = versionsView.getModsTab();

        return new Worker<>("downloading mod " + version.getTitle()) {
            @Override
            protected Mod work() throws Exception {
                ProgressDialog progressDialog = new ProgressDialog("Downloading " + file.getName());

                OkHttpClient httpClient = CRLauncher.getInstance().getHttpClient().newBuilder()
                    .addNetworkInterceptor(new ProgressNetworkInterceptor(progressDialog))
                    .build();

                Path saveAs = switch (instance.getType()) {
                    case VANILLA, FABRIC -> null;
                    case QUILT ->
                        instance.getQuiltModsDir().resolve(file.getName());
                    case PUZZLE ->
                        instance.getPuzzleModsDir().resolve(file.getName());
                };

                if (saveAs == null) {
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

                try (ZipFile file = new ZipFile(saveAs.toFile())) {
                    FileHeader fileHeader = file.getFileHeader(
                        instance.getType() == InstanceType.QUILT ?
                            "quilt.mod.json" : "puzzle.mod.json"
                    );

                    String json = StreamUtils.readToString(file.getInputStream(fileHeader));

                    Mod mod = Json.parse(json,
                        switch (instance.getType()) {
                            case VANILLA, FABRIC -> throw new UnsupportedOperationException();
                            case QUILT -> QuiltMod.class;
                            case PUZZLE -> PuzzleMod.class;
                        }
                    );

                    if (instance.getType() == InstanceType.QUILT) {
                        QuiltMod quiltMod = (QuiltMod) mod;
                        if (instance.getQuiltMods().stream().anyMatch(qMod -> qMod.quiltLoader.id.equals(quiltMod.quiltLoader.id))) {
                            MessageBox.showErrorMessage(CRLauncher.frame, "Mod with id '" + quiltMod.quiltLoader.id + "' already added!");
                            return null;
                        }
                    } else if (instance.getType() == InstanceType.PUZZLE) {
                        PuzzleMod puzzleMod = (PuzzleMod) mod;
                        if (instance.getPuzzleMods().stream().anyMatch(pMod -> puzzleMod.getId().equals(pMod.getId()))) {
                            MessageBox.showErrorMessage(CRLauncher.frame, "Mod with id '" + puzzleMod.getId() + "' already added!");
                            return null;
                        }
                    }

                    mod.setActive(true);

                    return mod;
                }
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

                JPanel modsView = modsTab.getModsView();

                if (modsView instanceof QuiltModsView quiltModsView) {
                    quiltModsView.getQuiltModsModel().add((QuiltMod) mod);
                } else if (modsView instanceof PuzzleModsView puzzleModsView) {
                    puzzleModsView.getPuzzleModsModel().add((PuzzleMod) mod);
                }
            }
        };
    }
}
