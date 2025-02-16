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

public class DetailsCard extends Card {
    public DetailsCard(Project project) {
        this.setLayout(new BorderLayout());

        JLabel featuredVersionsLabel = new JLabel("<html><b>Details</b><html>");
        featuredVersionsLabel.setBorder(new EmptyBorder(-5, 0, 10, 0));
        featuredVersionsLabel.setFont(featuredVersionsLabel.getFont().deriveFont(16.0f));
        this.add(featuredVersionsLabel, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel(new GridLayout(3, 1));
        detailsPanel.setOpaque(false);

        JLabel licenseLabel = new JLabel("<html><b>LICENSED " + project.getLicenseId() + "</b></html");
        detailsPanel.add(licenseLabel);

        JLabel createdLabel = new JLabel(project.getDatePublished());
        detailsPanel.add(createdLabel);

        JLabel updatedLabel = new JLabel(project.getDateUpdated());
        detailsPanel.add(updatedLabel);

        this.add(detailsPanel, BorderLayout.CENTER);
    }
}
