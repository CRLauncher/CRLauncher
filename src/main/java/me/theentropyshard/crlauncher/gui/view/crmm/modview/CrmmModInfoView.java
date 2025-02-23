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
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTab;
import me.theentropyshard.crlauncher.gui.view.crmm.ModVersionsView;
import me.theentropyshard.crlauncher.gui.view.crmm.WorkerSupplier;
import me.theentropyshard.crlauncher.instance.CosmicInstance;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CrmmModInfoView extends JPanel {
    public CrmmModInfoView(Project project, CosmicInstance instance, ModsTab modsTab, WorkerSupplier<?, Void> supplier) {
        super(new BorderLayout());

        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);

        JPanel infoCard = new JPanel(new BorderLayout());
        infoCard.add(new ModDescriptionCard(project), BorderLayout.CENTER);

        cardPanel.add(infoCard, "info");

        ModVersionsView versionsView = new ModVersionsView(project, instance, modsTab, supplier);
        cardPanel.add(versionsView, "versions");

        this.add(cardPanel, BorderLayout.CENTER);

        TabsCard tabsCard = new TabsCard(
            () -> {
                cardLayout.show(cardPanel, "info");
            },
            () -> {},
            () -> {},
            () -> {
                versionsView.loadVersions();
                cardLayout.show(cardPanel, "versions");
            }
        );
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(tabsCard, BorderLayout.CENTER);

        this.add(panel, BorderLayout.NORTH);
    }
}
