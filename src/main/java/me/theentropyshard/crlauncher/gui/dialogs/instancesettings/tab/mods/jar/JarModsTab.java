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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.jar;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.Tab;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.utils.OperatingSystem;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class JarModsTab extends Tab {
    private final JarModsView jarModsView;

    public JarModsTab(CosmicInstance instance, JDialog dialog) {
        super(CRLauncher.getInstance().getLanguage()
            .getString("gui.instanceSettingsDialog.jarModsTab.name"), instance, dialog);

        Language language = CRLauncher.getInstance().getLanguage();

        JPanel root = this.getRoot();
        root.setLayout(new BorderLayout());

        JPanel jarModsPanel = new JPanel(new BorderLayout());
        jarModsPanel.setBorder(new TitledBorder(language.getString("gui.instanceSettingsDialog.modsTab.modsTable.borderName")));

        this.jarModsView = new JarModsView(instance);
        jarModsPanel.add(this.jarModsView, BorderLayout.CENTER);

        root.add(jarModsPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton openModsFolderButton = new JButton(
            language.getString("gui.instanceSettingsDialog.modsTab.openModsFolder")
        );
        openModsFolderButton.addActionListener(e -> {
            OperatingSystem.open(instance.getJarModsDir());
        });
        bottomPanel.add(openModsFolderButton);

        root.add(bottomPanel, BorderLayout.SOUTH);
    }
}
