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
import me.theentropyshard.crlauncher.gui.view.crmm.navbar.NavBar;
import me.theentropyshard.crlauncher.instance.Instance;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CrmmModsView extends JPanel {
    public static final String MODS_TAB = "gui.searchCRMMModsDialog.mods";
    public static final String DATAMODS_TAB = "gui.searchCRMMModsDialog.datamods";
    public static final String RESOURCE_PACKS_TAB = "gui.searchCRMMModsDialog.resourcePacks";
    public static final String SHADERS_TAB = "gui.searchCRMMModsDialog.shaders";
    public static final String MODPACKS_TAB = "gui.searchCRMMModsDialog.modpacks";
    public static final String SEARCH_MODS_PLACEHOLDER = "gui.searchCRMMModsDialog.searchMods";
    public static final String SEARCH_BUTTON = "gui.searchCRMMModsDialog.searchButton";
    public static final String SEARCH_DATAMODS_PLACEHOLDER = "gui.searchCRMMModsDialog.searchDatamods";
    public static final String SEARCH_RESOURCE_PACKS_PLACEHOLDER = "gui.searchCRMMModsDialog.searchResourcePacks";
    public static final String SEARCH_SHADERS_PLACEHOLDER = "gui.searchCRMMModsDialog.searchShaders";
    public static final String SEARCH_MODPACKS_PLACEHOLDER = "gui.searchCRMMModsDialog.searchModpacks";

    private final NavBar navBar;
    private final JTextField searchField;
    private final JButton searchButton;
    private final CardLayout cardLayout;
    private final JPanel modsViewsPanel;

    private final SearchCrmmModsView modsModsView;
    private final SearchCrmmModsView dataModsModsView;
    private final SearchCrmmModsView resourcePacksModsView;
    private final SearchCrmmModsView shadersModsView;
    private final SearchCrmmModsView modpacksView;

    private int tab;

    public CrmmModsView(Instance instance, ModsTab modsTab) {
        super(new BorderLayout());

        Language language = CRLauncher.getInstance().getLanguage();

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 5));

        this.navBar = new NavBar();
        this.navBar.addItem(language.getString(CrmmModsView.MODS_TAB));
        this.navBar.addItem(language.getString(CrmmModsView.DATAMODS_TAB));
        this.navBar.addItem(language.getString(CrmmModsView.RESOURCE_PACKS_TAB));
        this.navBar.addItem(language.getString(CrmmModsView.SHADERS_TAB));
        this.navBar.addItem(language.getString(CrmmModsView.MODPACKS_TAB));
        topPanel.add(this.navBar);

        JPanel searchPanel = new JPanel(new BorderLayout(2, 0));

        this.searchField = new JTextField();
        this.searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, language.getString(CrmmModsView.SEARCH_MODS_PLACEHOLDER));
        searchPanel.add(this.searchField, BorderLayout.CENTER);

        this.searchButton = new JButton(language.getString(CrmmModsView.SEARCH_BUTTON));
        searchPanel.add(this.searchButton, BorderLayout.EAST);

        topPanel.add(searchPanel);

        this.add(topPanel, BorderLayout.NORTH);

        this.cardLayout = new CardLayout();
        this.modsViewsPanel = new JPanel(this.cardLayout);
        this.modsViewsPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        this.modsModsView = new SearchCrmmModsView(instance, modsTab);
        this.modsViewsPanel.add(this.modsModsView, "0");

        this.dataModsModsView = new SearchCrmmDataModsView(instance, modsTab, DataModType.DATAMOD);
        this.modsViewsPanel.add(this.dataModsModsView, "1");

        this.resourcePacksModsView = new SearchCrmmDataModsView(instance, modsTab, DataModType.RESOURCE_PACK);
        this.modsViewsPanel.add(this.resourcePacksModsView, "2");

        this.shadersModsView = new SearchCrmmDataModsView(instance, modsTab, DataModType.SHADER);
        this.modsViewsPanel.add(this.shadersModsView, "3");

        this.modpacksView = new SearchCrmmDataModsView(instance, modsTab, DataModType.MODPACK);
        this.modsViewsPanel.add(this.modpacksView, "4");

        this.modsModsView.searchMods("");

        String[] placeHolders = {
            language.getString(CrmmModsView.SEARCH_MODS_PLACEHOLDER),
            language.getString(CrmmModsView.SEARCH_DATAMODS_PLACEHOLDER),
            language.getString(CrmmModsView.SEARCH_RESOURCE_PACKS_PLACEHOLDER),
            language.getString(CrmmModsView.SEARCH_SHADERS_PLACEHOLDER),
            language.getString(CrmmModsView.SEARCH_MODPACKS_PLACEHOLDER),
        };

        this.navBar.addTabListener(tab -> {
            this.tab = tab;
            this.searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeHolders[tab]);
            ((SearchCrmmModsView) this.modsViewsPanel.getComponent(tab)).searchMods(this.searchField.getText());
            this.cardLayout.show(this.modsViewsPanel, String.valueOf(tab));
        });

        this.searchButton.addActionListener(e -> {
            ((SearchCrmmModsView) this.modsViewsPanel.getComponent(this.tab)).searchMods(this.searchField.getText());
        });

        this.add(this.modsViewsPanel, BorderLayout.CENTER);
    }
}
