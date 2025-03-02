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

package me.theentropyshard.crlauncher.gui.view.crmm;

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.crmm.ModInfo;
import me.theentropyshard.crlauncher.crmm.filter.ShowPerPage;
import me.theentropyshard.crlauncher.crmm.filter.SortBy;
import me.theentropyshard.crlauncher.crmm.model.mod.CrmmMod;
import me.theentropyshard.crlauncher.crmm.model.mod.SearchModsResponse;
import me.theentropyshard.crlauncher.crmm.model.mod.SearchType;
import me.theentropyshard.crlauncher.crmm.model.project.Project;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectResponse;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTab;
import me.theentropyshard.crlauncher.gui.utils.MouseClickListener;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.gui.view.crmm.modview.CrmmModView;
import me.theentropyshard.crlauncher.gui.view.crmm.modview.ModDownloadWorkerSupplier;
import me.theentropyshard.crlauncher.gui.view.crmm.navbar.NavBar;
import me.theentropyshard.crlauncher.gui.view.crmm.pagination.PageButton;
import me.theentropyshard.crlauncher.gui.view.crmm.pagination.PaginationPanel;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.language.LanguageSection;
import me.theentropyshard.crlauncher.logging.Log;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class CrmmModsView extends JPanel {
    private final CosmicInstance instance;
    private final ModsTab modsTab;

    private final NavBar navBar;
    private final JTextField searchField;
    private final JComboBox<SortBy> searchTypeCombo;
    private final JComboBox<ShowPerPage> showPerPageCombo;
    private final CardLayout cardLayout;
    private final JPanel modsViewsPanel;
    private final PaginationPanel paginationPanel;

    private final SearchCrmmModsView modsModsView;
    private final SearchCrmmModsView dataModsModsView;
    private final SearchCrmmModsView resourcePacksModsView;
    private final SearchCrmmModsView shadersModsView;
    private final SearchCrmmModsView modpacksView;

    private SortBy sortBy;
    private ShowPerPage showPerPage;

    private int tab;
    private int page = 1;
    private int totalPages;

    public CrmmModsView(CosmicInstance instance, ModsTab modsTab) {
        super(new BorderLayout());

        this.instance = instance;
        this.modsTab = modsTab;

        this.sortBy = SortBy.RELEVANCE;
        this.showPerPage = ShowPerPage.TWENTY;

        LanguageSection crmmDialogSection = CRLauncher.getInstance().getLanguage().getSection("gui.searchCRMMModsDialog");

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 5));

        LanguageSection navbarSection = crmmDialogSection.getSection("navbar");

        this.navBar = new NavBar();
        this.navBar.addItem(navbarSection.getString("mods"));
        this.navBar.addItem(navbarSection.getString("datamods"));
        this.navBar.addItem(navbarSection.getString("resourcePacks"));
        this.navBar.addItem(navbarSection.getString("shaders"));
        this.navBar.addItem(navbarSection.getString("modpacks"));
        topPanel.add(this.navBar);

        LanguageSection searchSection = crmmDialogSection.getSection("search");

        JPanel searchPanel = new JPanel(new MigLayout("nogrid, fillx, insets 2 2 2 2", "[fill][fill][fill][fill][fill]", "[]"));

        this.searchField = new JTextField();

        LanguageSection inputPlaceholderSection = searchSection.getSection("inputPlaceholder");
        this.searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, inputPlaceholderSection.getString("searchMods"));
        searchPanel.add(this.searchField, "grow");

        LanguageSection sortBySection = searchSection.getSection("sortBy");
        LanguageSection sortByOptions = sortBySection.getSection("options");

        JLabel sortByLabel = new JLabel(sortBySection.getString("label"));
        searchPanel.add(sortByLabel);

        this.searchTypeCombo = new JComboBox<>(SortBy.values());
        this.searchTypeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof SortBy sort && c instanceof JLabel label) {
                    label.setText(sortByOptions.getString(sort.getValue()));
                } else {
                    Log.warn("Got something unexpected: " + value.getClass() + ", " + c.getClass());
                }

                return c;
            }
        });
        searchPanel.add(this.searchTypeCombo);

        LanguageSection showPerPageSection = searchSection.getSection("showPerPage");
        LanguageSection showPerPageOptions = showPerPageSection.getSection("options");

        JLabel showPerPageLabel = new JLabel(showPerPageSection.getString("label"));
        searchPanel.add(showPerPageLabel);

        this.showPerPageCombo = new JComboBox<>(ShowPerPage.values());
        this.showPerPageCombo.setSelectedIndex(ShowPerPage.TWENTY.ordinal());
        this.showPerPageCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof ShowPerPage s && c instanceof JLabel label) {
                    label.setText(showPerPageOptions.getString(s.getKey()));
                } else {
                    Log.warn("Got something unexpected: " + value.getClass() + ", " + c.getClass());
                }

                return c;
            }
        });
        searchPanel.add(this.showPerPageCombo);

        topPanel.add(searchPanel);

        this.add(topPanel, BorderLayout.NORTH);

        this.cardLayout = new CardLayout();
        this.modsViewsPanel = new JPanel(this.cardLayout);
        this.modsViewsPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        this.modsModsView = new SearchCrmmModsView(instance, modsTab, SearchType.MOD);
        this.modsViewsPanel.add(this.modsModsView, "0");

        this.dataModsModsView = new SearchCrmmModsView(instance, modsTab, SearchType.DATAMOD);
        this.modsViewsPanel.add(this.dataModsModsView, "1");

        this.resourcePacksModsView = new SearchCrmmModsView(instance, modsTab, SearchType.RESOURCE_PACK);
        this.modsViewsPanel.add(this.resourcePacksModsView, "2");

        this.shadersModsView = new SearchCrmmModsView(instance, modsTab, SearchType.SHADER);
        this.modsViewsPanel.add(this.shadersModsView, "3");

        this.modpacksView = new SearchCrmmModsView(instance, modsTab, SearchType.MODPACK);
        this.modsViewsPanel.add(this.modpacksView, "4");

        this.search();

        String[] placeHolders = {
            inputPlaceholderSection.getString("searchMods"),
            inputPlaceholderSection.getString("searchDatamods"),
            inputPlaceholderSection.getString("searchResourcePacks"),
            inputPlaceholderSection.getString("searchShaders"),
            inputPlaceholderSection.getString("searchModpacks")
        };

        this.navBar.addTabListener(tab -> {
            this.tab = tab;
            this.searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeHolders[tab]);
            this.search();
            this.cardLayout.show(this.modsViewsPanel, String.valueOf(tab));
        });

        this.searchTypeCombo.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }

            this.sortBy = (SortBy) this.searchTypeCombo.getSelectedItem();

            this.search();
        });

        this.showPerPageCombo.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }

            this.showPerPage = (ShowPerPage) this.showPerPageCombo.getSelectedItem();

            this.search();
        });

        this.searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                CrmmModsView.this.search();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                CrmmModsView.this.search();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        this.add(this.modsViewsPanel, BorderLayout.CENTER);

        this.paginationPanel = new PaginationPanel();
        this.paginationPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        this.paginationPanel.addPageSelectedListener(page -> {
            this.page = page;
            this.search();
        });

        this.paginationPanel.addPrevPageButtonListener(e -> {
            this.page = Math.max(1, this.page - 1);
            this.search();
        });

        this.paginationPanel.addNextPageButtonListener(e -> {
            this.page = Math.min(this.totalPages, this.page + 1);
            this.search();
        });

        this.add(this.paginationPanel, BorderLayout.SOUTH);
    }

    private void search() {
        SearchCrmmModsView searchModsView = (SearchCrmmModsView) this.modsViewsPanel.getComponent(this.tab);

        new Worker<List<CrmmMod>, Void>("searching mods") {
            @Override
            protected List<CrmmMod> work() {
                SearchModsResponse response = CRLauncher.getInstance().getCrmmApi().search(
                    searchModsView.getSearchType(),
                    CrmmModsView.this.sortBy,
                    CrmmModsView.this.showPerPage,
                    CrmmModsView.this.searchField.getText(),
                    CrmmModsView.this.page
                );

                int hits = response.getEstimatedTotalHits();
                int showPerPage = Integer.parseInt(CrmmModsView.this.showPerPage.getValue());
                CrmmModsView.this.totalPages = (hits / showPerPage) + ((hits % showPerPage) == 0 ? 0 : 1);

                if (CrmmModsView.this.page > CrmmModsView.this.totalPages) {
                    CrmmModsView.this.page = 1;
                }

                SwingUtilities.invokeLater(() -> {
                    CrmmModsView.this.paginationPanel.clearAll();
                    for (int i = 0; i < CrmmModsView.this.totalPages; i++) {
                        CrmmModsView.this.paginationPanel.addButton(new PageButton(String.valueOf(i + 1), i + 1));
                    }
                    CrmmModsView.this.paginationPanel.selectPage(CrmmModsView.this.page);
                });


                return response.getMods();
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                searchModsView.clear();

                List<CrmmMod> crmmMods = null;

                try {
                    crmmMods = this.get();
                } catch (InterruptedException | ExecutionException ex) {
                    Log.error(ex);
                }

                if (crmmMods == null) {
                    return;
                }

                for (CrmmMod crmmMod : crmmMods) {
                    ModInfo modInfo = crmmMod.toModInfo();

                    ModCard card = new ModCard(modInfo);

                    card.addMouseListener(new MouseClickListener(e -> {
                        new Worker<Project, Void>("fetching project") {
                            private final AtomicBoolean showDialog = new AtomicBoolean(true);

                            @Override
                            protected Project work() {
                                ProjectResponse projectResponse = CRLauncher.getInstance().getCrmmApi().getProject(modInfo.getSlug());

                                if (!projectResponse.isSuccess()) {
                                    return null;
                                }

                                Project project = projectResponse.getProject();

                                if (e.getButton() == MouseEvent.BUTTON3) {
                                    this.showDialog.set(false);

                                    CrmmModView.downloadLatestModVersion(project, CrmmModsView.this.instance,
                                        CrmmModsView.this.modsTab.getModsView().getModsTableModel());
                                }

                                return project;
                            }

                            @Override
                            protected void done() {
                                Project project;

                                try {
                                    project = this.get();
                                } catch (InterruptedException | ExecutionException ex) {
                                    Log.error("Unexpected error");

                                    return;
                                }

                                if (project == null) {
                                    Log.warn("Got null project for mod " + modInfo.getName());

                                    return;
                                }

                                if (this.showDialog.get()) {
                                    new ModViewDialog(project, CrmmModsView.this.instance, CrmmModsView.this.modsTab,
                                        new ModDownloadWorkerSupplier());
                                }
                            }
                        }.execute();
                    }));

                    searchModsView.getModCardsPanel().add(card);
                }
            }
        }.execute();
    }
}
