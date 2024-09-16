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

package me.theentropyshard.crlauncher.gui.dialogs;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.cosmic.icon.CosmicIcon;
import me.theentropyshard.crlauncher.cosmic.icon.IconManager;
import me.theentropyshard.crlauncher.gui.components.InstanceItem;
import me.theentropyshard.crlauncher.gui.layouts.WrapLayout;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.concurrent.ExecutionException;

public class SelectIconDialog extends AppDialog {
    public SelectIconDialog(InstanceItem item, Instance instance) {
        super(CRLauncher.frame, "Select an Icon - " + instance.getName());

        JPanel root = new JPanel(new BorderLayout());
        root.setPreferredSize(new Dimension(280, 180));

        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");

        ActionMap actionMap = root.getActionMap();
        actionMap.put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SelectIconDialog.this.getDialog().dispose();
            }
        });

        JPanel iconButtonsPanel = new JPanel(new WrapLayout(WrapLayout.LEFT, 8, 8));
        root.add(iconButtonsPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        root.add(buttonsPanel, BorderLayout.SOUTH);

        JButton addIconButton = new JButton("Add Icon");
        buttonsPanel.add(addIconButton);
        addIconButton.addActionListener(e -> {
            new Worker<CosmicIcon, Void>("picking icon") {
                @Override
                protected CosmicIcon work() throws Exception {
                    UIManager.put("FileChooser.readOnly", Boolean.TRUE);
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileFilter(new FileNameExtensionFilter("Images (*.png, *.jpg)", "png", "jpg"));

                    Settings settings = CRLauncher.getInstance().getSettings();
                    if (settings.lastDir != null && !settings.lastDir.isEmpty()) {
                        fileChooser.setCurrentDirectory(new File(settings.lastDir));
                    }

                    int option = fileChooser.showOpenDialog(CRLauncher.frame);
                    if (option == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        if (selectedFile == null) {
                            return null;
                        }

                        settings.lastDir = fileChooser.getCurrentDirectory().getAbsolutePath();

                        IconManager iconManager = CRLauncher.getInstance().getIconManager();

                        return iconManager.saveIcon(selectedFile.toPath());
                    }

                    UIManager.put("FileChooser.readOnly", Boolean.FALSE);
                    return null;
                }

                @Override
                protected void done() {
                    CosmicIcon gotIcon = null;
                    try {
                        gotIcon = this.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        Log.error(ex);
                    }
                    if (gotIcon == null) {
                        return;
                    }
                    CosmicIcon icon = gotIcon;
                    instance.setIconFileName(icon.fileName());
                    item.getIconLabel().setIcon(icon.icon());

                    JButton cosmicButton = new JButton(icon.icon());
                    cosmicButton.addActionListener(e -> {
                        instance.setIconFileName(icon.fileName());
                        item.getIconLabel().setIcon(icon.icon());

                        SelectIconDialog.this.getDialog().dispose();
                    });
                    iconButtonsPanel.add(cosmicButton);
                    iconButtonsPanel.revalidate();
                }
            }.execute();
        });

        for (CosmicIcon icon : CRLauncher.getInstance().getIconManager().getIcons()) {
            JButton cosmicButton = new JButton(icon.icon());
            cosmicButton.addActionListener(e -> {
                instance.setIconFileName(icon.fileName());
                item.getIconLabel().setIcon(icon.icon());

                this.getDialog().dispose();
            });
            iconButtonsPanel.add(cosmicButton);
        }

        this.setResizable(false);
        this.setContent(root);
        this.center(0);
        this.setVisible(true);
    }
}
