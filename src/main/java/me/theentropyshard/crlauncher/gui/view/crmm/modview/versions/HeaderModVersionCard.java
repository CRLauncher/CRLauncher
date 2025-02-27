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

package me.theentropyshard.crlauncher.gui.view.crmm.modview.versions;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.language.Language;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HeaderModVersionCard extends JPanel {
    private static final int BORDER_SIZE = 12;
    private static final int ARC_SIZE = 10;

    private Color defaultColor;
    private Color hoveredColor;
    private Color pressedColor;

    private boolean mouseOver;
    private boolean mousePressed;

    public HeaderModVersionCard() {
        super(new MigLayout("insets 0, align left, aligny center", "[30%]push[20%][10%][10%]push[10%]", ""));

        Language language = CRLauncher.getInstance().getLanguage();

        JPanel versionInfoPanel = new JPanel(new GridLayout(1, 1));
        versionInfoPanel.setOpaque(false);
        JLabel versionTitleLabel = new JLabel(
            "<html><nobr><b>" + language.getString("gui.modFilesDialog.name") + "</nobr></b></html>"
        );
        versionInfoPanel.add(versionTitleLabel);

        this.add(versionInfoPanel);

        JPanel compatibility = new JPanel(new GridLayout(1, 2));
        compatibility.setOpaque(false);

        JLabel gameVersion = new JLabel(
            "<html><nobr><b>" + language.getString("gui.modFilesDialog.compatibility") + "</nobr></b></html>"
        );
        compatibility.add(gameVersion);

        this.add(compatibility);

        JLabel published = new JLabel(
            "<html><nobr><b>" + language.getString("gui.modFilesDialog.datePublished") + "</nobr></b></html>"
        );
        this.add(published);

        JLabel downloads = new JLabel(
            "<html><nobr><b>" + language.getString("gui.modFilesDialog.downloads") + "</nobr></b></html>"
        );
        this.add(downloads);

        JPanel buttonsPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS);
        buttonsPanel.setLayout(boxLayout);
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(new JLabel(
            ""
        ));

        buttonsPanel.setPreferredSize(new Dimension(176, 26));
        buttonsPanel.setMinimumSize(new Dimension(176, 26));

        this.add(buttonsPanel);


        this.setOpaque(false);
        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));

        this.setBorder(new EmptyBorder(
            HeaderModVersionCard.BORDER_SIZE,
            HeaderModVersionCard.BORDER_SIZE,
            HeaderModVersionCard.BORDER_SIZE,
            HeaderModVersionCard.BORDER_SIZE
        ));

        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                HeaderModVersionCard.this.mouseOver = true;
                HeaderModVersionCard.this.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                HeaderModVersionCard.this.mouseOver = false;
                HeaderModVersionCard.this.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                HeaderModVersionCard.this.mousePressed = true;
                HeaderModVersionCard.this.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                HeaderModVersionCard.this.mousePressed = false;
                HeaderModVersionCard.this.repaint();
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
        g2d.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), HeaderModVersionCard.ARC_SIZE, HeaderModVersionCard.ARC_SIZE);
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
