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
import me.theentropyshard.crlauncher.crmm.model.project.Project;
import me.theentropyshard.crlauncher.gui.components.Card;
import me.theentropyshard.crlauncher.language.LanguageSection;
import me.theentropyshard.crlauncher.utils.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CompatibilityCard extends Card {
    public CompatibilityCard(Project project) {
        this.setLayout(new BorderLayout());

        LanguageSection section = CRLauncher.getInstance().getLanguage().getSection("gui.searchCRMMModsDialog.modViewDialog.sideView.compatibilityCard");

        JLabel compatibilityLabel = new JLabel("<html><b>" + section.getString("title") + "</b><html>");
        compatibilityLabel.setBorder(new EmptyBorder(-5, 0, 10, 0));
        compatibilityLabel.setFont(compatibilityLabel.getFont().deriveFont(16.0f));
        this.add(compatibilityLabel, BorderLayout.NORTH);

        List<String> loaders = project.getLoaders();

        JPanel contentPanel = new JPanel(new GridLayout(loaders.isEmpty() ? 2 : 3, 1));
        contentPanel.setBorder(new EmptyBorder(0, 0, -5, 0));
        contentPanel.setOpaque(false);
        this.add(contentPanel, BorderLayout.CENTER);

        JPanel gameVersionsPanel = new JPanel(new BorderLayout());
        gameVersionsPanel.setOpaque(false);

        JLabel gameVersionsLabel = new JLabel("<html><b>" + section.getString("gameVersions") + "</b><html>");
        gameVersionsLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        gameVersionsLabel.setFont(gameVersionsLabel.getFont().deriveFont(14.0f));
        gameVersionsPanel.add(gameVersionsLabel, BorderLayout.NORTH);

        JPanel gameVersionsSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gameVersionsSubPanel.setBorder(new EmptyBorder(0, -5, 0, 0));
        gameVersionsSubPanel.setOpaque(false);

        gameVersionsSubPanel.add(new GameVersionLabel(
            project.getGameVersions().get(project.getGameVersions().size() - 1) + " - " + project.getGameVersions().get(0)));

        gameVersionsPanel.add(gameVersionsSubPanel, BorderLayout.CENTER);

        contentPanel.add(gameVersionsPanel);

        if (!loaders.isEmpty()) {
            JPanel loadersPanel = new JPanel(new BorderLayout());
            loadersPanel.setOpaque(false);

            JLabel loadersLabel = new JLabel("<html><b>" + section.getString("loaders") + "</b><html>");
            loadersLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
            loadersLabel.setFont(loadersLabel.getFont().deriveFont(14.0f));
            loadersPanel.add(loadersLabel, BorderLayout.NORTH);

            JPanel loadersSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            loadersSubPanel.setBorder(new EmptyBorder(0, -5, 0, 0));
            loadersSubPanel.setOpaque(false);

            for (String loader : loaders) {
                loader = Arrays.stream(loader.replace("_", " ").split(" "))
                    .map(StringUtils::capitalize)
                    .collect(Collectors.joining(" "));

                loadersSubPanel.add(new GameVersionLabel(loader));
            }

            loadersPanel.add(loadersSubPanel, BorderLayout.CENTER);

            contentPanel.add(loadersPanel);
        }

        JPanel environmentsPanel = new JPanel(new BorderLayout());
        environmentsPanel.setOpaque(false);

        JLabel environmentsLabel = new JLabel("<html><b>" + section.getString("environments") + "</b><html>");
        environmentsLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        environmentsLabel.setFont(environmentsLabel.getFont().deriveFont(14.0f));
        environmentsPanel.add(environmentsLabel, BorderLayout.NORTH);

        JPanel environmentsSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        environmentsSubPanel.setBorder(new EmptyBorder(0, -5, 0, 0));
        environmentsSubPanel.setOpaque(false);

        String clientSide = project.getClientSide();

        if (clientSide != null && clientSide.equals("required")) {
            environmentsSubPanel.add(new GameVersionLabel(section.getString("clientSide")));
        }

        String serverSide = project.getServerSide();

        if (serverSide != null && serverSide.equals("required")) {
            environmentsSubPanel.add(new GameVersionLabel(section.getString("serverSide")));
        }

        environmentsPanel.add(environmentsSubPanel, BorderLayout.CENTER);

        contentPanel.add(environmentsPanel);
    }
}
