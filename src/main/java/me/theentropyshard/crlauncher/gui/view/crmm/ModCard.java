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
import me.theentropyshard.crlauncher.crmm.ModInfo;
import me.theentropyshard.crlauncher.gui.utils.ClickThroughListener;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.ImageUtils;
import me.theentropyshard.crlauncher.utils.StringUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ModCard extends JPanel {
    private static final Icon EMPTY_ICON = new ImageIcon(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));
    private static final Icon NO_ICON = SwingUtils.getIcon("/assets/images/icons/mod_no_icon.png");

    private static final int DESCRIPTION_LIMIT = 100;
    private static final int MAX_HEIGHT = 148;

    private final JLabel iconLabel;
    private final JLabel nameLabel;
    private final JTextPane descriptionArea;

    private final int border = 12;

    private Color defaultColor;
    private Color hoveredColor;
    private Color pressedColor;

    private boolean mouseOver;
    private boolean mousePressed;

    public ModCard(ModInfo modInfo) {
        super(new BorderLayout());

        this.iconLabel = new JLabel(ModCard.EMPTY_ICON);

        this.nameLabel = new ModNameAuthorLabel(modInfo);
        this.nameLabel.setBorder(new EmptyBorder(0, 12, 0, 0));
        this.nameLabel.setFont(this.nameLabel.getFont().deriveFont(24.0f));

        this.fetchIcon(modInfo);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        JPanel nameDescriptionPanel = new JPanel(new GridLayout(2, 1));
        nameDescriptionPanel.setOpaque(false);
        nameDescriptionPanel.add(this.nameLabel, BorderLayout.CENTER);

        this.descriptionArea = new JTextPane();
        this.descriptionArea.setEditable(false);
        this.descriptionArea.setBorder(new EmptyBorder(0, 12, 0, 0));
        this.descriptionArea.setOpaque(false);
        SwingUtils.removeMouseListeners(this.descriptionArea);
        this.descriptionArea.addMouseListener(new ClickThroughListener(this));

        String summary = modInfo.getDescription();
        if (summary.length() > ModCard.DESCRIPTION_LIMIT) {
            this.descriptionArea.setText(summary.substring(0, ModCard.DESCRIPTION_LIMIT - 3) + "...");
        } else {
            this.descriptionArea.setText(summary);
        }

        this.descriptionArea.setToolTipText(summary);

        this.descriptionArea.setFont(this.descriptionArea.getFont().deriveFont(14.0f));
        nameDescriptionPanel.add(this.descriptionArea);

        centerPanel.add(nameDescriptionPanel, BorderLayout.NORTH);

        JPanel tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tagsPanel.setOpaque(false);
        tagsPanel.setBorder(new EmptyBorder(0, 7, 0, 0));
        for (String category : modInfo.getFeaturedCategories()) {
            //tagsPanel.add(new JLabel(category, SwingUtils.getIcon("/assets/images/icons/utility_icon.png"), SwingConstants.LEFT));
            tagsPanel.add(new JLabel(StringUtils.capitalize(category).replace("_", " ")));
        }
        for (String loader : modInfo.getLoaders()) {
            //tagsPanel.add(new JLabel(loader, SwingUtils.getIcon("/assets/images/icons/quilt_icon.png"), SwingConstants.LEFT));
            tagsPanel.add(new JLabel(StringUtils.capitalize(loader).replace("_", " ")));
        }
        centerPanel.add(tagsPanel, BorderLayout.SOUTH);

        JPanel iconPanel = new JPanel();
        iconPanel.setOpaque(false);
        iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.Y_AXIS));
        iconPanel.add(this.iconLabel);

        this.add(iconPanel, BorderLayout.WEST);
        this.add(centerPanel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);

        JPanel downloadsFollowersPanel = new JPanel(new GridLayout(2, 1));
        downloadsFollowersPanel.setOpaque(false);

        JLabel downloadsLabel = new JLabel(modInfo.getDownloads() + " downloads");
        downloadsLabel.setFont(downloadsLabel.getFont().deriveFont(14.0f));
        downloadsLabel.setHorizontalAlignment(JLabel.RIGHT);
        downloadsFollowersPanel.add(downloadsLabel);

        JLabel followersLabel = new JLabel(modInfo.getFollowers() + " followers");
        followersLabel.setFont(followersLabel.getFont().deriveFont(14.0f));
        followersLabel.setHorizontalAlignment(JLabel.RIGHT);
        downloadsFollowersPanel.add(followersLabel);

        infoPanel.add(downloadsFollowersPanel, BorderLayout.NORTH);

        JLabel updatedLabel = new JLabel("Updated " + ModCard.getAgoFromNow(OffsetDateTime.parse(modInfo.getDateUpdated())));
        updatedLabel.setFont(updatedLabel.getFont().deriveFont(14.0f));
        updatedLabel.setHorizontalAlignment(JLabel.RIGHT);
        infoPanel.add(updatedLabel, BorderLayout.SOUTH);

        this.add(infoPanel, BorderLayout.EAST);

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

    @Override
    public Dimension getMaximumSize() {
        Dimension maximumSize = super.getMaximumSize();

        return new Dimension(maximumSize.width, ModCard.MAX_HEIGHT);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize();

        return new Dimension(preferredSize.width, ModCard.MAX_HEIGHT);
    }

    public static String getAgoFromNow(Temporal temporal) {
        OffsetDateTime now = OffsetDateTime.now();

        long years = ChronoUnit.YEARS.between(temporal, now);
        if (years == 0) {
            long months = ChronoUnit.MONTHS.between(temporal, now);
            if (months == 0) {
                long weeks = ChronoUnit.WEEKS.between(temporal, now);
                if (weeks == 0) {
                    int days = (int) ChronoUnit.DAYS.between(temporal, now);
                    if (days == 0) {
                        int hours = (int) ChronoUnit.HOURS.between(temporal, now);
                        if (hours == 0) {
                            int minutes = (int) ChronoUnit.MINUTES.between(temporal, now);
                            if (minutes == 0) {
                                int seconds = (int) ChronoUnit.SECONDS.between(temporal, now);

                                return seconds + " seconds ago";
                            } else {
                                if (minutes == 1) {
                                    return "1 minute ago";
                                } else {
                                    return minutes + " minutes ago";
                                }
                            }
                        } else {
                            if (hours == 1) {
                                return "1 hour ago";
                            } else {
                                return hours + " hours ago";
                            }
                        }
                    } else {
                        if (days == 1) {
                            return "yesterday";
                        } else {
                            return days + " days ago";
                        }
                    }
                } else {
                    if (weeks == 1) {
                        return "1 week ago";
                    } else {
                        return weeks + " weeks ago";
                    }
                }
            } else {
                if (months == 1) {
                    return "1 month ago";
                } else {
                    return months + " months ago";
                }
            }
        } else {
            if (years == 1) {
                return "1 year ago";
            } else {
                return years + " years ago";
            }
        }
    }

    private void fetchIcon(ModInfo modInfo) {
        new Worker<Icon, Void>("fetching icon for mod " + modInfo.getName()) {
            @Override
            protected Icon work() throws Exception {
                OkHttpClient httpClient = CRLauncher.getInstance().getHttpClient();

                String iconUrl = modInfo.getIconUrl();

                if (iconUrl == null) {
                    return ModCard.NO_ICON;
                }

                Request request = new Request.Builder()
                    .url(iconUrl)
                    .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    BufferedImage bufferedImage = ImageIO.read(Objects.requireNonNull(response.body()).byteStream());
                    BufferedImage scaledImage = ImageUtils.toBufferedImage(bufferedImage.getScaledInstance(64, 64, BufferedImage.SCALE_SMOOTH));

                    BufferedImage clippedImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g2d = clippedImage.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    TexturePaint paint = new TexturePaint(scaledImage, new Rectangle(64, 64));
                    g2d.setPaint(paint);
                    g2d.fill(new RoundRectangle2D.Double(0, 0, 64, 64, 10, 10));

                    g2d.dispose();

                    return new ImageIcon(clippedImage);
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

                if (icon == null) {
                    icon = ModCard.NO_ICON;
                }

                ModCard.this.iconLabel.setIcon(icon);
            }
        }.execute();
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
