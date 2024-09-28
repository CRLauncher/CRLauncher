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
import me.theentropyshard.crlauncher.crmm.model.project.ProjectFile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class FileCard extends JPanel {
    private static final int BORDER_SIZE = 12;
    private static final int ARC_SIZE = 10;

    private final JLabel fileNameLabel;
    private final JButton downloadButton;

    private Color defaultColor;
    private Color hoveredColor;
    private Color pressedColor;

    private boolean mouseOver;
    private boolean mousePressed;

    public FileCard(ProjectFile file, ModVersionCard.FileListener listener, boolean primary) {
        super(new BorderLayout());

        Language language = CRLauncher.getInstance().getLanguage();

        String primaryText = language.getString("gui.searchCRMMModsDialog.primary");

        this.fileNameLabel = new JLabel(
            "<html>" + file.getName() + " (" + FileCard.humanReadableByteCountBin(
                file.getSize()
            ) + ")" + (primary ? " <i><b>[" + primaryText + "]</b></i>" : "") + "</html>"
        );
        this.add(this.fileNameLabel, BorderLayout.WEST);

        this.add(Box.createHorizontalBox(), BorderLayout.CENTER);

        String downloadText = language.getString("gui.searchCRMMModsDialog.downloadButton");

        if (primary) {
            this.downloadButton = new JButton(downloadText) {
                @Override
                public boolean isDefaultButton() {
                    return true;
                }
            };
        } else {
            this.downloadButton = new JButton(downloadText);
        }

        this.downloadButton.addActionListener(e -> {
            listener.fileChosen(file);
        });
        this.add(this.downloadButton, BorderLayout.EAST);

        this.setOpaque(false);
        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));

        this.setBorder(new EmptyBorder(
            FileCard.BORDER_SIZE,
            FileCard.BORDER_SIZE,
            FileCard.BORDER_SIZE,
            FileCard.BORDER_SIZE
        ));

        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                FileCard.this.mouseOver = true;
                FileCard.this.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                FileCard.this.mouseOver = false;
                FileCard.this.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                FileCard.this.mousePressed = true;
                FileCard.this.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                FileCard.this.mousePressed = false;
                FileCard.this.repaint();
            }
        });
    }

    // idk how this works, but https://stackoverflow.com/a/3758880/19857533
    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
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
        g2d.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), FileCard.ARC_SIZE, FileCard.ARC_SIZE);
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
