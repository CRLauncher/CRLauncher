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
import me.theentropyshard.crlauncher.gui.components.InstanceItem;
import me.theentropyshard.crlauncher.gui.dialogs.AppDialog;
import me.theentropyshard.crlauncher.gui.playview.InstancesPanel;
import me.theentropyshard.crlauncher.instance.OldInstance;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InstanceSettingsDialog extends AppDialog {

    private final JTabbedPane tabbedPane;
    private final List<Tab> tabs;

    public InstanceSettingsDialog(OldInstance oldInstance) {
        super(CRLauncher.window.getFrame(), "Instance Settings - " + oldInstance.getName());

        this.tabbedPane = new JTabbedPane(JTabbedPane.LEFT);

        this.tabs = new ArrayList<>();

        this.addTab(new MainTab("Main", oldInstance, this.getDialog()));
        this.addTab(new JavaTab("Java", oldInstance, this.getDialog()));
        this.addTab(new JarModsTab(oldInstance, this.getDialog()));
        this.addTab(new FabricModsTab(oldInstance, this.getDialog()));

        this.getDialog().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                InstanceSettingsDialog.this.tabs.forEach(tab -> {
                    try {
                        tab.save();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                InstancesPanel instancesPanel = CRLauncher.getInstance().getGui().getPlayView().getCurrentInstancesPanel();
                JPanel itemsPanel = instancesPanel.getInstancesPanel();
                for (Component component : itemsPanel.getComponents()) {
                    OldInstance associatedOldInstance = ((InstanceItem) component).getAssociatedInstance();
                    if (associatedOldInstance == oldInstance) {
                        ((InstanceItem) component).getTextLabel().setText(oldInstance.getName());
                        try {
                            oldInstance.save();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });

        this.setContent(this.tabbedPane);
        this.center(0);
        this.setVisible(true);
    }

    public void addTab(Tab tab) {
        this.tabs.add(tab);
        this.tabbedPane.addTab(tab.getName(), tab.getRoot());
    }
}
