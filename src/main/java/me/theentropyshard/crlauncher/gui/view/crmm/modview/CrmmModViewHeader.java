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

package me.theentropyshard.crlauncher.gui.view.crmm.modview;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.crmm.ModInfo;
import me.theentropyshard.crlauncher.gui.components.Card;
import me.theentropyshard.crlauncher.gui.utils.GifIcon;
import me.theentropyshard.crlauncher.gui.utils.Worker;
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
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class CrmmModViewHeader extends Card {
    private final JLabel iconLabel;
    private final JLabel nameLabel;
    private final JLabel descriptionLabel;
    private final JButton downloadButton;

    public CrmmModViewHeader(ModInfo modInfo) {
        this.setLayout(new BorderLayout());

        this.iconLabel = new JLabel();
        this.iconLabel.setPreferredSize(new Dimension(96, 96));
        this.add(this.iconLabel, BorderLayout.WEST);

        this.fetchIcon(modInfo);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(0, 10, 0, 0));

        JPanel nameDescriptionPanel = new JPanel(new GridLayout(2, 1));
        nameDescriptionPanel.setOpaque(false);

        this.nameLabel = new JLabel("<html><b>" + modInfo.getName() + "</b></html>");
        nameDescriptionPanel.add(this.nameLabel);

        this.descriptionLabel = new JLabel("<html><p>" + modInfo.getDescription() + "</p></html>");
        this.descriptionLabel.setFont(this.descriptionLabel.getFont().deriveFont(14.0f));
        nameDescriptionPanel.add(this.descriptionLabel);

        centerPanel.add(nameDescriptionPanel, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.setBorder(new EmptyBorder(0, -5, -5, 0));
        statsPanel.setOpaque(false);

        Language language = CRLauncher.getInstance().getLanguage();

        String downloads = modInfo.getDownloads();
        int downloadsLastDigit = Integer.parseInt(downloads) % 10;
        String downloadsText;
        if (downloadsLastDigit == 1) {
            downloadsText = downloads + " " + language.getString("gui.searchCRMMModsDialog.download1");
        } else if (downloadsLastDigit == 2 || downloadsLastDigit == 3 || downloadsLastDigit == 4) {
            downloadsText = downloads + " " + language.getString("gui.searchCRMMModsDialog.downloads234");
        } else {
            downloadsText = downloads + " " + language.getString("gui.searchCRMMModsDialog.downloads");
        }
        JLabel downloadsLabel = new JLabel(downloadsText);
        downloadsLabel.setFont(downloadsLabel.getFont().deriveFont(14.0f));
        statsPanel.add(downloadsLabel);

        String followers = modInfo.getFollowers();
        int followersLastDigit = Integer.parseInt(followers) % 10;
        String followersText;
        if (followersLastDigit == 1) {
            followersText = followers + " " + language.getString("gui.searchCRMMModsDialog.follower1");
        } else if (followersLastDigit == 2 || followersLastDigit == 3 || followersLastDigit == 4) {
            followersText = followers + " " + language.getString("gui.searchCRMMModsDialog.followers234");
        } else {
            followersText = followers + " " + language.getString("gui.searchCRMMModsDialog.followers");
        }
        JLabel followersLabel = new JLabel(followersText);
        followersLabel.setFont(followersLabel.getFont().deriveFont(14.0f));
        statsPanel.add(followersLabel);


        centerPanel.add(statsPanel, BorderLayout.SOUTH);

        this.add(centerPanel, BorderLayout.CENTER);

        this.downloadButton = new JButton(language.getString("gui.searchCRMMModsDialog.downloadButton"));

        Box verticalBox = Box.createVerticalBox();
        verticalBox.setOpaque(false);
        verticalBox.add(Box.createVerticalGlue());
        verticalBox.add(this.downloadButton);
        verticalBox.add(Box.createVerticalGlue());

        this.add(verticalBox, BorderLayout.EAST);
    }

    private void fetchIcon(ModInfo modInfo) {
        new Worker<Icon, Void>("fetching icon for mod " + modInfo.getName()) {
            @Override
            protected Icon work() throws Exception {
                OkHttpClient httpClient = CRLauncher.getInstance().getHttpClient();

                String iconUrl = modInfo.getIconUrl();

                if (iconUrl == null) {
                    return ModNoIcon.getInstance();
                }

                Request request = new Request.Builder()
                    .url(iconUrl)
                    .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (iconUrl.endsWith(".gif")) {
                        return new GifIcon(Objects.requireNonNull(response.body()).byteStream(), 96, 96, CrmmModViewHeader.this);
                    }

                    BufferedImage bufferedImage = ImageIO.read(Objects.requireNonNull(response.body()).byteStream());
                    BufferedImage scaledImage = ImageUtils.toBufferedImage(bufferedImage.getScaledInstance(96, 96, BufferedImage.SCALE_SMOOTH));

                    BufferedImage clippedImage = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g2d = clippedImage.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    TexturePaint paint = new TexturePaint(scaledImage, new Rectangle(96, 96));
                    g2d.setPaint(paint);
                    g2d.fill(new RoundRectangle2D.Double(0, 0, 96, 96, 10, 10));

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
                    icon = ModNoIcon.getInstance();
                }

                CrmmModViewHeader.this.iconLabel.setIcon(icon);
            }
        }.execute();
    }

    public JButton getDownloadButton() {
        return this.downloadButton;
    }
}
