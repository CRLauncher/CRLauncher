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

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.gui.Gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
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
    public static final String LANGUAGE = "gui.settingsView.other.language";

    private final TitledBorder themeSettingsBorder;
    private final JRadioButton darkThemeButton;
    private final JRadioButton lightThemeButton;
    private final TitledBorder uiSettingsBorder;
    private final JLabel dialogPositionLabel;
    private final JCheckBox showAmountOfTime;
    private final TitledBorder otherSettingsBorder;
    private final JCheckBox prettyJson;
    private final JLabel launchOptionLabel;
    private final JLabel exitOptionLabel;
    private final JCheckBox checkUpdates;
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
            JPanel otherSettings = new JPanel(new GridLayout(5, 3));
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

        this.launchOptionLabel.setText(language.getString(SettingsView.GAME_LAUNCH_LABEL));
        DefaultComboBoxModel<String> launchModel = new DefaultComboBoxModel<>(
            new String[] {
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
        this.languageLabel.setText(language.getString(SettingsView.LANGUAGE));
    }
}
