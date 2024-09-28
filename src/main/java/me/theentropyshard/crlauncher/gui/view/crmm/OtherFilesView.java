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

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectFile;
import me.theentropyshard.crlauncher.gui.FlatSmoothScrollPaneUI;
import me.theentropyshard.crlauncher.gui.dialogs.AppDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import java.util.List;

public class OtherFilesView extends JPanel {
    private final JPanel fileCardsPanel;

    public OtherFilesView(List<ProjectFile> projectFiles, ModVersionCard.FileListener fileListener) {
        super(new BorderLayout());

        this.fileCardsPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        this.fileCardsPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.add(this.fileCardsPanel, BorderLayout.PAGE_START);

        JScrollPane scrollPane = new JScrollPane(borderPanel);
        scrollPane.setUI(new FlatSmoothScrollPaneUI());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(scrollPane, BorderLayout.CENTER);

        this.setBorder(new EmptyBorder(0, 0, 10, 0));

        ProjectFile primaryFile = null;

        for (ProjectFile projectFile : projectFiles) {
            if (projectFile.isPrimary()) {
                primaryFile = projectFile;

                continue;
            }

            this.addFileCard(new FileCard(projectFile, fileListener, false));
        }

        if (primaryFile != null) {
            this.addFileCardFirst(new FileCard(primaryFile, fileListener, true));
        }
    }

    public void addFileCard(FileCard fileCard) {
        this.fileCardsPanel.add(fileCard);
    }

    public void addFileCardFirst(FileCard fileCard) {
        this.fileCardsPanel.add(fileCard, 0);
    }

    public static void showDialog(List<ProjectFile> projectFiles, ModVersionCard.FileListener fileListener) {
        AppDialog appDialog = new AppDialog(
            CRLauncher.frame, CRLauncher.getInstance().getLanguage().getString("gui.searchCRMMModsDialog.otherFiles")
        ) {};

        OtherFilesView view = new OtherFilesView(projectFiles, fileListener);
        view.setPreferredSize(new Dimension((int) (900 * 1.2), (int) (480 * 1.2)));

        appDialog.setContent(view);
        appDialog.center(0);

        appDialog.setVisible(true);
    }
}
