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

package me.theentropyshard.crlauncher.gui.view.crmm.modview;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.crmm.CrmmApi;
import me.theentropyshard.crlauncher.crmm.model.project.Project;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectVersion;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTab;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTableModel;
import me.theentropyshard.crlauncher.gui.view.crmm.modview.side.CrmmModSideView;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CrmmModView extends JPanel {
    private final CrmmModViewHeader header;
    private final CrmmModSideView sideView;
    private final CrmmModInfoView infoView;

    public CrmmModView(Project project, CosmicInstance instance, ModsTab modsTab, WorkerSupplier<?, Void> supplier) {
        super(new BorderLayout());

        this.setBorder(new EmptyBorder(0, 10, 0, 10));

        this.header = new CrmmModViewHeader(project);
        this.header.getDownloadButton().addActionListener(e -> {
            CrmmModView.downloadLatestModVersion(project, instance, modsTab.getModsView().getModsTableModel());
        });
        this.add(this.header, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.BOTH;

        this.sideView = new CrmmModSideView(project, instance, modsTab.getModsView().getModsTableModel());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(this.sideView, BorderLayout.PAGE_START);

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.25;
        centerPanel.add(panel, gbc);

        this.infoView = new CrmmModInfoView(project, instance, modsTab, supplier);
        this.infoView.setBorder(new EmptyBorder(0, 10, 0, 0));

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        centerPanel.add(this.infoView, gbc);

        gbc.gridy = 1;
        gbc.weighty = 1;
        centerPanel.add(Box.createHorizontalBox(), gbc);

        this.add(centerPanel, BorderLayout.CENTER);
    }

    public static void downloadLatestModVersion(Project project, CosmicInstance instance, ModsTableModel tableModel) {
        CRLauncher.getInstance().doTask(() -> {
            String loader = switch (instance.getModLoader()) {
                case QUILT -> "quilt";
                case PUZZLE -> "puzzle_loader";
                case FABRIC -> "fabric";
                default -> null;
            };

            if ("fabric".equals(loader)) {
                return;
            }

            CrmmApi api = CRLauncher.getInstance().getCrmmApi();
            ProjectVersion projectVersion = api.getLatestVersion(project.getSlug(), loader).getProjectVersion();

            new ModDownloadWorker(instance, tableModel, projectVersion, projectVersion.getPrimaryFile()).execute();
        });
    }

    public CrmmModViewHeader getHeader() {
        return this.header;
    }
}
