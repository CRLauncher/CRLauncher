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

import com.formdev.flatlaf.ui.FlatScrollPaneUI;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.mods.Mod;
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.QuiltMod;
import me.theentropyshard.crlauncher.cosmic.mods.puzzle.PuzzleMod;
import me.theentropyshard.crlauncher.crmm.CrmmApi;
import me.theentropyshard.crlauncher.crmm.ModInfo;
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
import java.util.function.Supplier;

public class ModVersionsView extends JPanel {
    private final JPanel modVersionCardsPanel;
    private final ModInfo modInfo;
    private final Instance instance;
    private final ModsTab modsTab;
    private final WorkerSupplier<?, Void> workerSupplier;

    public ModVersionsView(ModInfo modInfo, Instance instance, ModsTab modsTab, WorkerSupplier<?, Void> workerSupplier) {
        super(new BorderLayout());

        this.modInfo = modInfo;
        this.instance = instance;
        this.modsTab = modsTab;
        this.workerSupplier = workerSupplier;

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
        new Worker<Void, ProjectVersion>("loading versions for mod " + this.modInfo.getName()) {
            @Override
            protected Void work() throws Exception {
                CrmmApi crmmApi = CRLauncher.getInstance().getCrmmApi();
                String slug = ModVersionsView.this.modInfo.getSlug();
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
                    ModVersionCard card = new ModVersionCard(version, e -> {
                        ModVersionsView.this.workerSupplier.getWorker(ModVersionsView.this, version).execute();
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

    public JPanel getModVersionCardsPanel() {
        return this.modVersionCardsPanel;
    }

    public ModInfo getModInfo() {
        return this.modInfo;
    }

    public Instance getInstance() {
        return this.instance;
    }

    public ModsTab getModsTab() {
        return this.modsTab;
    }
}
