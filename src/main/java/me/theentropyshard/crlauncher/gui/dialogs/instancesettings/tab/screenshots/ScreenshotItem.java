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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.screenshots;

import com.google.gson.JsonObject;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.gui.components.MouseListenerBuilder;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.language.LanguageSection;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.OperatingSystem;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

public class ScreenshotItem extends JPanel {
    private Color defaultColor;
    private Color hoveredColor;
    private Color pressedColor;

    private boolean mouseOver;
    private boolean mousePressed;

    public ScreenshotItem(ScreenshotInfo info, ScreenshotsPanel screenshotsPanel) {
        super(new MigLayout("wrap, flowy", "[center]", "[center][bottom]"));

        JLabel imageLabel = new JLabel(new ImageIcon(info.getImage()));
        this.add(imageLabel);

        JLabel textLabel = new JLabel(info.getText());
        this.add(textLabel);

        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));

        this.setOpaque(false);
        this.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseListener listener = new MouseListenerBuilder()
            .mouseEntered(e -> {
                this.mouseOver = true;
                this.repaint();
            })
            .mouseExited(e -> {
                this.mouseOver = false;
                this.repaint();
            })
            .mousePressed(e -> {
                this.mousePressed = true;
                this.repaint();
            })
            .mouseReleased(e -> {
                this.mousePressed = false;
                this.repaint();
            })
            .mouseClicked(e -> {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    SwingUtils.startWorker(() -> {
                        OperatingSystem.open(info.getFilePath());
                    });
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    Language language = CRLauncher.getInstance().getLanguage();
                    LanguageSection section = language.getSection("gui.instanceSettingsDialog.screenshotsTab");

                    JPopupMenu popupMenu = new JPopupMenu();

                    JMenuItem copyImageItem = new JMenuItem(section.getString("copyImage"));
                    copyImageItem.addActionListener(copy -> {
                        SwingUtils.startWorker(() -> {
                            OperatingSystem.copyToClipboard(info.getOriginalImage());
                        });
                    });

                    JMenuItem copyFileItem = new JMenuItem(section.getString("copyFile"));
                    copyFileItem.addActionListener(copy -> {
                        SwingUtils.startWorker(() -> {
                            OperatingSystem.copyToClipboard(info.getFilePath());
                        });
                    });

                    // TODO: add confirmation dialog
                    JMenuItem deleteFileItem = new JMenuItem(section.getString("delete"));
                    deleteFileItem.addActionListener(delete -> {
                        SwingUtils.startWorker(() -> {
                            try {
                                FileUtils.delete(info.getFilePath());
                                screenshotsPanel.removeScreenshot(ScreenshotItem.this);
                            } catch (IOException ex) {
                                Log.error("Could not delete screenshot", ex);
                            }
                        });
                    });

                    JMenuItem renameFileItem = new JMenuItem(section.getString("rename"));
                    renameFileItem.addActionListener(rename -> {
                        SwingUtils.startWorker(() -> {
                            String oldFileName = info.getFilePath().getFileName().toString();

                            String newFileName = MessageBox.showInputMessage(
                                CRLauncher.frame,
                                section.getString("rename"),
                                section.getString("renameMessage"),
                                oldFileName
                            );

                            if (newFileName == null || newFileName.isEmpty() || oldFileName.equals(newFileName)) {
                                return;
                            }

                            try {
                                FileUtils.renameFile(info.getFilePath(), newFileName);
                            } catch (IOException ex) {
                                Log.error("Could not rename screenshot", ex);
                            }

                            SwingUtilities.invokeLater(() -> {
                                textLabel.setText(newFileName);
                            });
                        });
                    });

                    popupMenu.add(copyImageItem);
                    popupMenu.add(copyFileItem);
                    popupMenu.add(renameFileItem);
                    popupMenu.addSeparator();
                    popupMenu.add(deleteFileItem);

                    popupMenu.show(ScreenshotItem.this, e.getX(), e.getY());
                }
            })
            .build();

        this.addMouseListener(listener);
    }

    protected void paintBackground(Graphics g) {
        Color color = this.defaultColor;

        if (this.mouseOver) {
            color = this.hoveredColor;
        }

        if (this.mousePressed) {
            color = this.pressedColor;
        }

        g.setColor(color);
        g.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 10, 10);
    }

    @Override
    protected void paintComponent(Graphics g) {
        this.paintBackground(g);

        super.paintComponent(g);
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
