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

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTab;
import me.theentropyshard.crlauncher.instance.Instance;

import javax.swing.*;
import java.awt.*;

public class CrmmModsView extends JPanel {
    private SearchCrmmModsView modsModsView;
    private SearchCrmmModsView dataModsModsView;
    private SearchCrmmModsView resourcePacksModsView;
    private SearchCrmmModsView shadersModsView;
    private SearchCrmmModpacksView modpacksView;

    public CrmmModsView(Instance instance, ModsTab modsTab) {
        super(new BorderLayout());

        this.modsModsView = new SearchCrmmModsView(instance, modsTab);
        this.dataModsModsView = new SearchCrmmDataModsView(instance, modsTab);
        this.resourcePacksModsView = new SearchCrmmResourcePacksView(instance, modsTab);
        this.shadersModsView = new SearchCrmmShadersView(instance, modsTab);
        this.modpacksView = new SearchCrmmModpacksView(instance, modsTab);

        this.modsModsView.searchMods();

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_FILL);

        Language language = CRLauncher.getInstance().getLanguage();

        tabbedPane.addTab(language.getString("gui.searchCRMMModsDialog.mods"), this.modsModsView);
        tabbedPane.addTab(language.getString("gui.searchCRMMModsDialog.datamods"), this.dataModsModsView);
        tabbedPane.addTab(language.getString("gui.searchCRMMModsDialog.resourcePacks"), this.resourcePacksModsView);
        tabbedPane.addTab(language.getString("gui.searchCRMMModsDialog.shaders"), this.shadersModsView);
        tabbedPane.addTab(language.getString("gui.searchCRMMModsDialog.modpacks"), this.modpacksView);

        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();

            if (selectedIndex == -1) {
                return;
            }

            ((SearchCrmmModsView) tabbedPane.getComponentAt(selectedIndex)).searchMods();
        });

        this.add(tabbedPane, BorderLayout.CENTER);
    }
}
