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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.main;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatScrollPaneBorder;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.cosmic.version.CosmicArchiveVersion;
import me.theentropyshard.crlauncher.cosmic.version.Version;
import me.theentropyshard.crlauncher.cosmic.version.VersionManager;
import me.theentropyshard.crlauncher.gui.FlatSmoothScrollPaneUI;
import me.theentropyshard.crlauncher.gui.dialogs.addinstance.AddInstanceDialog;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.Tab;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.java.JavaTab;
import me.theentropyshard.crlauncher.gui.utils.*;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.language.LanguageSection;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

public class MainTab extends Tab {
    private final JComboBox<Version> versionsCombo;
    private final JTextField windowTitleField;
    private final String oldWindowTitle;
    private final JTextField widthField;
    private final JTextField heightField;
    private final JTextArea envVarsArea;

    private List<Version> versions;
    private Version previousValue;

    public MainTab(CosmicInstance instance, JDialog dialog) {
        super(CRLauncher.getInstance().getLanguage()
            .getString("gui.instanceSettingsDialog.mainTab.name"), instance, dialog);

        JPanel root = this.getRoot();
        root.setLayout(new BorderLayout());

        JPanel panel = new ScrollablePanel(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(
            panel,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setUI(new FlatSmoothScrollPaneUI());
        scrollPane.setHorizontalScrollBar(null);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        root.add(scrollPane, BorderLayout.CENTER);

        Language language = CRLauncher.getInstance().getLanguage();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        {
            JPanel crVersionSettings = new JPanel(new GridLayout(0, 1));
            crVersionSettings.setBorder(new TitledBorder(
                language.getString("gui.instanceSettingsDialog.mainTab.cosmicReachVersion.borderName")
            ));

            JPanel borderPanel = new JPanel(new BorderLayout());

            this.versionsCombo = new JComboBox<>();
            this.versionsCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                    if (value instanceof Version version) {
                        this.setText(version.getId());
                    }

                    return c;
                }
            });
            this.versionsCombo.addItemListener(e -> {
                if (e.getStateChange() != ItemEvent.SELECTED) {
                    return;
                }

                Version version = (Version) e.getItem();

                if (version instanceof CosmicArchiveVersion caVersion && caVersion.getClient() == null) {
                    MessageBox.showErrorMessage(this.getDialog(),
                        CRLauncher.getInstance().getLanguage().getString(AddInstanceDialog.NO_CLIENT_MESSAGE)
                            .replace("$$CR_VERSION$$", version.getId()));

                    if (this.previousValue != null) {
                        this.versionsCombo.setSelectedItem(this.previousValue);
                    }
                } else {
                    instance.setCosmicVersion(version.getId());
                    this.previousValue = version;
                }
            });
            borderPanel.add(this.versionsCombo, BorderLayout.CENTER);
            JButton refreshButton = new JButton(language.getString("gui.addInstanceDialog.refreshButton"));
            refreshButton.addActionListener(e -> {
                refreshButton.setEnabled(false);

                this.versionsCombo.removeAllItems();

                new VersionsLoadWorker(instance, this, () -> {
                    refreshButton.setEnabled(true);
                }, true).execute();
            });
            borderPanel.add(refreshButton, BorderLayout.EAST);

            crVersionSettings.add(borderPanel);

            JCheckBox updateToLatestAutomatically = new JCheckBox(
                language.getString("gui.instanceSettingsDialog.mainTab.cosmicReachVersion.autoUpdateToLatest")
            );
            updateToLatestAutomatically.setSelected(instance.isAutoUpdateToLatest());
            updateToLatestAutomatically.addActionListener(e -> {
                instance.setAutoUpdateToLatest(!instance.isAutoUpdateToLatest());
            });
            crVersionSettings.add(updateToLatestAutomatically);

            JCheckBox showOnlyInstalled = new JCheckBox(language.getString("gui.addInstanceDialog.showOnlyInstalled"));
            showOnlyInstalled.setSelected(CRLauncher.getInstance().getSettings().showOnlyInstalledVersions);
            showOnlyInstalled.addActionListener(e -> {
                CRLauncher launcher = CRLauncher.getInstance();
                VersionManager versionManager = launcher.getVersionManager();
                Settings settings = launcher.getSettings();

                settings.showOnlyInstalledVersions = showOnlyInstalled.isSelected();

                SwingUtils.startWorker(() -> {
                    Vector<Version> data = new Vector<>(this.versions);
                    data.removeIf(version -> (!versionManager.isInstalled(version) && settings.showOnlyInstalledVersions));

                    SwingUtilities.invokeLater(() -> this.versionsCombo.setModel(new DefaultComboBoxModel<>(data)));
                });
            });
            crVersionSettings.add(showOnlyInstalled);

            gbc.gridy++;
            panel.add(crVersionSettings, gbc);
        }

