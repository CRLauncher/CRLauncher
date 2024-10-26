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

package me.theentropyshard.crlauncher.gui.view;

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.gui.Gui;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.utils.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.nio.file.Paths;
import java.util.Map;

public class SettingsView extends JPanel {

    public static final String THEME_BORDER = "gui.settingsView.themeSettings.borderName";
    public static final String THEME_DARK = "gui.settingsView.themeSettings.dark";
    public static final String THEME_LIGHT = "gui.settingsView.themeSettings.light";
    public static final String UI_BORDER = "gui.settingsView.ui.borderName";
    public static final String DIALOG_POSITION_LABEL = "gui.settingsView.ui.dialogPosition.label";
    public static final String RELATIVE_TO_PARENT = "gui.settingsView.ui.dialogPosition.options.relativeToParent";
    public static final String ALWAYS_CENTERED = "gui.settingsView.ui.dialogPosition.options.alwaysCentered";
    public static final String AMOUNT_OF_TIME = "gui.settingsView.ui.amountOfTime";
    public static final String OTHER_BORDER = "gui.settingsView.other.borderName";
    public static final String WRITE_PRETTY_JSON = "gui.settingsView.other.writePrettyJson";
    public static final String GAME_LAUNCH_LABEL = "gui.settingsView.other.onGameLaunch.label";
    public static final String LAUNCH_DO_NOTHING = "gui.settingsView.other.onGameLaunch.options.doNothing";
    public static final String LAUNCH_HIDE_LAUNCHER = "gui.settingsView.other.onGameLaunch.options.hideLauncher";
    public static final String LAUNCH_HIDE_LAUNCHER_AND_CONSOLE = "gui.settingsView.other.onGameLaunch.options.hideLauncherAndConsole";
    public static final String LAUNCH_EXIT_LAUNCHER = "gui.settingsView.other.onGameLaunch.options.exitLauncher";
    public static final String EXIT_LABEL = "gui.settingsView.other.onGameExit.label";
    public static final String EXIT_DO_NOTHING = "gui.settingsView.other.onGameExit.options.doNothing";
    public static final String EXIT_EXIT_LAUNCHER = "gui.settingsView.other.onGameExit.options.exitLauncher";
    public static final String CHECK_FOR_UPDATES = "gui.settingsView.other.checkForUpdatesAtStartup";
    public static final String CHECK_UPDATES_NOW_BUTTON = "gui.settingsView.other.checkUpdatesNowButton";
    public static final String CHECKING_UPDATES = "gui.settingsView.other.checkingForUpdates";
    public static final String APPEND_USERNAME = "gui.settingsView.other.appendUsername";
    public static final String LANGUAGE = "gui.settingsView.other.language";
    public static final String STORAGE_BORDER = "gui.settingsView.storageSettings.borderName";
    public static final String VERSIONS_PATH_LABEL = "gui.settingsView.storageSettings.versionsPathLabel";
    public static final String VERSIONS_PATH_PLACEHOLDER = "gui.settingsView.storageSettings.versionsPathFieldPlaceholder";
    public static final String INSTANCES_PATH_LABEL = "gui.settingsView.storageSettings.instancesPathLabel";
    public static final String INSTANCES_PATH_PLACEHOLDER = "gui.settingsView.storageSettings.instancesPathFieldPlaceholder";
    public static final String MOD_LOADERS_PATH_LABEL = "gui.settingsView.storageSettings.modLoadersPathLabel";
    public static final String MOD_LOADERS_PATH_PLACEHOLDER = "gui.settingsView.storageSettings.modLoadersPathFieldPlaceholder";
    public static final String TIP_LABEL = "gui.settingsView.storageSettings.tip";

