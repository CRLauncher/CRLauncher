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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.java;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatScrollPaneBorder;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.gui.FlatSmoothScrollPaneUI;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.cosmic.CosmicRunner;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.Tab;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.language.LanguageSection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class JavaTab extends Tab {
    public static final Font MONOSPACED = new Font(Font.MONOSPACED, Font.PLAIN, 14);

    private final JTextArea flagsArea;
    private final JComboBox<String> flagsOptions;

    public JavaTab(CosmicInstance instance, JDialog dialog) {
        super(CRLauncher.getInstance().getLanguage()
            .getString("gui.instanceSettingsDialog.javaTab.name"), instance, dialog);

        JPanel root = this.getRoot();
        root.setLayout(new GridBagLayout());

        Language language = CRLauncher.getInstance().getLanguage();
        LanguageSection javaTabSection = language.getSection("gui.instanceSettingsDialog.javaTab");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        JPanel javaInstallation = new JPanel(new GridLayout(2, 1));
        JavaPathTextField javaPathTextField = new JavaPathTextField();
        javaPathTextField.setText(instance.getJavaPath());
        javaInstallation.add(javaPathTextField);
        javaInstallation.setBorder(new TitledBorder(javaTabSection.getString("javaInstallation.borderName")));

        JCheckBox customJavaPath = new JCheckBox(javaTabSection.getString("javaInstallation.customJavaPath"));
        customJavaPath.setSelected(instance.isCustomJavaPath());
        javaPathTextField.setEnabled(instance.isCustomJavaPath());
        customJavaPath.addActionListener(e -> {
            instance.setCustomJavaPath(customJavaPath.isSelected());
            javaPathTextField.setEnabled(customJavaPath.isSelected());
        });
        javaInstallation.add(customJavaPath);

        JPanel memorySettings = new JPanel(new GridLayout(2, 2));
        JLabel minMemoryLabel = new JLabel(javaTabSection.getString("memorySettings.minimum") + ":");
        JLabel maxMemoryLabel = new JLabel(javaTabSection.getString("memorySettings.maximum") + ":");
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
        memorySettings.setBorder(new TitledBorder(javaTabSection.getString("memorySettings.borderName")));

        gbc.gridy++;
        root.add(javaInstallation, gbc);

        gbc.gridy++;
        root.add(memorySettings, gbc);

        JPanel jvmFlags = new JPanel(new BorderLayout());
        jvmFlags.setBorder(BorderFactory.createTitledBorder(javaTabSection.getString("jvmFlags.borderName")));

        String[] items = {
            javaTabSection.getString("jvmFlags.options.customFlags"),
            javaTabSection.getString("jvmFlags.options.optimizedFlags1"),
            javaTabSection.getString("jvmFlags.options.optimizedFlags2")
        };
        this.flagsOptions = new JComboBox<>(items);
        int currentFlagsOption = instance.getCurrentFlagsOption();
        if (currentFlagsOption < 0 || currentFlagsOption >= items.length) {
            currentFlagsOption = 0;
        }
        this.flagsOptions.setSelectedIndex(currentFlagsOption);
        jvmFlags.add(this.flagsOptions, BorderLayout.NORTH);

        this.flagsArea = new JTextArea();
        this.flagsArea.setFont(JavaTab.MONOSPACED);
        this.flagsArea.setLineWrap(true);
        this.flagsArea.setWrapStyleWord(true);
        this.flagsArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                this.textUpdated();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                this.textUpdated();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }

            private void textUpdated() {
                if (instance.getCurrentFlagsOption() == 0) {
                    instance.setCustomJvmFlags(new LinkedHashSet<>(List.of(JavaTab.this.flagsArea.getText().split("\\s"))));
                }
            }
        });

        if (currentFlagsOption != 0) {
            this.flagsArea.setEditable(false);
            String[] builtinFlags = CosmicRunner.getBuiltinFlags(currentFlagsOption);
            this.flagsArea.setText(String.join(" ", builtinFlags));
        }

        this.flagsOptions.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }

            int selectedIndex = this.flagsOptions.getSelectedIndex();

            instance.setCurrentFlagsOption(selectedIndex);

            if (selectedIndex != 0) {
                this.flagsArea.setEditable(false);
                String[] builtinFlags = CosmicRunner.getBuiltinFlags(selectedIndex);
                this.flagsArea.setText(String.join(" ", builtinFlags));
            } else {
                JavaTab.setFlags(this.flagsArea, instance.getCustomJvmFlags());
                this.flagsArea.setEditable(true);
            }
        });

        JavaTab.setFlags(this.flagsArea, instance.getCustomJvmFlags());

        JScrollPane scrollPane = new JScrollPane(
            this.flagsArea,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setUI(new FlatSmoothScrollPaneUI());
        scrollPane.setPreferredSize(new Dimension(0, 250));
        scrollPane.setMaximumSize(new Dimension(1000, 250));
        scrollPane.setBorder(new FlatScrollPaneBorder());
        jvmFlags.add(scrollPane, BorderLayout.CENTER);

        JLabel noticeLabel = new JLabel(javaTabSection.getString("jvmFlags.notice"));
        noticeLabel.setBorder(new EmptyBorder(0, 2, 1, 2));
        jvmFlags.add(noticeLabel, BorderLayout.SOUTH);

        gbc.gridy++;
        gbc.weighty = 1;
        root.add(jvmFlags, gbc);

        this.getDialog().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
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

    public static void setFlags(JTextArea textArea, Set<String> flags) {
        if (flags != null && !flags.isEmpty()) {
            textArea.setText(String.join(" ", flags));
        }
    }
}
