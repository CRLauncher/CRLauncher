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

package me.theentropyshard.crlauncher.gui.view.crmm.modview.side;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.crmm.model.project.Member;
import me.theentropyshard.crlauncher.crmm.model.project.Project;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectVersion;
import me.theentropyshard.crlauncher.gui.components.Card;
import me.theentropyshard.crlauncher.gui.components.MouseListenerBuilder;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTableModel;
import me.theentropyshard.crlauncher.gui.view.crmm.modview.ModDownloadWorker;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.language.LanguageSection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseListener;

public class FeaturedVersionsCard extends Card {
    public FeaturedVersionsCard(Project project, CosmicInstance instance, ModsTableModel tableModel) {
        this.setLayout(new BorderLayout());

        LanguageSection section = CRLauncher.getInstance().getLanguage().getSection("gui.searchCRMMModsDialog.modViewDialog.sideView.featuredVersionsCard");

        JLabel featuredVersionsLabel = new JLabel("<html><b>" + section.getString("title") + "</b><html>");
        featuredVersionsLabel.setBorder(new EmptyBorder(-5, 0, 10, 0));
        featuredVersionsLabel.setFont(featuredVersionsLabel.getFont().deriveFont(16.0f));
        this.add(featuredVersionsLabel, BorderLayout.NORTH);

        JPanel membersPanel = new JPanel();
        membersPanel.setOpaque(false);

        if (!project.hasFeaturedVersions()) {
            return;
        }

        int count = 0;

        for (ProjectVersion version : project.getFeaturedVersions()) {
            membersPanel.add(new VersionCard(version.getTitle(), version.getVersionNumber(), () -> {
                new ModDownloadWorker(instance, tableModel, version, version.getPrimaryFile()).execute();
            }));

            count++;
        }

        membersPanel.setLayout(new GridLayout(count, 1, 0, 4));

        this.add(membersPanel, BorderLayout.CENTER);
    }

    private static final class VersionCard extends Card {
        public VersionCard(String name, String role, Runnable onClick) {
            this.setLayout(new BorderLayout());
            this.setBorder(new EmptyBorder(0, 4, 0, 0));
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            FlatSVGIcon.ColorFilter colorFilter = new FlatSVGIcon.ColorFilter(
                color -> CRLauncher.getInstance().getSettings().darkTheme ? Color.LIGHT_GRAY : Color.BLACK
            );

            FlatSVGIcon icon = new FlatSVGIcon(CreatorsCard.class.getResource("/assets/images/download.svg"))
                .setColorFilter(colorFilter)
                .derive(32, 32);

            JLabel avatarLabel = new JLabel(icon);
            avatarLabel.setPreferredSize(new Dimension(32, 32));
            this.add(avatarLabel, BorderLayout.WEST);

            JPanel nameRolePanel = new JPanel(new GridLayout(2, 1));
            nameRolePanel.setBorder(new EmptyBorder(0, 10, 0, 0));
            nameRolePanel.setOpaque(false);
            this.add(nameRolePanel, BorderLayout.CENTER);

            JLabel nameLabel = new JLabel("<html><b>" + name + "</b></html>");
            nameLabel.setFont(nameLabel.getFont().deriveFont(14.0f));
            nameRolePanel.add(nameLabel);

            JLabel roleLabel = new JLabel(role);
            nameRolePanel.add(roleLabel);

            MouseListener listener = new MouseListenerBuilder()
                .mouseClicked(e -> {
                    onClick.run();
                })
                .build();

            this.addMouseListener(listener);
        }
    }
}