    private final TitledBorder themeSettingsBorder;
    private final JRadioButton darkThemeButton;
    private final JRadioButton lightThemeButton;
    private final TitledBorder uiSettingsBorder;
    private final JLabel dialogPositionLabel;
    private final JCheckBox showAmountOfTime;
    private final TitledBorder storageSettingsBorder;
    private final JCheckBox versionsPathCheckbox;
    private final JTextField versionsPathField;
    private final JCheckBox instancesPathCheckbox;
    private final JTextField instancesPathField;
    private final JCheckBox modLoadersPathCheckbox;
    private final JTextField modLoadersPathField;
    private final JLabel tipLabel;
    private final TitledBorder otherSettingsBorder;
    private final JCheckBox prettyJson;
    private final JLabel launchOptionLabel;
    private final JLabel exitOptionLabel;
    private final JCheckBox checkUpdates;
    private final JButton checkUpdatesNowButton;
    private final JCheckBox appendUsername;
    private final JLabel languageLabel;
    private final JComboBox<String> whenLaunchesBehavior;
    private final JComboBox<String> whenExitsBehavior;
    private final JComboBox<String> position;

    public SettingsView() {
        this.setLayout(new GridBagLayout());

        Language language = CRLauncher.getInstance().getLanguage();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        {
            JPanel themeSettings = new JPanel(new GridLayout(0, 1));
            this.themeSettingsBorder = new TitledBorder(language.getString(SettingsView.THEME_BORDER));
            themeSettings.setBorder(this.themeSettingsBorder);
            this.darkThemeButton = new JRadioButton(language.getString(SettingsView.THEME_DARK));
            this.darkThemeButton.addActionListener(e -> {
                Gui gui = CRLauncher.getInstance().getGui();
                gui.setDarkTheme(true);
                gui.updateLookAndFeel();
                CRLauncher.getInstance().getSettings().darkTheme = true;
            });
            this.lightThemeButton = new JRadioButton(language.getString(SettingsView.THEME_LIGHT));
            this.lightThemeButton.addActionListener(e -> {
                Gui gui = CRLauncher.getInstance().getGui();
                gui.setDarkTheme(false);
                gui.updateLookAndFeel();
                CRLauncher.getInstance().getSettings().darkTheme = false;
            });
            themeSettings.add(this.darkThemeButton);
            themeSettings.add(this.lightThemeButton);
            Settings settings = CRLauncher.getInstance().getSettings();
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(this.darkThemeButton);
            buttonGroup.add(this.lightThemeButton);
            this.darkThemeButton.setSelected(settings.darkTheme);
            this.lightThemeButton.setSelected(!settings.darkTheme);


            gbc.gridy++;
            this.add(themeSettings, gbc);
        }

        {
            JPanel uiSettings = new JPanel(new GridLayout(2, 2));
            this.uiSettingsBorder = new TitledBorder(language.getString(SettingsView.UI_BORDER));
            uiSettings.setBorder(this.uiSettingsBorder);

            this.dialogPositionLabel = new JLabel(language.getString(SettingsView.DIALOG_POSITION_LABEL) + ": ");

            String relativeToParent = language.getString(SettingsView.RELATIVE_TO_PARENT);
            String alwaysCentered = language.getString(SettingsView.ALWAYS_CENTERED);
            this.position = new JComboBox<>(new String[]{
                relativeToParent,
                alwaysCentered
            });
            if (CRLauncher.getInstance().getSettings().dialogRelativeParent) {
                this.position.setSelectedIndex(0);
            } else {
                this.position.setSelectedIndex(1);
            }

            this.position.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String item = e.getItem().toString();

                    if (item.equals(relativeToParent)) {
                        CRLauncher.getInstance().getSettings().dialogRelativeParent = true;
                    } else if (item.equals(alwaysCentered)) {
                        CRLauncher.getInstance().getSettings().dialogRelativeParent = false;
                    } else {
                        throw new RuntimeException("Unreachable");
                    }
                }
            });

            uiSettings.add(this.dialogPositionLabel);
            uiSettings.add(this.position);

            this.showAmountOfTime = new JCheckBox(language.getString(SettingsView.AMOUNT_OF_TIME));
            this.showAmountOfTime.addActionListener(e -> {
                CRLauncher.getInstance().getSettings().showAmountOfTime = this.showAmountOfTime.isSelected();
            });
            this.showAmountOfTime.setSelected(CRLauncher.getInstance().getSettings().showAmountOfTime);
            uiSettings.add(this.showAmountOfTime);

            gbc.gridy++;
            this.add(uiSettings, gbc);
        }

        {
            Settings settings = CRLauncher.getInstance().getSettings();

            JPanel storageSettings = new JPanel(new BorderLayout());
            this.storageSettingsBorder = new TitledBorder(language.getString(SettingsView.STORAGE_BORDER));
            storageSettings.setBorder(this.storageSettingsBorder);

            JPanel pathsPanel = new JPanel(new GridLayout(3, 2));

            this.versionsPathCheckbox = new JCheckBox(language.getString(SettingsView.VERSIONS_PATH_LABEL) + ": ");
            this.versionsPathCheckbox.setSelected(settings.overrideVersionsPath);
            pathsPanel.add(this.versionsPathCheckbox);

            this.versionsPathField = new JTextField(settings.versionsDirPath == null ? "" : settings.versionsDirPath);
            this.versionsPathField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, language.getString(SettingsView.VERSIONS_PATH_PLACEHOLDER));
            this.versionsPathField.addActionListener(e -> {
                String path = SettingsView.checkPath(this.versionsPathField);

                if (path == null) {
                    return;
                }

                this.versionsPathField.setText(path);

                CRLauncher.getInstance().getSettings().overrideVersionsPath = this.versionsPathCheckbox.isSelected();
                CRLauncher.getInstance().getSettings().versionsDirPath = path;
            });
            pathsPanel.add(this.versionsPathField);

            this.instancesPathCheckbox = new JCheckBox(language.getString(SettingsView.INSTANCES_PATH_LABEL) + ": ");
            this.instancesPathCheckbox.setSelected(settings.overrideInstancesPath);
            pathsPanel.add(this.instancesPathCheckbox);

            this.instancesPathField = new JTextField(settings.instancesDirPath == null ? "" : settings.instancesDirPath);
            this.instancesPathField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, language.getString(SettingsView.INSTANCES_PATH_PLACEHOLDER));
            this.instancesPathField.addActionListener(e -> {
                String path = SettingsView.checkPath(this.instancesPathField);

                if (path == null) {
                    return;
                }

                this.instancesPathField.setText(path);

                CRLauncher.getInstance().getSettings().overrideInstancesPath = this.instancesPathCheckbox.isSelected();
                CRLauncher.getInstance().getSettings().instancesDirPath = path;
            });
            pathsPanel.add(this.instancesPathField);

            this.modLoadersPathCheckbox = new JCheckBox(language.getString(SettingsView.MOD_LOADERS_PATH_LABEL) + ": ");
            this.modLoadersPathCheckbox.setSelected(settings.overrideModloadersPath);
            pathsPanel.add(this.modLoadersPathCheckbox);

            this.modLoadersPathField = new JTextField(settings.modloadersDirPath == null ? "" : settings.modloadersDirPath);
            this.modLoadersPathField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, language.getString(SettingsView.MOD_LOADERS_PATH_PLACEHOLDER));
            this.modLoadersPathField.addActionListener(e -> {
                String path = SettingsView.checkPath(this.modLoadersPathField);

                if (path == null) {
                    return;
                }

                this.modLoadersPathField.setText(path);

                CRLauncher.getInstance().getSettings().overrideModloadersPath = this.modLoadersPathCheckbox.isSelected();
                CRLauncher.getInstance().getSettings().modloadersDirPath = path;
            });
            pathsPanel.add(this.modLoadersPathField);

            storageSettings.add(pathsPanel, BorderLayout.CENTER);

            this.tipLabel = new JLabel("<html><b>" + language.getString(SettingsView.TIP_LABEL) + "</b></html>");
            this.tipLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
            storageSettings.add(this.tipLabel, BorderLayout.SOUTH);

            this.versionsPathField.setEnabled(this.versionsPathCheckbox.isSelected());
            this.versionsPathCheckbox.addActionListener(e -> {
                boolean selected = this.versionsPathCheckbox.isSelected();
                this.versionsPathField.setEnabled(selected);
                if (selected) {
                    this.versionsPathField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "");
                } else {
                    this.versionsPathField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, language.getString(SettingsView.VERSIONS_PATH_PLACEHOLDER));
                }
            });

            this.instancesPathField.setEnabled(this.instancesPathCheckbox.isSelected());
            this.instancesPathCheckbox.addActionListener(e -> {
                boolean selected = this.instancesPathCheckbox.isSelected();
                this.instancesPathField.setEnabled(selected);
                if (selected) {
                    this.instancesPathField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "");
                } else {
                    this.instancesPathField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, language.getString(SettingsView.INSTANCES_PATH_PLACEHOLDER));
                }
            });

            this.modLoadersPathField.setEnabled(this.modLoadersPathCheckbox.isSelected());
            this.modLoadersPathCheckbox.addActionListener(e -> {
                boolean selected = this.modLoadersPathCheckbox.isSelected();
                this.modLoadersPathField.setEnabled(selected);
                if (selected) {
                    this.modLoadersPathField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "");
                } else {
                    this.modLoadersPathField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, language.getString(SettingsView.MOD_LOADERS_PATH_PLACEHOLDER));
                }
            });

            gbc.gridy++;
            this.add(storageSettings, gbc);
        }

        {
            JPanel otherSettings = new JPanel(new GridLayout(6, 3));
            this.otherSettingsBorder = new TitledBorder(language.getString(SettingsView.OTHER_BORDER));
            otherSettings.setBorder(this.otherSettingsBorder);

            this.prettyJson = new JCheckBox(language.getString(SettingsView.WRITE_PRETTY_JSON));
            this.prettyJson.addActionListener(e -> {
                CRLauncher.getInstance().getSettings().writePrettyJson = this.prettyJson.isSelected();
            });
            this.prettyJson.setSelected(CRLauncher.getInstance().getSettings().writePrettyJson);
            otherSettings.add(this.prettyJson);
            otherSettings.add(Box.createHorizontalGlue());

            this.launchOptionLabel = new JLabel(language.getString(SettingsView.GAME_LAUNCH_LABEL) + ": ");
            otherSettings.add(this.launchOptionLabel);
            String[] whenLaunchesOptions = {
                language.getString(SettingsView.LAUNCH_DO_NOTHING),
                language.getString(SettingsView.LAUNCH_HIDE_LAUNCHER),
                language.getString(SettingsView.LAUNCH_HIDE_LAUNCHER_AND_CONSOLE),
                language.getString(SettingsView.LAUNCH_EXIT_LAUNCHER)
            };
            this.whenLaunchesBehavior = new JComboBox<>(whenLaunchesOptions);
            this.whenLaunchesBehavior.addItemListener(e -> {
                if (e.getStateChange() != ItemEvent.SELECTED) {
                    return;
                }

                CRLauncher.getInstance().getSettings().whenCRLaunchesOption = this.whenLaunchesBehavior.getSelectedIndex();
            });
            int whenLaunchesIndex = CRLauncher.getInstance().getSettings().whenCRLaunchesOption;
            if (whenLaunchesIndex < 0 || whenLaunchesIndex >= whenLaunchesOptions.length) {
                whenLaunchesIndex = 0;
            }
            this.whenLaunchesBehavior.setSelectedIndex(whenLaunchesIndex);
            otherSettings.add(this.whenLaunchesBehavior);

            this.exitOptionLabel = new JLabel(language.getString(SettingsView.EXIT_LABEL) + ": ");
            otherSettings.add(this.exitOptionLabel);
            String[] whenExitsOptions = {
                language.getString(SettingsView.EXIT_DO_NOTHING),
                language.getString(SettingsView.EXIT_EXIT_LAUNCHER)
            };
            this.whenExitsBehavior = new JComboBox<>(whenExitsOptions);
            this.whenExitsBehavior.addItemListener(e -> {
                CRLauncher.getInstance().getSettings().whenCRExitsOption = this.whenExitsBehavior.getSelectedIndex();
            });
            int whenExitsIndex = CRLauncher.getInstance().getSettings().whenCRExitsOption;
            if (whenExitsIndex < 0 || whenExitsIndex >= whenExitsOptions.length) {
                whenExitsIndex = 0;
            }
            this.whenExitsBehavior.setSelectedIndex(whenExitsIndex);
            otherSettings.add(this.whenExitsBehavior);

            this.checkUpdates = new JCheckBox(language.getString(SettingsView.CHECK_FOR_UPDATES));
            this.checkUpdates.addActionListener(e -> {
                CRLauncher.getInstance().getSettings().checkUpdatesStartup = this.checkUpdates.isSelected();
            });
            this.checkUpdates.setSelected(CRLauncher.getInstance().getSettings().checkUpdatesStartup);
            otherSettings.add(this.checkUpdates);

            this.checkUpdatesNowButton = new JButton(language.getString(SettingsView.CHECK_UPDATES_NOW_BUTTON));
            this.checkUpdatesNowButton.addActionListener(e -> {
                this.checkUpdatesNowButton.setText(language.getString(SettingsView.CHECKING_UPDATES));

                new Worker<Void, Void>("checking for updates") {
                    @Override
                    protected Void work() throws Exception {
                        CRLauncher.checkForUpdates(true);

                        return null;
                    }

                    @Override
                    protected void done() {
                        SettingsView.this.checkUpdatesNowButton.setText(language.getString(SettingsView.CHECK_UPDATES_NOW_BUTTON));
                    }
                }.execute();
            });
            otherSettings.add(this.checkUpdatesNowButton);

            this.appendUsername = new JCheckBox(language.getString(SettingsView.APPEND_USERNAME));
            this.appendUsername.setSelected(CRLauncher.getInstance().getSettings().appendUsername);
            this.appendUsername.addActionListener(e -> {
                CRLauncher.getInstance().getSettings().appendUsername = this.appendUsername.isSelected();
            });
            otherSettings.add(this.appendUsername);

            otherSettings.add(Box.createHorizontalBox());

            this.languageLabel = new JLabel(language.getString(SettingsView.LANGUAGE));
            otherSettings.add(this.languageLabel);

            Map<String, Language> languages = CRLauncher.getInstance().getLanguages();
            JComboBox<Language> languageCombo = new JComboBox<>(languages.values().toArray(new Language[0]));
            languageCombo.setSelectedIndex(languages.values()
                .stream().map(Language::getName).toList().indexOf(CRLauncher.getInstance().getSettings().language));
            languageCombo.addItemListener(e -> {
                if (e.getStateChange() != ItemEvent.SELECTED) {
                    return;
                }

                CRLauncher.getInstance().getSettings().language = ((Language) e.getItem()).getName();
                CRLauncher.getInstance().getGui().reloadLanguage();
            });
            otherSettings.add(languageCombo);

            gbc.gridy++;
            gbc.weighty = 1;
            this.add(otherSettings, gbc);
        }
    }

    private static String checkPath(JTextField textField) {
        String path = textField.getText();
        boolean pathInvalid = FileUtils.isPathInvalid(path);

        if (pathInvalid) {
            textField.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);

            MessageBox.showErrorMessage(
                CRLauncher.frame, "Supplied path is invalid: " + path
            );

            return null;
        } else {
            textField.putClientProperty(FlatClientProperties.OUTLINE, "");
        }

        return Paths.get(path).toAbsolutePath().toString();
    }

    public void reloadLanguage() {
        Language language = CRLauncher.getInstance().getLanguage();

        this.themeSettingsBorder.setTitle(language.getString(SettingsView.THEME_BORDER));
        this.darkThemeButton.setText(language.getString(SettingsView.THEME_DARK));
        this.lightThemeButton.setText(language.getString(SettingsView.THEME_LIGHT));
        this.uiSettingsBorder.setTitle(language.getString(SettingsView.UI_BORDER));

        this.dialogPositionLabel.setText(language.getString(SettingsView.DIALOG_POSITION_LABEL));

        String relativeToParent = language.getString(SettingsView.RELATIVE_TO_PARENT);
        String alwaysCentered = language.getString(SettingsView.ALWAYS_CENTERED);
        DefaultComboBoxModel<String> positionModel = new DefaultComboBoxModel<>(
            new String[]{
                relativeToParent,
                alwaysCentered
            }
        );
        int positionIndex = this.position.getSelectedIndex();
        this.position.setModel(positionModel);
        this.position.setSelectedIndex(positionIndex);

        this.showAmountOfTime.setText(language.getString(SettingsView.AMOUNT_OF_TIME));
        this.otherSettingsBorder.setTitle(language.getString(SettingsView.OTHER_BORDER));
        this.prettyJson.setText(language.getString(SettingsView.WRITE_PRETTY_JSON));

        this.storageSettingsBorder.setTitle(language.getString(SettingsView.STORAGE_BORDER));
        this.versionsPathCheckbox.setText(language.getString(SettingsView.VERSIONS_PATH_LABEL) + ": ");
        this.versionsPathField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, language.getString(SettingsView.VERSIONS_PATH_PLACEHOLDER));
        this.instancesPathCheckbox.setText(language.getString(SettingsView.INSTANCES_PATH_LABEL) + ": ");
        this.instancesPathField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, language.getString(SettingsView.INSTANCES_PATH_PLACEHOLDER));
        this.modLoadersPathCheckbox.setText(language.getString(SettingsView.MOD_LOADERS_PATH_LABEL) + ": ");
        this.modLoadersPathField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, language.getString(SettingsView.MOD_LOADERS_PATH_PLACEHOLDER));
        this.tipLabel.setText("<html><b>" + language.getString(SettingsView.TIP_LABEL) + "</b></html>");

        this.launchOptionLabel.setText(language.getString(SettingsView.GAME_LAUNCH_LABEL));
        DefaultComboBoxModel<String> launchModel = new DefaultComboBoxModel<>(
            new String[]{
                language.getString(SettingsView.LAUNCH_DO_NOTHING),
                language.getString(SettingsView.LAUNCH_HIDE_LAUNCHER),
                language.getString(SettingsView.LAUNCH_HIDE_LAUNCHER_AND_CONSOLE),
                language.getString(SettingsView.LAUNCH_EXIT_LAUNCHER)
            }
        );
        int launchIndex = this.whenLaunchesBehavior.getSelectedIndex();
        this.whenLaunchesBehavior.setModel(launchModel);
        this.whenLaunchesBehavior.setSelectedIndex(launchIndex);

        this.exitOptionLabel.setText(language.getString(SettingsView.EXIT_LABEL));
        DefaultComboBoxModel<String> exitModel = new DefaultComboBoxModel<>(
            new String[]{
                language.getString(SettingsView.EXIT_DO_NOTHING),
                language.getString(SettingsView.EXIT_EXIT_LAUNCHER)
            }
        );
        int exitIndex = this.whenExitsBehavior.getSelectedIndex();
        this.whenExitsBehavior.setModel(exitModel);
        this.whenExitsBehavior.setSelectedIndex(exitIndex);

        this.checkUpdates.setText(language.getString(SettingsView.CHECK_FOR_UPDATES));
        this.checkUpdatesNowButton.setText(language.getString(SettingsView.CHECK_UPDATES_NOW_BUTTON));
        this.appendUsername.setText(language.getString(SettingsView.APPEND_USERNAME));
        this.languageLabel.setText(language.getString(SettingsView.LANGUAGE));
    }
}
