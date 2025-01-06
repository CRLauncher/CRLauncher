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

import me.theentropyshard.crlauncher.crmm.model.mod.SearchType;
import me.theentropyshard.crlauncher.gui.FlatSmoothScrollPaneUI;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTab;
import me.theentropyshard.crlauncher.instance.CosmicInstance;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SearchCrmmModsView extends JPanel {
    private final JPanel modCardsPanel;
    private final CosmicInstance instance;
    private final ModsTab modsTab;
    private final SearchType searchType;

    public SearchCrmmModsView(CosmicInstance instance, ModsTab modsTab, SearchType searchType) {
        super(new BorderLayout());

        this.instance = instance;
        this.modsTab = modsTab;
        this.searchType = searchType;

        this.modCardsPanel = new JPanel(new GridLayout(0, 1, 0, 10)) {
            @Override
            public void scrollRectToVisible(Rectangle aRect) {

            }
        };
        this.modCardsPanel.setBorder(new EmptyBorder(0, 0, 0, 10));
        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.add(this.modCardsPanel, BorderLayout.PAGE_START);

        JScrollPane modCardsScrollPane = new JScrollPane(borderPanel);
        modCardsScrollPane.setUI(new FlatSmoothScrollPaneUI());
        modCardsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.add(modCardsScrollPane, BorderLayout.CENTER);
    }

    public void clear() {
        if (this.modCardsPanel.getComponentCount() == 0) {
            return;
        }

        this.modCardsPanel.removeAll();
        this.modCardsPanel.revalidate();
    }

    public SearchType getSearchType() {
        return this.searchType;
    }

    public CosmicInstance getInstance() {
        return this.instance;
    }

    public ModsTab getModsTab() {
        return this.modsTab;
    }

    public JPanel getModCardsPanel() {
        return this.modCardsPanel;
    }
}
