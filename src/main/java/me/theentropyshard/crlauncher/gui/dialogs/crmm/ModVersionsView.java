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

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.crmm.CrmmApi;
import me.theentropyshard.crlauncher.crmm.model.mod.Mod;
import me.theentropyshard.crlauncher.crmm.model.project.*;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ModVersionsView extends JPanel {
    private final JPanel modVersionCardsPanel;
    private final Mod mod;
    private final Instance instance;

    public ModVersionsView(Mod mod, Instance instance) {
        super(new BorderLayout());

        this.mod = mod;
        this.instance = instance;

        this.modVersionCardsPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        this.modVersionCardsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.add(this.modVersionCardsPanel, BorderLayout.PAGE_START);

        JScrollPane scrollPane = new JScrollPane(borderPanel);
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
                    ModVersionCard card = new ModVersionCard(version, ModVersionsView.this.instance);
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
