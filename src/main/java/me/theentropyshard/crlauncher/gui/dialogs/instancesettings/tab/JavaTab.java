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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab;

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.instance.Instance;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class JavaTab extends Tab {
    public JavaTab(Instance instance, JDialog dialog) {
        super(CRLauncher.getInstance().getLanguage()
            .getString("gui.instanceSettingsDialog.javaTab.name"), instance, dialog);

        JPanel root = this.getRoot();
        root.setLayout(new GridBagLayout());

        Language language = CRLauncher.getInstance().getLanguage();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        JPanel javaInstallation = new JPanel(new GridLayout(0, 1));
        JTextField javaPathTextField = new JTextField();
        javaPathTextField.setText(instance.getJavaPath());
        javaPathTextField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
            language.getString("gui.instanceSettingsDialog.javaTab.javaInstallation.textFieldPlaceholder"));
        javaInstallation.add(javaPathTextField);
        javaInstallation.setBorder(new TitledBorder(
            language.getString("gui.instanceSettingsDialog.javaTab.javaInstallation.borderName")
        ));

        JPanel memorySettings = new JPanel(new GridLayout(2, 2));
        JLabel minMemoryLabel = new JLabel(
            language.getString("gui.instanceSettingsDialog.javaTab.memorySettings.minimum") +
                ":");
        JLabel maxMemoryLabel = new JLabel(
            language.getString("gui.instanceSettingsDialog.javaTab.memorySettings.maximum") +
            ":");
        JTextField minMemoryField = new JTextField();
        minMemoryField.setText(String.valueOf(instance.getMinimumMemoryInMegabytes()));
        minMemoryField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "512");
        JTextField maxMemoryField = new JTextField();
        maxMemoryField.setText(String.valueOf(instance.getMaximumMemoryInMegabytes()));
        maxMemoryField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "2048");
        memorySettings.add(minMemoryLabel);
        memorySettings.add(minMemoryField);
        memorySettings.add(maxMemoryLabel);
        memorySettings.add(maxMemoryField);
        memorySettings.setBorder(new TitledBorder(
            language.getString("gui.instanceSettingsDialog.javaTab.memorySettings.borderName")
        ));

        gbc.gridy++;
        root.add(javaInstallation, gbc);

        gbc.gridy++;
        gbc.weighty = 1;
        root.add(memorySettings, gbc);

        this.getDialog().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                instance.setJavaPath(javaPathTextField.getText());
                String minMemory = minMemoryField.getText();
                if (minMemory.isEmpty()) {
                    minMemory = "512";
                }

                String maxMemory = maxMemoryField.getText();
                if (maxMemory.isEmpty()) {
                    maxMemory = "2048";
                }

                int minimumMemoryInMegabytes = Integer.parseInt(minMemory);
                int maximumMemoryInMegabytes = Integer.parseInt(maxMemory);

                if (minimumMemoryInMegabytes > maximumMemoryInMegabytes) {
                    JOptionPane.showMessageDialog(JavaTab.this.getDialog(),
                        language.getString("messages.gui.instanceSettingsDialog.ramMinCannotBeLargerMax"),
                        language.getString("messages.gui.error"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (minimumMemoryInMegabytes < 512) {
                    JOptionPane.showMessageDialog(
                            JavaTab.this.getDialog(),
                        language.getString("messages.gui.instanceSettingsDialog.ramMinCannotBeLess512MB"),
                        language.getString("messages.gui.error"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }

                instance.setJavaPath(javaPathTextField.getText());
                instance.setMinimumMemoryInMegabytes(minimumMemoryInMegabytes);
                instance.setMaximumMemoryInMegabytes(maximumMemoryInMegabytes);
            }
        });
    }

    @Override
    public void save() throws IOException {

    }
}
