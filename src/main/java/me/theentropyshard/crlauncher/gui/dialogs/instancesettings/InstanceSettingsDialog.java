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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.gui.action.InstanceExportAction;
import me.theentropyshard.crlauncher.gui.components.InstanceItem;
import me.theentropyshard.crlauncher.gui.dialogs.AppDialog;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.gamelog.GameLogTab;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.MainTab;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.Tab;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.jar.JarModsTab;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.java.JavaTab;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTab;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.screenshots.ScreenshotsTab;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.worlds.WorldsTab;
import me.theentropyshard.crlauncher.gui.view.playview.InstancesPanel;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InstanceSettingsDialog extends AppDialog {
    private final JTabbedPane tabbedPane;
    private final List<Tab> tabs;

    public InstanceSettingsDialog(Instance instance) {
        super(CRLauncher.frame,
            CRLauncher.getInstance().getLanguage()
                .getString("gui.instanceSettingsDialog.title")
                .replace("$$INSTANCE_NAME$$", instance.getName()));

        this.tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        this.tabbedPane.setPreferredSize(new Dimension(900, 480));

        InputMap inputMap = this.tabbedPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");

        ActionMap actionMap = this.tabbedPane.getActionMap();
        actionMap.put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InstanceSettingsDialog.this.getDialog().dispose();
            }
        });

        this.tabs = new ArrayList<>();

        this.addTab(new MainTab(instance, this.getDialog()));
        this.addTab(new JavaTab(instance, this.getDialog()));
        this.addTab(new ModsTab(instance, this.getDialog()));
        this.addTab(new JarModsTab(instance, this.getDialog()));
        this.addTab(new WorldsTab(instance, this.getDialog()));
        this.addTab(new ScreenshotsTab(instance, this.getDialog()));
        this.addTab(new GameLogTab(instance, this.getDialog()));

        this.getDialog().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                InstanceSettingsDialog.this.tabs.forEach(tab -> {
                    try {
                        tab.save();
                    } catch (IOException ex) {
                        Log.error("Error saving tab", ex);
                    }
                });

                InstancesPanel instancesPanel = CRLauncher.getInstance().getGui().getPlayView().getCurrentInstancesPanel();
                JPanel itemsPanel = instancesPanel.getInstancesPanel();
                for (Component component : itemsPanel.getComponents()) {
                    if (component instanceof InstanceItem item) {
                        Instance associatedInstance = item.getAssociatedInstance();
                        if (associatedInstance == instance) {
                            ((InstanceItem) component).getTextLabel().setText(instance.getName());
                            try {
                                instance.save();
                            } catch (IOException ex) {
                                Log.error("Error saving instance " + instance.getName(), ex);
                            }
                        }
                    }
                }
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.add(this.tabbedPane, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
            UIManager.getColor("Component.borderColor")));

        JButton exportButton = new JButton();
        exportButton.setAction(new InstanceExportAction(instance));
        buttonsPanel.add(exportButton);
        root.add(buttonsPanel, BorderLayout.SOUTH);

        this.setContent(root);
        this.center(0);
        this.setVisible(true);
    }

    public void addTab(Tab tab) {
        this.tabs.add(tab);
        this.tabbedPane.addTab(tab.getName(), tab.getRoot());
    }
}
