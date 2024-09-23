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
import me.theentropyshard.crlauncher.crmm.model.project.ProjectVersion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModVersionCard extends JPanel {
    private static final int BORDER_SIZE = 12;
    private static final int ARC_SIZE = 10;

    private Color defaultColor;
    private Color hoveredColor;
    private Color pressedColor;

    private boolean mouseOver;
    private boolean mousePressed;

    public ModVersionCard(ProjectVersion version, ActionListener onClick) {
        super(new BorderLayout());

        JLabel versionNameLabel = new JLabel(version.getVersionNumber());
        this.add(versionNameLabel, BorderLayout.WEST);

        this.add(Box.createHorizontalBox(), BorderLayout.CENTER);

        Language language = CRLauncher.getInstance().getLanguage();
        JButton downloadButton = new JButton(language.getString("gui.searchCRMMModsDialog.downloadButton"));
        downloadButton.addActionListener(onClick);
        this.add(downloadButton, BorderLayout.EAST);

        this.setOpaque(false);
        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));

        this.setBorder(new EmptyBorder(
            ModVersionCard.BORDER_SIZE,
            ModVersionCard.BORDER_SIZE,
            ModVersionCard.BORDER_SIZE,
            ModVersionCard.BORDER_SIZE
        ));

        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                ModVersionCard.this.mouseOver = true;
                ModVersionCard.this.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ModVersionCard.this.mouseOver = false;
                ModVersionCard.this.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                ModVersionCard.this.mousePressed = true;
                ModVersionCard.this.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                ModVersionCard.this.mousePressed = false;
                ModVersionCard.this.repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        this.paintBackground(g2d);

        super.paintComponent(g2d);
    }

    private void paintBackground(Graphics2D g2d) {
        Color color = this.defaultColor;

        if (this.mouseOver) {
            color = this.hoveredColor;
        }

        if (this.mousePressed) {
            color = this.pressedColor;
        }

        g2d.setColor(color);
        g2d.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), ModVersionCard.ARC_SIZE, ModVersionCard.ARC_SIZE);
    }

    public void setDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    public void setHoveredColor(Color hoveredColor) {
        this.hoveredColor = hoveredColor;
    }

    public void setPressedColor(Color pressedColor) {
        this.pressedColor = pressedColor;
    }
}
