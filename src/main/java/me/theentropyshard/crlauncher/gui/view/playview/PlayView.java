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
import me.theentropyshard.crlauncher.Language;
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
import me.theentropyshard.crlauncher.gui.utils.Worker;
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
    private CosmicRunner cosmicRunner;
    private InstanceItem lastItem;
    private Instance lastPlayedInstance;

    public PlayView() {
        super(new BorderLayout());

        this.groups = new HashMap<>();
        this.cardLayout = new CardLayout();
        this.instancesPanelView = new JPanel(this.cardLayout);

        this.header = new PlayViewHeader();
        this.add(this.header, BorderLayout.NORTH);

        AddInstanceItem defaultItem = new AddInstanceItem();
        defaultItem.onClick(e -> new AddInstanceDialog(this, PlayView.DEFAULT_GROUP_NAME));

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

        new Worker<List<Instance>, Void>("loading instances") {
            @Override
            protected List<Instance> work() throws Exception {
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

                    if (instances.size() > 0) {
                        PlayView.this.lastPlayedInstance = instances.get(0);
                    }

                    for (Instance instance : instances) {
                        PlayView.this.loadInstance(instance, false);
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

    public void loadInstance(Instance instance, boolean sort) {
        IconManager iconManager = CRLauncher.getInstance().getIconManager();

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
        PlayView.this.addInstanceItem(item, instance.getGroupName(), sort);
    }

    public void addInstanceItem(InstanceItem item, String groupName, boolean sort) {
        if (item.getAssociatedInstance() == this.lastPlayedInstance) {
            this.lastItem = item;
        }

        InstancesPanel panel = this.groups.get(groupName);
        if (panel == null) {
            AddInstanceItem addInstanceItem = new AddInstanceItem();
            addInstanceItem.onClick(e -> new AddInstanceDialog(this, groupName));

            panel = new InstancesPanel(addInstanceItem);
            this.groups.put(groupName, panel);
            this.model.addElement(groupName);
            this.instancesPanelView.add(panel, groupName);
        }
        panel.addInstanceItem(item, sort);

        InstancesPanel finalPanel = panel;

        item.addMouseListener(new MouseClickListener(e -> {
            int mouseButton = e.getButton();
            if (mouseButton == MouseEvent.BUTTON1) { // left mouse button
                finalPanel.makeItemFirst(item);

                this.lastItem = item;
                this.lastPlayedInstance = item.getAssociatedInstance();

                this.cosmicRunner = new CosmicRunner(this.lastPlayedInstance, this.lastItem);
                this.cosmicRunner.start();
            } else if (mouseButton == MouseEvent.BUTTON3) { // right mouse button
                Language language = CRLauncher.getInstance().getLanguage();

                Instance instance = item.getAssociatedInstance();

                JPopupMenu popupMenu = new JPopupMenu();

                JMenuItem editMenuItem = new JMenuItem(language.getString("gui.instanceItem.contextMenu.edit"));
                editMenuItem.addActionListener(edit -> {
                    new InstanceSettingsDialog(instance);
                });
                popupMenu.add(editMenuItem);

                JMenuItem iconMenuItem = new JMenuItem(language.getString("gui.instanceItem.contextMenu.icon"));
                iconMenuItem.addActionListener(edit -> {
                    new SelectIconDialog(item, instance);
                });
                popupMenu.add(iconMenuItem);

                JMenuItem renameItem = new JMenuItem(language.getString("gui.instanceItem.contextMenu.rename"));
                renameItem.addActionListener(rename -> {
                    String newName = MessageBox.showInputMessage(CRLauncher.frame,
                        language.getString("gui.playView.renameInstanceDialog.title"),
                        language.getString("gui.playView.renameInstanceDialog.message"),
                        instance.getName());

                    if (newName == null || newName.isEmpty() || instance.getName().equals(newName)) {
                        return;
                    }

                    InstanceManager manager = CRLauncher.getInstance().getInstanceManager();
                    try {
                        if (manager.renameInstance(instance, newName)) {
                            MessageBox.showWarningMessage(CRLauncher.frame,
                                language.getString("messages.gui.playView.invalidInstanceName"));
                        }
                        item.instanceChanged(instance);
                    } catch (IOException ex) {
                        Log.error("Could not rename instance " + instance.getName() + " (" + instance.getWorkDir() + ") to " + newName);
                    }
                });
                popupMenu.add(renameItem);

                popupMenu.addSeparator();

                JMenuItem deleteMenuItem = new JMenuItem(language.getString("gui.instanceItem.contextMenu.delete"));
                deleteMenuItem.addActionListener(delete -> {
                    this.deleteInstance(item);
                });
                popupMenu.add(deleteMenuItem);

                popupMenu.addSeparator();

                JMenuItem openInstanceFolder = new JMenuItem(language.getString("gui.instanceItem.contextMenu.openInstanceFolder"));
                openInstanceFolder.addActionListener(open -> {
                    OperatingSystem.open(instance.getWorkDir());
                });
                popupMenu.add(openInstanceFolder);

                JMenuItem openCosmicFolder = new JMenuItem(language.getString("gui.instanceItem.contextMenu.openCosmicFolder"));
                openCosmicFolder.addActionListener(open -> {
                    OperatingSystem.open(instance.getCosmicDir());
                });
                popupMenu.add(openCosmicFolder);

                popupMenu.addSeparator();

                JMenuItem exitInstanceItem = new JMenuItem(language.getString("gui.instanceItem.contextMenu.killProcess"));
                exitInstanceItem.setEnabled(item.getAssociatedInstance().isRunning());
                exitInstanceItem.addActionListener(exit -> {
                    this.cosmicRunner.stopGame();
                });
                popupMenu.add(exitInstanceItem);

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

                Language language = CRLauncher.getInstance().getLanguage();
                String lastPlayedForText = language.getString("gui.playView.lastPlayedFor");
                String totalPlayedForText = language.getString("gui.playView.totalPlayedFor");
                String runningText = language.getString("gui.playView.running");

                String lastPlayedTime = TimeUtils.getHoursMinutesSecondsLocalized(instance.getLastPlaytime());
                String totalPlayedTime = TimeUtils.getHoursMinutesSecondsLocalized(instance.getTotalPlaytime());

                String timeString = "";

                if (!lastPlayedTime.isEmpty()) {
                    timeString = " - " + lastPlayedForText + " " + lastPlayedTime;
                }

                if (!totalPlayedTime.isEmpty()) {
                    if (lastPlayedTime.isEmpty()) {
                        timeString = " - " + totalPlayedForText + " " + totalPlayedTime;
                    } else {
                        timeString = timeString + ", " + totalPlayedForText + " " + totalPlayedTime;
                    }
                }

                timeString = instance.getName() + timeString;

                if (instance.isRunning()) {
                    timeString = "[" + runningText + "] " + timeString;
                }

                this.instanceInfoLabel.setText(timeString);
            },

            exit -> {
                this.instanceInfoLabel.setVisible(false);
                this.instanceInfoLabel.setText("");
            }
        ));
    }

    public void playLastInstance() {
        if (this.lastItem != null && this.lastPlayedInstance != null) {
            this.cosmicRunner = new CosmicRunner(this.lastPlayedInstance, this.lastItem);
            this.cosmicRunner.start();
        }
    }

    public void deleteInstance(InstanceItem item) {
        Language language = CRLauncher.getInstance().getLanguage();

        Instance instance = item.getAssociatedInstance();

        boolean ok = MessageBox.showConfirmMessage(
            CRLauncher.frame,
            language.getString("gui.playView.deleteInstanceTitle"),
            language.getString("messages.gui.playView.deleteInstanceConfirm")
                .replace("$$INSTANCE_NAME$$", instance.getName())
        );

        if (ok) {
            InstanceManager instanceManager = CRLauncher.getInstance().getInstanceManager();
            try {
                instanceManager.removeInstance(instance.getName());
            } catch (IOException ex) {
                Log.error(language.getString("messages.gui.playView.cannotDeleteInstance")
                    .replace("$$INSTANCE_DIR$$", instance.getWorkDir().toString()), ex);

                return;
            }

            JPanel instancesPanel = this.currentPanel.getInstancesPanel();
            instancesPanel.remove(item);
            instancesPanel.revalidate();
        }
    }

    public void reloadLanguage() {
        this.header.reloadLanguage();
    }

    public Instance getLastPlayedInstance() {
        return this.lastPlayedInstance;
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
