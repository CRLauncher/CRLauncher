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

package me.theentropyshard.crlauncher.gui.view.crmm;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.crmm.ModInfo;
import me.theentropyshard.crlauncher.crmm.model.project.Member;
import me.theentropyshard.crlauncher.crmm.model.project.Project;
import me.theentropyshard.crlauncher.gui.FlatSmoothScrollPaneUI;
import me.theentropyshard.crlauncher.gui.dialogs.AppDialog;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTab;
import me.theentropyshard.crlauncher.gui.view.crmm.modview.CrmmModView;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.utils.ListUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ModViewDialog extends AppDialog {
    public ModViewDialog(Project project, CosmicInstance instance, ModsTab modsTab, WorkerSupplier<?, Void> supplier) {
        super(CRLauncher.frame,
            CRLauncher.getInstance().getLanguage().getString("gui.searchCRMMModsDialog.modVersionsDialogTitle") +
                " - " + project.getName());

        /*CrmmModView view = new CrmmModView(project);

        JScrollPane scrollPane = new JScrollPane(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setViewport(new JViewport() {
            @Override
            public void scrollRectToVisible(Rectangle contentRect) {
                contentRect.y = 0;
            }
        });
        scrollPane.setViewportView(view);
        scrollPane.setPreferredSize(new Dimension(1280, 720));
        scrollPane.setBorder(new EmptyBorder(10, 0, 10, 10));
        scrollPane.setUI(new FlatSmoothScrollPaneUI());
        scrollPane.getViewport().setViewPosition(new Point(0, 0));

        this.getDialog().getRootPane().setDefaultButton(view.getHeader().getDownloadButton());

        this.setContent(scrollPane);*/


        Member owner = ListUtils.search(project.getMembers(), Member::isOwner);
        String userName = owner == null ? "Unknown owner" : owner.getUserName();
        ModVersionsView view = new ModVersionsView(new ModInfo(
            project.getIcon(), project.getName(), project.getDescription(), userName, project.getDatePublished(),
            project.getDateUpdated(), String.valueOf(project.getDownloads()), String.valueOf(project.getFollowers()),
            project.getFeaturedCategories(), project.getLoaders(), project.getSlug()
        ), instance, modsTab, supplier);
        view.setPreferredSize(new Dimension(960, 540));
        view.loadVersions();
        this.setContent(view);

        this.center(0);

        this.setVisible(true);
    }
}