        {
            this.oldWindowTitle = instance.getCustomWindowTitle();

            LanguageSection section = language.getSection("gui.instanceSettingsDialog.mainTab.windowSettings");

            JPanel windowSettings = new JPanel(new GridLayout(6, 2));
            windowSettings.setBorder(new TitledBorder(section.getString("borderName")));

            JLabel windowTitleLabel = new JLabel(section.getString("customTitle.label") + ": ");
            windowSettings.add(windowTitleLabel);

            this.windowTitleField = new JTextField(this.oldWindowTitle);
            this.windowTitleField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, section.getString("customTitle.textFieldPlaceholder"));
            this.windowTitleField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
                instance.setCustomWindowTitle(this.windowTitleField.getText());
            }));

            windowSettings.add(this.windowTitleField);

            ButtonGroup buttonGroup = new ButtonGroup();

            JRadioButton startFullScreen = new JRadioButton(section.getString("startup.fullscreen"));
            windowSettings.add(startFullScreen);
            windowSettings.add(Box.createHorizontalGlue());

            JRadioButton startMaximized = new JRadioButton(section.getString("startup.maximized"));
            windowSettings.add(startMaximized);
            windowSettings.add(Box.createHorizontalGlue());

            JRadioButton customSize = new JRadioButton(section.getString("startup.customSize.radioButton"));
            windowSettings.add(customSize);
            windowSettings.add(Box.createHorizontalGlue());

            JLabel widthLabel = new JLabel(section.getString("startup.customSize.width") + ": ");
            windowSettings.add(widthLabel);

            this.widthField = new JTextField(String.valueOf(
                instance.getCosmicWindowWidth() <= 0 ? 1024 : instance.getCosmicWindowWidth()
            ));
            this.widthField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
                String text = this.widthField.getText();

                if (text.trim().isEmpty()) {
                    return;
                }

                this.widthField.putClientProperty(FlatClientProperties.OUTLINE, null);
                instance.setCosmicWindowWidth(Integer.parseInt(text));
            }));
            ((AbstractDocument) this.widthField.getDocument()).setDocumentFilter(new IntegerDocumentFilter(wrongInput -> {}, true));
            windowSettings.add(this.widthField);

            JLabel heightLabel = new JLabel(section.getString("startup.customSize.height") + ": ");
            windowSettings.add(heightLabel);

            this.heightField = new JTextField(String.valueOf(
                instance.getCosmicWindowHeight() <= 0 ? 576 : instance.getCosmicWindowHeight()
            ));
            this.heightField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
                String text = this.heightField.getText();

                if (text.trim().isEmpty()) {
                    return;
                }

                this.heightField.putClientProperty(FlatClientProperties.OUTLINE, null);
                instance.setCosmicWindowHeight(Integer.parseInt(text));
            }));
            ((AbstractDocument) this.heightField.getDocument()).setDocumentFilter(new IntegerDocumentFilter(wrongInput -> {}, true));
            windowSettings.add(this.heightField);

            buttonGroup.add(startFullScreen);
            buttonGroup.add(startMaximized);
            buttonGroup.add(customSize);

            startFullScreen.addActionListener(e -> {
                this.toggleFields(false);
                instance.setFullscreen(true);
                instance.setMaximized(false);
            });
            startFullScreen.setSelected(instance.isFullscreen());

            startMaximized.addActionListener(e -> {
                this.toggleFields(false);
                instance.setFullscreen(false);
                instance.setMaximized(true);
            });
            startMaximized.setSelected(instance.isMaximized());

            customSize.addActionListener(e -> {
                this.toggleFields(true);
                instance.setFullscreen(false);
                instance.setMaximized(false);
            });

            boolean currentlySelected = !instance.isFullscreen() && !instance.isMaximized();
            this.toggleFields(currentlySelected);
            customSize.setSelected(currentlySelected);

            gbc.gridy++;
            panel.add(windowSettings, gbc);
        }

        {
            JPanel envVarsPanel = new JPanel(new BorderLayout());
            envVarsPanel.setBorder(new TitledBorder(CRLauncher.getInstance().getLanguage().getString("gui.instanceSettingsDialog.mainTab.environmentVariables.borderName")));

            String text = instance.getEnvironmentVariables().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(";"));

            this.envVarsArea = new JTextArea(text);
            this.envVarsArea.setFont(JavaTab.MONOSPACED);
            this.envVarsArea.setLineWrap(true);
            this.envVarsArea.setWrapStyleWord(true);

            JScrollPane envVarsScrollPane = new JScrollPane(
                this.envVarsArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            );
            envVarsScrollPane.setUI(new FlatSmoothScrollPaneUI());
            envVarsScrollPane.setPreferredSize(new Dimension(0, 250));
            envVarsScrollPane.setMaximumSize(new Dimension(1000, 250));
            envVarsScrollPane.setBorder(new FlatScrollPaneBorder());

            envVarsPanel.add(envVarsScrollPane, BorderLayout.CENTER);

            gbc.gridy++;
            gbc.weighty = 1;
            panel.add(envVarsPanel, gbc);
        }

        this.getDialog().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (MainTab.this.onWindowClose(instance)) {
                    MainTab.this.getDialog().dispose();
                }
            }
        });

        new VersionsLoadWorker(instance, this).execute();
    }

    private void saveEnvVars(CosmicInstance instance) {
        Map<String, String> envVars = instance.getEnvironmentVariables();
        envVars.clear();

        String[] pairs = this.envVarsArea.getText().split(";");

        for (String pair : pairs) {
            pair = pair.trim();

            if (pair.isEmpty()) {
                continue;
            }

            String[] nameValue = pair.split("=");

            if (nameValue.length != 2) {
                continue;
            }

            envVars.put(nameValue[0], nameValue[1]);
        }
    }

    private boolean checkFieldsValidity() {
        boolean widthEmpty = false;
        boolean heightEmpty = false;

        if (MainTab.this.widthField.getText().trim().isEmpty()) {
            MainTab.this.widthField.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
            widthEmpty = true;
        }

        if (MainTab.this.heightField.getText().trim().isEmpty()) {
            MainTab.this.heightField.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
            heightEmpty = true;
        }

        return !widthEmpty && !heightEmpty;
    }

    private boolean onWindowClose(CosmicInstance instance) {
        this.saveEnvVars(instance);

        return this.checkFieldsValidity();
    }

    private void toggleFields(boolean enabled) {
        this.widthField.setEnabled(enabled);
        this.heightField.setEnabled(enabled);
    }

    @Override
    public void shown() {
        this.getDialog().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    @Override
    public void hidden() {
        this.getDialog().setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public List<Version> getVersions() {
        return this.versions;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }

    public JComboBox<Version> getVersionsCombo() {
        return this.versionsCombo;
    }

    public Version getPreviousValue() {
        return this.previousValue;
    }

    public void setPreviousValue(Version previousValue) {
        this.previousValue = previousValue;
    }
}
