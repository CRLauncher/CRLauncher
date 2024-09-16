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

package me.theentropyshard.crlauncher.gui.dialogs.crmm;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.crmm.model.Mod;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.logging.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ModCard extends JPanel {
    private static final BufferedImage DEFAULT_ICON = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);

    private final JLabel iconLabel;
    private final JLabel nameLabel;

    private final int border = 12;

    private Color defaultColor;
    private Color hoveredColor;
    private Color pressedColor;

    private boolean mouseOver;
    private boolean mousePressed;

    public ModCard(Mod mod) {
        super(new BorderLayout());

        this.iconLabel = new JLabel(new ImageIcon(ModCard.DEFAULT_ICON));
        this.nameLabel = new JLabel(mod.getName());

        this.fetchIcon(mod);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(this.iconLabel, BorderLayout.WEST);
        topPanel.add(this.nameLabel, BorderLayout.CENTER);

        this.add(topPanel, BorderLayout.CENTER);

        this.setOpaque(false);
        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));

        this.setBorder(new EmptyBorder(
            this.border,
            this.border,
            this.border,
            this.border
        ));

        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                ModCard.this.mouseOver = true;
                ModCard.this.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ModCard.this.mouseOver = false;
                ModCard.this.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                ModCard.this.mousePressed = true;
                ModCard.this.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                ModCard.this.mousePressed = false;
                ModCard.this.repaint();
            }
        });
    }

    private void fetchIcon(Mod mod) {
        new Worker<Icon, Void>("fetching icon for mod " + mod.getName()) {
            @Override
            protected Icon work() throws Exception {
                OkHttpClient httpClient = CRLauncher.getInstance().getHttpClient();

                Request request = new Request.Builder()
                    .url(mod.getIcon())
                    .build();

                // TODO: refactor this
                try (Response response = httpClient.newCall(request).execute()) {
                    InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
                    BufferedImage bufferedImage = ImageIO.read(inputStream);

                    //Image scaledImage = bufferedImage.getScaledInstance(64, 64, BufferedImage.SCALE_SMOOTH);
                    BufferedImage clippedImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = clippedImage.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    TexturePaint paint = new TexturePaint(bufferedImage, new Rectangle(64, 64));
                    Clipper clipper = new Clipper(new Rectangle(64, 64));
                    g2d.setPaint(paint);
                    g2d.fill(clipper.clip(new RoundRectangle2D.Double(0, 0, 64, 64, 10, 10)));
                    g2d.dispose();

                    BufferedImage resultImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D rg = resultImage.createGraphics();
                    rg.drawImage(clippedImage, 0, 0, null);
                    rg.dispose();

                    return new ImageIcon(resultImage);
                }
            }

            @Override
            protected void done() {
                Icon icon = null;

                try {
                    icon = this.get();
                } catch (InterruptedException | ExecutionException e) {
                    Log.error(e);
                }

                ModCard.this.iconLabel.setIcon(icon);
            }
        }.execute();
    }

    private static class Clipper {
        private final Shape shape;

        public Clipper(Shape shape) {
            this.shape = shape;
        }

        public Shape clip(Shape other) {
            Area a = new Area(this.shape);
            a.intersect(new Area(other));

            return a;
        }
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
        g2d.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 10, 10);
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
