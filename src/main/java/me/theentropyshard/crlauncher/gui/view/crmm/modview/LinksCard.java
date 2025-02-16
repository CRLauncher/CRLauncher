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

import me.theentropyshard.crlauncher.crmm.model.project.Project;
import me.theentropyshard.crlauncher.gui.components.Card;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LinksCard extends Card {
    private int links;

    public LinksCard(Project project) {
        this.setLayout(new BorderLayout());

        JPanel linksPanel = new JPanel();
        linksPanel.setOpaque(false);

        this.addLinkLabel("Issue tracker", project.getIssueTrackerUrl(), linksPanel);
        this.addLinkLabel("Source code", project.getProjectSourceUrl(), linksPanel);
        this.addLinkLabel("Wiki", project.getProjectWikiUrl(), linksPanel);
        this.addLinkLabel("Discord invite", project.getDiscordInviteUrl(), linksPanel);

        linksPanel.setLayout(new GridLayout(this.links, 1));

        JLabel linksLabel = new JLabel("<html><b>Links</b><html>");
        linksLabel.setBorder(new EmptyBorder(-5, 0, 5, 0));
        linksLabel.setFont(linksLabel.getFont().deriveFont(16.0f));

        this.add(linksLabel, BorderLayout.NORTH);
        this.add(linksPanel, BorderLayout.CENTER);
    }

    private void addLinkLabel(String name, String link, JPanel linksPanel) {
        if (link == null || link.trim().isEmpty()) {
            return;
        }

        linksPanel.add(new LinkLabel(name, link));

        this.links++;
    }
}
