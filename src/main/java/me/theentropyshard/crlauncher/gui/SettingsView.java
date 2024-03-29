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

package me.theentropyshard.crlauncher.gui;

import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.CRLauncher;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SettingsView extends View {
    public SettingsView() {
        JPanel root = this.getRoot();
        root.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

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
        gbc.weighty = 1;
        root.add(themeSettings, gbc);
    }
}
