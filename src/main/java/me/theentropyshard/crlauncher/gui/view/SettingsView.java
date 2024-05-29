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
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.gui.Gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;

public class SettingsView extends JPanel {
    public SettingsView() {
        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        {
            JPanel themeSettings = new JPanel(new GridLayout(0, 1));
            themeSettings.setBorder(new TitledBorder("Theme"));
            JRadioButton darkThemeButton = new JRadioButton("Dark");
            darkThemeButton.addActionListener(e -> {
                Gui gui = CRLauncher.getInstance().getGui();
                gui.setDarkTheme(true);
                gui.updateLookAndFeel();
                CRLauncher.getInstance().getSettings().darkTheme = true;
            });
            JRadioButton lightThemeButton = new JRadioButton("Light");
            lightThemeButton.addActionListener(e -> {
                Gui gui = CRLauncher.getInstance().getGui();
                gui.setDarkTheme(false);
                gui.updateLookAndFeel();
                CRLauncher.getInstance().getSettings().darkTheme = false;
            });
            themeSettings.add(darkThemeButton);
            themeSettings.add(lightThemeButton);
            Settings settings = CRLauncher.getInstance().getSettings();
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(darkThemeButton);
            buttonGroup.add(lightThemeButton);
            darkThemeButton.setSelected(settings.darkTheme);
            lightThemeButton.setSelected(!settings.darkTheme);


            gbc.gridy++;
            this.add(themeSettings, gbc);
        }

        {
            JPanel uiSettings = new JPanel(new GridLayout(2, 2));
            uiSettings.setBorder(new TitledBorder("UI"));

            JLabel dialogPosition = new JLabel("Dialog position: ");

            JComboBox<String> position = new JComboBox<>(new String[]{"Relative to parent", "Always centered"});
            if (CRLauncher.getInstance().getSettings().dialogRelativeToParent) {
                position.setSelectedIndex(0);
            } else {
                position.setSelectedIndex(1);
            }

            position.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Settings settings = CRLauncher.getInstance().getSettings();

                    switch (e.getItem().toString()) {
                        case "Relative to parent" -> settings.dialogRelativeToParent = true;
                        case "Always centered" -> settings.dialogRelativeToParent = false;
                        default -> throw new RuntimeException("Unreachable");
                    }
                }
            });

            uiSettings.add(dialogPosition);
            uiSettings.add(position);

            JCheckBox showConsole = new JCheckBox("Show console at startup");
            showConsole.setSelected(CRLauncher.getInstance().getSettings().showConsoleAtStartup);
            showConsole.addActionListener(e -> {
                Settings settings = CRLauncher.getInstance().getSettings();
                settings.showConsoleAtStartup = !settings.showConsoleAtStartup;
            });
            uiSettings.add(showConsole);

            gbc.gridy++;
            gbc.weighty = 1;
            this.add(uiSettings, gbc);
        }
    }
}
