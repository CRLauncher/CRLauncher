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

package me.theentropyshard.crlauncher.gui.view.crmm;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.gui.dialogs.AppDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectLoadersDialog extends AppDialog {
    private final Set<String> loaders;

    public SelectLoadersDialog() {
        super(CRLauncher.frame,
            CRLauncher.getInstance().getLanguage().getString("gui.searchCRMMModsDialog.selectLoadersDialog.title")
        );

        this.loaders = new HashSet<>();
        this.loaders.add("quilt");
        this.loaders.add("puzzle_loader");

        Language language = CRLauncher.getInstance().getLanguage();

        JPanel root = new JPanel(new BorderLayout());

        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");

        ActionMap actionMap = root.getActionMap();
        actionMap.put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SelectLoadersDialog.this.getDialog().dispose();
            }
        });

        JLabel selectLoadersLabel = new JLabel(language.getString("gui.searchCRMMModsDialog.selectLoadersDialog.message"));
        selectLoadersLabel.setBorder(new EmptyBorder(0, 12, 0, 12));
        selectLoadersLabel.setFont(selectLoadersLabel.getFont().deriveFont(18.0f));

        root.add(selectLoadersLabel, BorderLayout.NORTH);

        JPanel loadersPanel = new JPanel(new MigLayout("nogrid, flowy, wrap", "", "[center][center]"));
        loadersPanel.setBorder(new EmptyBorder(0, 4, 0, 0));

        JCheckBox quiltCheck = new JCheckBox("Quilt", true);
        quiltCheck.addActionListener(e -> {
            String quilt = "quilt";

            if (quiltCheck.isSelected()) {
                this.loaders.add(quilt);
            } else {
                this.loaders.remove(quilt);
            }
        });
        loadersPanel.add(quiltCheck);

        JCheckBox puzzleCheck = new JCheckBox("Puzzle Loader", true);
        puzzleCheck.addActionListener(e -> {
            String puzzle = "puzzle_loader";

            if (puzzleCheck.isSelected()) {
                this.loaders.add(puzzle);
            } else {
                this.loaders.remove(puzzle);
            }
        });
        loadersPanel.add(puzzleCheck);

        root.add(loadersPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setBorder(new MatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));

        JButton okButton = new JButton(language.getString("gui.general.ok"));
        okButton.addActionListener(e -> {
            this.getDialog().dispose();
        });
        this.getDialog().getRootPane().setDefaultButton(okButton);
        buttonsPanel.add(okButton);

        root.add(buttonsPanel, BorderLayout.SOUTH);

        this.setResizable(false);
        this.setContent(root);
        this.center(0);
        this.setVisible(true);
    }

    public List<String> getLoaders() {
        return new ArrayList<>(this.loaders);
    }
}
