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

package me.theentropyshard.crlauncher.gui.dialogs.crmm;

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.crmm.CrmmApi;
import me.theentropyshard.crlauncher.crmm.model.Mod;
import me.theentropyshard.crlauncher.crmm.model.SearchModsResponse;
import me.theentropyshard.crlauncher.gui.dialogs.AppDialog;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SearchCrmmModsDialog extends AppDialog {

    private final JPanel modCardsPanel;

    public SearchCrmmModsDialog(Instance instance) {
        super(CRLauncher.frame, "Search mods on CRMM");

        JPanel root = new JPanel(new BorderLayout());
        root.setPreferredSize(new Dimension(800, 600));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());

        JTextField searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search mods...");
        topPanel.add(searchField, BorderLayout.CENTER);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> {
            new Worker<List<Mod>, Void>("searching mods") {
                @Override
                protected List<Mod> work() throws Exception {
                    CrmmApi crmmApi = CRLauncher.getInstance().getCrmmApi();
                    SearchModsResponse searchModsResponse = crmmApi.searchMods(searchField.getText());

                    return searchModsResponse.getMods();
                }

                @Override
                protected void done() {
                    SearchCrmmModsDialog.this.modCardsPanel.removeAll();

                    List<Mod> mods = null;
                    try {
                        mods = this.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        Log.error(ex);
                    }

                    if (mods == null) {
                        return;
                    }

                    for (Mod mod : mods) {
                        SearchCrmmModsDialog.this.modCardsPanel.add(new ModCard(mod));
                    }

                    SearchCrmmModsDialog.this.modCardsPanel.revalidate();
                }
            }.execute();
        });
        topPanel.add(searchButton, BorderLayout.EAST);

        root.add(topPanel, BorderLayout.NORTH);

        this.modCardsPanel = new JPanel(new GridLayout(0, 1, 0, 1));
        this.modCardsPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.add(this.modCardsPanel, BorderLayout.PAGE_START);

        JScrollPane modCardsScrollPane = new JScrollPane(borderPanel);
        root.add(modCardsScrollPane, BorderLayout.CENTER);

        this.setContent(root);
        this.center(0);
        this.setVisible(true);
    }
}
