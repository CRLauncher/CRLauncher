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

package me.theentropyshard.crlauncher.gui.dialogs.addinstance;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.version.CosmicArchiveVersion;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.cosmic.version.Version;
import me.theentropyshard.crlauncher.gui.FlatSmoothScrollPaneUI;
import me.theentropyshard.crlauncher.gui.action.InstanceImportAction;
import me.theentropyshard.crlauncher.gui.components.InstanceItem;
import me.theentropyshard.crlauncher.gui.dialogs.AppDialog;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.gui.view.playview.PlayView;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.instance.InstanceAlreadyExistsException;
import me.theentropyshard.crlauncher.instance.InstanceManager;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class AddInstanceDialog extends AppDialog {
    public static final String TITLE = "gui.addInstanceDialog.title";
    public static final String NAME_LABEL = "gui.addInstanceDialog.nameFieldLabel";
    public static final String GROUP_LABEL = "gui.addInstanceDialog.groupFieldLabel";
    public static final String AUTO_UPDATE_TO_LATEST = "gui.addInstanceDialog.autoUpdateToLatest";
    public static final String FILTER = "gui.addInstanceDialog.filter";
    public static final String REFRESH_BUTTON = "gui.addInstanceDialog.refreshButton";
    public static final String ADD_BUTTON = "gui.addInstanceDialog.addButton";
    public static final String CANCEL_BUTTON = "gui.addInstanceDialog.cancelButton";
    public static final String EMPTY_NAME_MESSAGE = "messages.gui.addInstanceDialog.instanceNameCannotBeEmpty";
    public static final String GROUP_NAME_EMPTY_MESSAGE = "messages.gui.addInstanceDialog.groupNameCannotBeEmpty";
    public static final String VERSION_NOT_SELECTED_MESSAGE = "messages.gui.addInstanceDialog.cosmicVersionNotSelected";
    public static final String UNABLE_TO_CREATE_MESSAGE = "messages.gui.addInstanceDialog.unableToCreateInstance";
    public static final String NO_CLIENT_MESSAGE = "messages.gui.addInstanceDialog.doesNotHaveClient";
    public static final String SHOW_ONLY_INSTALLED = "gui.addInstanceDialog.showOnlyInstalled";
    private final JTextField nameField;
    private final JTextField groupField;
    private final JButton addButton;
    private final JCheckBox preAlphasBox;

    private boolean nameEdited;

    public AddInstanceDialog(PlayView playView, String groupName) {
        super(CRLauncher.frame,
            CRLauncher.getInstance().getLanguage().getString(AddInstanceDialog.TITLE));

        JPanel root = new JPanel(new BorderLayout());

        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");

        ActionMap actionMap = root.getActionMap();
        actionMap.put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddInstanceDialog.this.getDialog().dispose();
            }
        });

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

        Language language = CRLauncher.getInstance().getLanguage();

        JPanel headerPanelLeftPanel = new JPanel();
        headerPanelLeftPanel.setLayout(new GridLayout(2, 1));
        headerPanelLeftPanel.add(new JLabel(language.getString(AddInstanceDialog.NAME_LABEL) + ":") {{
            this.setVerticalTextPosition(JLabel.CENTER);
            this.setBorder(new EmptyBorder(0, 0, 0, 10));
        }});
        headerPanelLeftPanel.add(new JLabel(language.getString(AddInstanceDialog.GROUP_LABEL) + ":") {{
            this.setVerticalTextPosition(JLabel.CENTER);
            this.setBorder(new EmptyBorder(0, 0, 0, 10));
        }});

        JPanel headerPanelRightPanel = new JPanel();
        headerPanelRightPanel.setLayout(new GridLayout(2, 1));

        this.nameField = new JTextField();
        this.nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                AddInstanceDialog.this.nameEdited = true;
            }
        });
        headerPanelRightPanel.add(this.nameField);

        this.groupField = new JTextField(groupName);
        headerPanelRightPanel.add(this.groupField);

        headerPanel.add(headerPanelLeftPanel, BorderLayout.WEST);
        headerPanel.add(headerPanelRightPanel, BorderLayout.CENTER);

        JCheckBox updateToLatestAutomatically = new JCheckBox(language.getString(AddInstanceDialog.AUTO_UPDATE_TO_LATEST));
        updateToLatestAutomatically.setSelected(CRLauncher.getInstance().getSettings().settingsDialogUpdateToLatest);
        updateToLatestAutomatically.addActionListener(e -> {
            Settings settings = CRLauncher.getInstance().getSettings();
            settings.settingsDialogUpdateToLatest = !settings.settingsDialogUpdateToLatest;
        });
        headerPanel.add(updateToLatestAutomatically, BorderLayout.SOUTH);

        root.add(headerPanel, BorderLayout.NORTH);

        JTable versionsTable = new JTable();
        versionsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (c instanceof JLabel label) {
                    if (column == 0) {
                        label.setHorizontalAlignment(JLabel.LEFT);
                    } else {
                        label.setHorizontalAlignment(JLabel.CENTER);
                    }
                }

                return c;
            }
        });
        SwingUtils.setJTableColumnsWidth(versionsTable, 70, 15, 15);
        versionsTable.getTableHeader().setEnabled(false);
        versionsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        CosmicVersionsTableModel tableModel = new CosmicVersionsTableModel(this, versionsTable);
        versionsTable.setModel(tableModel);

        versionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (this.nameEdited) {
                return;
            }

            int selectedRow = versionsTable.getSelectedRow();

            if (selectedRow != -1) {
                selectedRow = versionsTable.convertRowIndexToModel(selectedRow);
                this.nameField.setText(String.valueOf(versionsTable.getModel().getValueAt(selectedRow, 0)));
            }
        });

        JScrollPane scrollPane = new JScrollPane(
            versionsTable,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setUI(new FlatSmoothScrollPaneUI());

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy++;

        JLabel filterLabel = new JLabel(language.getString(AddInstanceDialog.FILTER));
        filterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        filterPanel.add(filterLabel, gbc);

        gbc.anchor = GridBagConstraints.WEST;

        this.preAlphasBox = new JCheckBox("Pre-Alpha", true);
        JCheckBox experimentsBox = new JCheckBox("Experiments");

        gbc.gridy++;
        filterPanel.add(this.preAlphasBox, gbc);

        gbc.gridy++;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 1.0;
        filterPanel.add(experimentsBox, gbc);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(filterPanel, BorderLayout.EAST);

        JPanel buttonsPanel = new JPanel(new BorderLayout());

        FlowLayout leftLayout = new FlowLayout(FlowLayout.LEFT);
        leftLayout.setHgap(0);
        leftLayout.setVgap(0);

        JPanel leftButtonsPanel = new JPanel(leftLayout);
        buttonsPanel.add(leftButtonsPanel, BorderLayout.WEST);

        JButton refreshManifest = new JButton(language.getString(AddInstanceDialog.REFRESH_BUTTON));
        refreshManifest.addActionListener(e -> {
            tableModel.reload(true);
            root.revalidate();
        });

        leftButtonsPanel.add(refreshManifest);

        JButton importButton = new JButton();
        importButton.setAction(new InstanceImportAction(this.getDialog()));
        leftButtonsPanel.add(importButton);

        JCheckBox showOnlyInstalled = new JCheckBox(language.getString(AddInstanceDialog.SHOW_ONLY_INSTALLED));
        showOnlyInstalled.setSelected(CRLauncher.getInstance().getSettings().showOnlyInstalledVersions);
        showOnlyInstalled.addActionListener(e -> {
            CRLauncher.getInstance().getSettings().showOnlyInstalledVersions = showOnlyInstalled.isSelected();
            tableModel.showInstalled(showOnlyInstalled.isSelected());
            tableModel.fireTableDataChanged();
        });
        leftButtonsPanel.add(showOnlyInstalled);

        FlowLayout rightLayout = new FlowLayout(FlowLayout.RIGHT);
        rightLayout.setHgap(0);
        rightLayout.setVgap(0);

        JPanel rightButtonsPanel = new JPanel(rightLayout);
        buttonsPanel.add(rightButtonsPanel, BorderLayout.EAST);

        this.addButton = new JButton(language.getString(AddInstanceDialog.ADD_BUTTON));
        this.getDialog().getRootPane().setDefaultButton(this.addButton);
        this.addButton.setEnabled(false);
        this.addButton.addActionListener(e -> {
            String instanceName = this.nameField.getText();
            if (instanceName.trim().isEmpty()) {
                MessageBox.showErrorMessage(AddInstanceDialog.this.getDialog(),
                    language.getString(AddInstanceDialog.EMPTY_NAME_MESSAGE));

                return;
            }

            String chosenGroupName = this.groupField.getText();
            if (chosenGroupName.trim().isEmpty()) {
                MessageBox.showErrorMessage(AddInstanceDialog.this.getDialog(),
                    language.getString(AddInstanceDialog.GROUP_NAME_EMPTY_MESSAGE));

                return;
            }

            if (versionsTable.getSelectedRow() == -1) {
                MessageBox.showErrorMessage(AddInstanceDialog.this.getDialog(),
                    language.getString(AddInstanceDialog.VERSION_NOT_SELECTED_MESSAGE));

                return;
            }

            CosmicVersionsTableModel model = (CosmicVersionsTableModel) versionsTable.getModel();
            int selectedRow = versionsTable.getSelectedRow();
            selectedRow = versionsTable.convertRowIndexToModel(selectedRow);
            Version version = model.getVersion(selectedRow);
            String crVersion = version.getId();
            CRLauncher.getInstance().doTask(() -> {
                if (version instanceof CosmicArchiveVersion caVersion && caVersion.getClient() == null) {
                    MessageBox.showErrorMessage(AddInstanceDialog.this.getDialog(),
                        CRLauncher.getInstance().getLanguage().getString(AddInstanceDialog.NO_CLIENT_MESSAGE)
                            .replace("$$CR_VERSION$$", crVersion));
                    return;
                }

                InstanceManager instanceManager = CRLauncher.getInstance().getInstanceManager();

                Instance instance;

                try {
                    instance = instanceManager.createInstance(instanceName, chosenGroupName, crVersion,
                        CRLauncher.getInstance().getSettings().settingsDialogUpdateToLatest);
                } catch (InstanceAlreadyExistsException ex) {
                    MessageBox.showErrorMessage(
                        AddInstanceDialog.this.getDialog(),
                        ex.getMessage()
                    );

                    Log.warn(ex.getMessage());

                    return;
                } catch (IOException ex) {
                    Log.error(language.getString(AddInstanceDialog.UNABLE_TO_CREATE_MESSAGE), ex);

                    return;
                }

                SwingUtilities.invokeLater(() -> {
                    playView.addInstanceItem(
                        new InstanceItem(instance), chosenGroupName, false
                    );

                    this.getDialog().dispose();
                });
            });
        });
        rightButtonsPanel.add(this.addButton);
        JButton cancelButton = new JButton(language.getString(AddInstanceDialog.CANCEL_BUTTON));
        cancelButton.addActionListener(e -> {
            this.getDialog().dispose();
        });
        rightButtonsPanel.add(cancelButton);
        buttonsPanel.setBorder(new EmptyBorder(6, 10, 10, 10));
        root.add(buttonsPanel, BorderLayout.SOUTH);

        root.add(centerPanel, BorderLayout.CENTER);
        root.setPreferredSize(new Dimension(900, 480));

        this.setResizable(false);
        this.setContent(root);
        this.center(0);
        this.setVisible(true);
    }

    public JButton getAddButton() {
        return this.addButton;
    }

    public JCheckBox getPreAlphasBox() {
        return this.preAlphasBox;
    }
}
