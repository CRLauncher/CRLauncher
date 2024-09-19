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

package me.theentropyshard.crlauncher.gui.dialogs.crmm;

import com.formdev.flatlaf.ui.FlatScrollPaneUI;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.QuiltMod;
import me.theentropyshard.crlauncher.cosmic.mods.puzzle.PuzzleMod;
import me.theentropyshard.crlauncher.crmm.CrmmApi;
import me.theentropyshard.crlauncher.crmm.model.mod.Mod;
import me.theentropyshard.crlauncher.crmm.model.project.*;
import me.theentropyshard.crlauncher.gui.SmoothScrollMouseWheelListener;
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
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseWheelListener;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ModVersionsView extends JPanel {
    private final JPanel modVersionCardsPanel;
    private final Mod mod;
    private final Instance instance;
    private final ModsTab modsTab;

    public ModVersionsView(Mod mod, Instance instance, ModsTab modsTab) {
        super(new BorderLayout());

        this.mod = mod;
        this.instance = instance;
        this.modsTab = modsTab;

        this.modVersionCardsPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        this.modVersionCardsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.add(this.modVersionCardsPanel, BorderLayout.PAGE_START);

        JScrollPane scrollPane = new JScrollPane(borderPanel);
        scrollPane.setUI(new FlatScrollPaneUI() {
            @Override
            protected MouseWheelListener createMouseWheelListener() {
                if (this.isSmoothScrollingEnabled()) {
                    return new SmoothScrollMouseWheelListener(scrollPane.getVerticalScrollBar());
                } else {
                    return super.createMouseWheelListener();
                }
            }
        });
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(scrollPane, BorderLayout.CENTER);
    }

    public void addModVersionCard(ModVersionCard card) {
        this.modVersionCardsPanel.add(card);
    }

    public void loadVersions() {
        new Worker<Void, ProjectVersion>("loading versions for mod " + this.mod.getName()) {
            @Override
            protected Void work() throws Exception {
                CrmmApi crmmApi = CRLauncher.getInstance().getCrmmApi();
                String slug = ModVersionsView.this.mod.getSlug();
                ProjectResponse projectResponse = crmmApi.getProject(slug);

                if (projectResponse == null) {
                    throw new Exception("Could not get project by slug " + slug);
                }

                ProjectVersionResponse response = crmmApi.getProjectVersions(slug);

                if (response.isSuccess()) {
                    List<ProjectVersion> projectVersions = response.getProjectVersions();
                    for (ProjectVersion version : projectVersions) {
                        this.publish(version);
                    }
                } else {
                    Log.warn("Project versions response for " + slug + " is unsuccessful");
                }

                return null;
            }

            @Override
            protected void process(List<ProjectVersion> chunks) {
                for (ProjectVersion version : chunks) {
                    Instance instance = ModVersionsView.this.instance;
                    ModVersionCard card = new ModVersionCard(version, e -> {
                        new Worker<me.theentropyshard.crlauncher.cosmic.mods.Mod, Void>("downloading mod " + version.getTitle()) {
                            @Override
                            protected me.theentropyshard.crlauncher.cosmic.mods.Mod work() throws Exception {
                                ProjectFile primaryFile = version.getPrimaryFile();

                                ProgressDialog progressDialog = new ProgressDialog("Downloading " + primaryFile.getName());

                                OkHttpClient httpClient = CRLauncher.getInstance().getHttpClient().newBuilder()
                                    .addNetworkInterceptor(new ProgressNetworkInterceptor(progressDialog))
                                    .build();

                                Path saveAs = switch (instance.getType()) {
                                    case VANILLA, FABRIC -> null;
                                    case QUILT -> instance.getQuiltModsDir().resolve(primaryFile.getName());
                                    case PUZZLE -> instance.getPuzzleModsDir().resolve(primaryFile.getName());
                                };

                                if (saveAs == null) {
                                    return null;
                                }

                                HttpDownload download = new HttpDownload.Builder()
                                    .url(primaryFile.getUrl())
                                    .expectedSize(primaryFile.getSize())
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

                                    me.theentropyshard.crlauncher.cosmic.mods.Mod mod = Json.parse(json,
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
                                me.theentropyshard.crlauncher.cosmic.mods.Mod mod;
                                try {
                                    mod = this.get();
                                } catch (InterruptedException | ExecutionException ex) {
                                    Log.error(ex);

                                    return;
                                }

                                JPanel modsView = ModVersionsView.this.modsTab.getModsView();

                                if (modsView instanceof QuiltModsView quiltModsView) {
                                    quiltModsView.getQuiltModsModel().add((QuiltMod) mod);
                                } else if (modsView instanceof PuzzleModsView puzzleModsView) {
                                    puzzleModsView.getPuzzleModsModel().add((PuzzleMod) mod);
                                }
                            }
                        }.execute();
                    });
                    ModVersionsView.this.addModVersionCard(card);
                }
            }

            @Override
            protected void done() {
                ModVersionsView.this.modVersionCardsPanel.revalidate();
            }
        }.execute();
    }
}
