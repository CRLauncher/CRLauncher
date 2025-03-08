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
import me.theentropyshard.crlauncher.crmm.model.project.Project;
import me.theentropyshard.crlauncher.gui.FlatSmoothScrollPaneUI;
import me.theentropyshard.crlauncher.gui.dialogs.AppDialog;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTab;
import me.theentropyshard.crlauncher.gui.view.crmm.modview.CrmmModView;
import me.theentropyshard.crlauncher.gui.view.crmm.modview.WorkerSupplier;
import me.theentropyshard.crlauncher.instance.CosmicInstance;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ModViewDialog extends AppDialog {
    public static JDialog instance;

    public ModViewDialog(Project project, CosmicInstance instance, ModsTab modsTab, WorkerSupplier<?, Void> supplier) {
        super(CRLauncher.frame,
            CRLauncher.getInstance().getLanguage().getString("gui.searchCRMMModsDialog.modViewDialog.title")
                .replace("$$MOD_NAME$$", project.getName()));

        ModViewDialog.instance = this.getDialog();

        this.getDialog().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ModViewDialog.instance = null;
            }
        });

        CrmmModView view = new CrmmModView(project, instance, modsTab, supplier);

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

        this.setContent(scrollPane);

        this.center(0);

        this.setVisible(true);
    }
}
