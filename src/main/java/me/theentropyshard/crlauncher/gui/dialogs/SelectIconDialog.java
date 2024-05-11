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
import me.theentropyshard.crlauncher.cosmic.icon.CosmicIcon;
import me.theentropyshard.crlauncher.gui.components.InstanceItem;
import me.theentropyshard.crlauncher.gui.layouts.WrapLayout;
import me.theentropyshard.crlauncher.instance.Instance;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

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

        /*String cosmicPath = "/assets/images/cosmic_logo_x32.png";
        JButton cosmicButton = new JButton(SwingUtils.getIcon(cosmicPath));
        cosmicButton.addActionListener(e -> {
            instance.setIconPath(cosmicPath);
            item.getIconLabel().setIcon(SwingUtils.getIcon(cosmicPath));

            this.getDialog().dispose();
        });
        root.add(cosmicButton);*/

        this.setResizable(false);
        this.setContent(root);
        this.center(0);
        this.setVisible(true);
    }
}
