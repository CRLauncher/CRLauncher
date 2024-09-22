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
    private final Instance instance;
    private final ModsTab modsTab;

    public DataModDownloadWorkerSupplier(Instance instance, ModsTab modsTab) {
        this.instance = instance;
        this.modsTab = modsTab;
    }

    @Override
    public Worker getWorker(ModVersionsView versionsView, ProjectVersion version) {
        return new Worker<>("downloading mod " + version.getTitle()) {
            @Override
            protected String work() throws Exception {
                ProjectFile primaryFile = version.getPrimaryFile();

                ProgressDialog progressDialog = new ProgressDialog("Downloading " + primaryFile.getName());

                OkHttpClient httpClient = CRLauncher.getInstance().getHttpClient().newBuilder()
                    .addNetworkInterceptor(new ProgressNetworkInterceptor(progressDialog))
                    .build();

                Path saveAs = DataModDownloadWorkerSupplier.this.instance.getDataModsDir()
                    .resolve(primaryFile.getName());

                HttpDownload download = new HttpDownload.Builder()
                    .url(primaryFile.getUrl())
                    .expectedSize(primaryFile.getSize())
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
                    file.extractAll(DataModDownloadWorkerSupplier.this.instance.getDataModsDir().toString());
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

                JPanel modsView = DataModDownloadWorkerSupplier.this.modsTab.getModsView();

                if (modsView instanceof DataModsView dataModsView) {
                    dataModsView.getDataModsTableModel().addRow(modName);
                }
            }
        };
    }
}
