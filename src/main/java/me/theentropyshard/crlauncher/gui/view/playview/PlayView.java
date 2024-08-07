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

package me.theentropyshard.crlauncher.gui.view.playview;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.CosmicRunner;
import me.theentropyshard.crlauncher.cosmic.icon.IconManager;
import me.theentropyshard.crlauncher.gui.components.AddInstanceItem;
import me.theentropyshard.crlauncher.gui.components.InstanceItem;
import me.theentropyshard.crlauncher.gui.dialogs.SelectIconDialog;
import me.theentropyshard.crlauncher.gui.dialogs.addinstance.AddInstanceDialog;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.InstanceSettingsDialog;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.MouseClickListener;
import me.theentropyshard.crlauncher.gui.utils.MouseEnterExitListener;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.instance.InstanceManager;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.OperatingSystem;
import me.theentropyshard.crlauncher.utils.TimeUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PlayView extends JPanel {


    public static final String DEFAULT_GROUP_NAME = "<default>";

    private final PlayViewHeader header;
    private final JPanel instancesPanelView;
    private final InstancesPanel defaultInstancesPanel;
    private final Map<String, InstancesPanel> groups;
    private final CardLayout cardLayout;
    private final DefaultComboBoxModel<String> model;
    private final JLabel instanceInfoLabel;

    private InstancesPanel currentPanel;

    public PlayView() {
        super(new BorderLayout());

        this.groups = new HashMap<>();
        this.cardLayout = new CardLayout();
        this.instancesPanelView = new JPanel(this.cardLayout);

        this.header = new PlayViewHeader();
        this.add(this.header, BorderLayout.NORTH);

        AddInstanceItem defaultItem = new AddInstanceItem();
        defaultItem.addMouseListener(new MouseClickListener(e -> {
            new AddInstanceDialog(this, PlayView.DEFAULT_GROUP_NAME);
        }));
        this.defaultInstancesPanel = new InstancesPanel(defaultItem);
        this.currentPanel = this.defaultInstancesPanel;
        this.groups.put(PlayView.DEFAULT_GROUP_NAME, this.defaultInstancesPanel);
        this.add(this.instancesPanelView, BorderLayout.CENTER);

        this.groups.forEach((name, panel) -> {
            this.instancesPanelView.add(panel, name);
        });

        JComboBox<String> instanceGroups = this.header.getInstanceGroups();
        String[] items = {PlayView.DEFAULT_GROUP_NAME};
        this.model = new DefaultComboBoxModel<>(items);

        instanceGroups.setModel(this.model);
        instanceGroups.addItemListener(e -> {
            int stateChange = e.getStateChange();
            if (stateChange == ItemEvent.SELECTED) {
                Object[] selectedObjects = e.getItemSelectable().getSelectedObjects();
                String groupName = String.valueOf(selectedObjects[0]);
                this.cardLayout.show(this.instancesPanelView, groupName);
                this.currentPanel = this.groups.get(groupName);
            }
        });

        this.instanceInfoLabel = new JLabel();
        this.instanceInfoLabel.setVisible(false);

        this.add(this.instanceInfoLabel, BorderLayout.SOUTH);

        new SwingWorker<List<Instance>, Void>() {
            @Override
            protected List<Instance> doInBackground() {
                InstanceManager instanceManager = CRLauncher.getInstance().getInstanceManager();

                List<Instance> instances = instanceManager.getInstances();
                instances.sort((instance1, instance2) -> {
                    LocalDateTime lastTimePlayed1 = instance1.getLastTimePlayed();
                    LocalDateTime lastTimePlayed2 = instance2.getLastTimePlayed();
                    return lastTimePlayed2.compareTo(lastTimePlayed1);
                });

                return instances;
            }

            @Override
            protected void done() {
                try {
                    List<Instance> instances = this.get();

                    IconManager iconManager = CRLauncher.getInstance().getIconManager();

                    for (Instance instance : instances) {
                        Icon icon;
                        try {
                            icon = iconManager.getIcon(instance.getIconFileName()).icon();
                        } catch (Exception e) {
                            Log.warn("Could not load icon '" + instance.getIconFileName() + "' for instance '" + instance.getName() + "'");

                            String validIconPath = "cosmic_logo_x32.png";
                            instance.setIconFileName(validIconPath);
                            icon = iconManager.getIcon(validIconPath).icon();
                        }
                        InstanceItem item = new InstanceItem(icon, instance.getName());
                        PlayView.this.addInstanceItem(item, instance.getGroupName());
                    }

                    String group = CRLauncher.getInstance().getSettings().lastInstanceGroup;
                    if (group != null && !group.isEmpty()) {
                        PlayView.this.model.setSelectedItem(group);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public void addInstanceItem(InstanceItem item, String groupName) {
        if (item instanceof AddInstanceItem) {
            throw new IllegalArgumentException("Adding AddInstanceItem is not allowed");
        }

        InstancesPanel panel = this.groups.get(groupName);
        if (panel == null) {
            AddInstanceItem addInstanceItem = new AddInstanceItem();
            addInstanceItem.addMouseListener(new MouseClickListener(e -> {
                new AddInstanceDialog(this, groupName);
            }));
            panel = new InstancesPanel(addInstanceItem);
            this.groups.put(groupName, panel);
            this.model.addElement(groupName);
            this.instancesPanelView.add(panel, groupName);
        }
        panel.addInstanceItem(item);

        InstancesPanel finalPanel = panel;

        item.addMouseListener(new MouseClickListener(e -> {
            int mouseButton = e.getButton();
            if (mouseButton == MouseEvent.BUTTON1) { // left mouse button
                finalPanel.makeItemFirst(item);

                new CosmicRunner(item.getAssociatedInstance()).start();
            } else if (mouseButton == MouseEvent.BUTTON3) { // right mouse button
                Instance instance = item.getAssociatedInstance();

                JPopupMenu popupMenu = new JPopupMenu();

                JMenuItem editMenuItem = new JMenuItem("Edit");
                editMenuItem.addActionListener(edit -> {
                    new InstanceSettingsDialog(instance);
                });
                popupMenu.add(editMenuItem);

                JMenuItem iconMenuItem = new JMenuItem("Icon");
                iconMenuItem.addActionListener(edit -> {
                    new SelectIconDialog(item, instance);
                });
                popupMenu.add(iconMenuItem);

                JMenuItem renameItem = new JMenuItem("Rename");
                renameItem.addActionListener(rename -> {
                    String newName = MessageBox.showInputMessage(CRLauncher.frame, "Rename instance", "Enter new name");

                    if (newName == null || newName.isEmpty()) {
                        return;
                    }

                    InstanceManager manager = CRLauncher.getInstance().getInstanceManager();
                    try {
                        if (manager.renameInstance(instance, newName)) {
                            MessageBox.showWarningMessage(CRLauncher.frame, "An invalid name was supplied! Valid name was created.");
                        }
                        item.instanceChanged(instance);
                    } catch (IOException ex) {
                        Log.error("Could not rename instance " + instance.getName() + " (" + instance.getWorkDir() + ") to " + newName);
                    }
                });
                popupMenu.add(renameItem);

                popupMenu.addSeparator();

                JMenuItem deleteMenuItem = new JMenuItem("Delete");
                deleteMenuItem.addActionListener(delete -> {
                    this.deleteInstance(item);
                });
                popupMenu.add(deleteMenuItem);

                popupMenu.addSeparator();

                JMenuItem openInstanceFolder = new JMenuItem("Open instance folder");
                openInstanceFolder.addActionListener(open -> {
                    OperatingSystem.open(instance.getWorkDir());
                });
                popupMenu.add(openInstanceFolder);

                JMenuItem openCosmicFolder = new JMenuItem("Open Cosmic folder");
                openCosmicFolder.addActionListener(open -> {
                    OperatingSystem.open(instance.getCosmicDir());
                });
                popupMenu.add(openCosmicFolder);

                popupMenu.show(item, e.getX(), e.getY());
            }
        }));

        item.addMouseListener(new MouseEnterExitListener(
            enter -> {
                this.instanceInfoLabel.setVisible(true);

                Instance instance = item.getAssociatedInstance();

                if (instance == null) {
                    return;
                }

                String lastPlayedTime = TimeUtils.getHoursMinutesSeconds(instance.getLastPlaytime());
                String totalPlayedTime = TimeUtils.getHoursMinutesSeconds(instance.getTotalPlaytime());

                String timeString = "";

                if (!lastPlayedTime.isEmpty()) {
                    timeString = " - Last played for " + lastPlayedTime;
                }

                if (!totalPlayedTime.isEmpty()) {
                    if (lastPlayedTime.isEmpty()) {
                        timeString = " - Total played for " + totalPlayedTime;
                    } else {
                        timeString = timeString + ", Total played for " + totalPlayedTime;
                    }
                }

                timeString = instance.getName() + timeString;

                if (instance.isRunning()) {
                    timeString = "[Running] " + timeString;
                }

                this.instanceInfoLabel.setText(timeString);
            },

            exit -> {
                this.instanceInfoLabel.setVisible(false);
                this.instanceInfoLabel.setText("");
            }
        ));
    }

    public void deleteInstance(InstanceItem item) {
        Instance instance = item.getAssociatedInstance();

        boolean ok = MessageBox.showConfirmMessage(
            CRLauncher.frame,
            "Delete instance",
            "Are you sure you want to delete instance '" + instance.getName() + "'?"
        );

        if (ok) {
            InstanceManager instanceManager = CRLauncher.getInstance().getInstanceManager();
            try {
                instanceManager.removeInstance(instance.getName());
            } catch (IOException ex) {
                Log.error("Could not remove instance instance " + instance.getWorkDir(), ex);

                return;
            }

            JPanel instancesPanel = this.currentPanel.getInstancesPanel();
            instancesPanel.remove(item);
            instancesPanel.revalidate();
        }
    }

    public DefaultComboBoxModel<String> getModel() {
        return this.model;
    }

    public InstancesPanel getCurrentInstancesPanel() {
        return this.currentPanel;
    }

    public PlayViewHeader getHeader() {
        return this.header;
    }

    public Map<String, InstancesPanel> getGroups() {
        return this.groups;
    }

    public InstancesPanel getDefaultInstancesPanel() {
        return this.defaultInstancesPanel;
    }
}
