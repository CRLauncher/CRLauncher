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

package me.theentropyshard.crlauncher.gui.view.devlog;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.gui.components.MouseListenerBuilder;
import me.theentropyshard.crlauncher.gui.utils.GifIcon;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.gui.view.crmm.ModCard;
import me.theentropyshard.crlauncher.gui.view.crmm.ModNoIcon;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.ImageUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class DevlogCard extends JPanel {
    private final int border = 12;

    private Color defaultColor;
    private Color hoveredColor;
    private Color pressedColor;

    private boolean mouseOver;
    private boolean mousePressed;

    public DevlogCard(PostInfo postInfo, Runnable onClick) {
        super(new BorderLayout());

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel titleLabel = new JLabel(postInfo.getTitle());
        titleLabel.setFont(titleLabel.getFont().deriveFont(20.0f));

        JLabel metaLabel = new JLabel(postInfo.getDate() + ", by " + postInfo.getAuthor());
        JLabel summaryLabel = new JLabel("<html><p>" + postInfo.getSummary() + "</p></html>");

        centerPanel.add(titleLabel);
        centerPanel.add(metaLabel);
        centerPanel.add(Box.createVerticalStrut(25));
        centerPanel.add(summaryLabel);

        this.add(centerPanel, BorderLayout.CENTER);

        if (postInfo.getImageUrl() != null) {
            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setOpaque(false);

            JLabel imageLabel = new JLabel();
            rightPanel.add(imageLabel, BorderLayout.CENTER);

            this.add(rightPanel, BorderLayout.EAST);

            this.fetchImage(postInfo, imageLabel);
        }

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
                onClick.run();
            })
            .build();

        this.addMouseListener(listener);
    }

    @Override
    public void updateUI() {
        super.updateUI();

        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));
    }

    private void fetchImage(PostInfo postInfo, JLabel imageLabel) {
        new Worker<ImageIcon, Void>("loading icon for post " + postInfo.getTitle()) {
            @Override
            protected ImageIcon work() throws Exception {
                OkHttpClient httpClient = CRLauncher.getInstance().getHttpClient();

                String imageUrl = postInfo.getImageUrl();

                Request request = new Request.Builder()
                    .url(imageUrl)
                    .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    BufferedImage bufferedImage = ImageIO.read(Objects.requireNonNull(response.body()).byteStream());

                    int width = bufferedImage.getWidth();
                    int height = bufferedImage.getHeight();

                    BufferedImage clippedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g2d = clippedImage.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    TexturePaint paint = new TexturePaint(bufferedImage, new Rectangle(width, height));
                    g2d.setPaint(paint);
                    g2d.fill(new RoundRectangle2D.Double(0, 0, width, height, 10, 10));

                    g2d.dispose();

                    return new ImageIcon(clippedImage);
                }
            }

            @Override
            protected void done() {
                ImageIcon imageIcon;

                try {
                    imageIcon = this.get();
                } catch (InterruptedException | ExecutionException e) {
                    Log.error("Unexpected error", e);

                    return;
                }

                imageLabel.setIcon(imageIcon);
            }
        }.execute();
    }

    public static String formatUnit(String key, int count) {
        Language language = CRLauncher.getInstance().getLanguage();
        String ago = language.getString("general.time.ago");

        return switch (count) {
            case 1 -> count + " " + language.getString(key + "1") + " " + ago;
            case 2, 3, 4 -> count + " " + language.getString(key + "s234") + " " + ago;
            default -> count + " " + language.getString(key + "s") + " " + ago;
        };
    }

    public static String getAgoFromNow(Temporal temporal) {
        OffsetDateTime now = OffsetDateTime.now();

        int years = (int) ChronoUnit.YEARS.between(temporal, now);
        if (years != 0) {
            return DevlogCard.formatUnit("general.time.units.year", years);
        }

        int months = (int) ChronoUnit.MONTHS.between(temporal, now);
        if (months != 0) {
            return DevlogCard.formatUnit("general.time.units.month", months);
        }

        int weeks = (int) ChronoUnit.WEEKS.between(temporal, now);
        if (weeks != 0) {
            return DevlogCard.formatUnit("general.time.units.week", weeks);
        }

        int days = (int) ChronoUnit.DAYS.between(temporal, now);
        if (days != 0) {
            return DevlogCard.formatUnit("general.time.units.day", days);
        }

        int hours = (int) ChronoUnit.HOURS.between(temporal, now);
        if (hours != 0) {
            return DevlogCard.formatUnit("general.time.units.hour", hours);
        }

        int minutes = (int) ChronoUnit.MINUTES.between(temporal, now);
        if (minutes != 0) {
            return DevlogCard.formatUnit("general.time.units.minute", minutes);
        }

        int seconds = (int) ChronoUnit.SECONDS.between(temporal, now);

        return DevlogCard.formatUnit("general.time.units.second", seconds);
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
