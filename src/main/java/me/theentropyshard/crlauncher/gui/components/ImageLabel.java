/*
 * CRLauncher - https://github.com/CRLauncher/CRLauncher
 * Copyright (C) 2024-2026 CRLauncher
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

package me.theentropyshard.crlauncher.gui.components;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.gui.utils.EmptyIcon;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.logging.Log;

public class ImageLabel extends JLabel {
    private static final Icon EMPTY_ICON = new EmptyIcon();

    private final String imageUrl;

    private boolean painted;

    public ImageLabel(String imageUrl) {
        this.imageUrl = imageUrl;

        this.setIcon(ImageLabel.EMPTY_ICON);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!this.painted) {
            this.painted = true;

            this.loadImage();
        }
    }

    private void loadImage() {
        new Worker<ImageIcon, Void>("loading image from " + this.imageUrl) {
            @Override
            protected ImageIcon work() throws Exception {
                OkHttpClient httpClient = CRLauncher.getInstance().getHttpClient();

                Request request = new Request.Builder()
                    .url(ImageLabel.this.imageUrl)
                    .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    BufferedImage bufferedImage = ImageIO.read(Objects.requireNonNull(response.body()).byteStream());

                    int width = bufferedImage.getWidth();
                    int height = bufferedImage.getHeight();

                    BufferedImage clippedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g2d = clippedImage.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.setPaint(new TexturePaint(bufferedImage, new Rectangle(width, height)));
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

                ImageLabel.this.setIcon(imageIcon);
            }
        }.execute();
    }
}
