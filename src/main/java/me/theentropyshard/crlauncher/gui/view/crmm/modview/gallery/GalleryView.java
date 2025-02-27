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

package me.theentropyshard.crlauncher.gui.view.crmm.modview.gallery;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.crmm.model.project.GalleryImage;
import me.theentropyshard.crlauncher.crmm.model.project.Project;
import me.theentropyshard.crlauncher.gui.layouts.WrapLayout;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.utils.ImageUtils;
import okhttp3.Request;
import okhttp3.Response;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class GalleryView extends JPanel {
    private final Project project;
    private final CosmicInstance instance;

    private final JPanel galleryImagesPanel;

    public GalleryView(Project project, CosmicInstance instance) {
        this.project = project;
        this.instance = instance;

        this.setLayout(new BorderLayout());

        this.galleryImagesPanel = new JPanel(new WrapLayout(WrapLayout.LEFT, 8, 8));

        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.setBorder(new EmptyBorder(-8, -8, -8, -8));
        borderPanel.add(this.galleryImagesPanel, BorderLayout.CENTER);

        this.add(borderPanel, BorderLayout.CENTER);
    }

    public void loadImages() {
        this.galleryImagesPanel.removeAll();

        new Worker<Void, GalleryImageInfo>("loading gallery images for project " + this.project.getName()) {
            @Override
            protected Void work() throws Exception {
                for (GalleryImage galleryImage : GalleryView.this.project.getGallery()) {
                    Request request = new Request.Builder()
                        .url(galleryImage.getImageThumbnail())
                        .build();

                    try (Response response = CRLauncher.getInstance().getHttpClient().newCall(request).execute()) {
                        this.publish(new GalleryImageInfo(
                            ImageUtils.fitImageAndResize(ImageIO.read(response.body().byteStream()), 192, 108),
                            galleryImage.getName(),
                            galleryImage.isFeatured(),
                            galleryImage.getDateCreated()
                        ));
                    }
                }

                return null;
            }

            @Override
            protected void process(List<GalleryImageInfo> imageInfos) {
                for (GalleryImageInfo imageInfo : imageInfos) {
                    GalleryView.this.galleryImagesPanel.add(new GalleryImageItem(imageInfo));
                    GalleryView.this.galleryImagesPanel.revalidate();
                }
            }
        }.execute();
    }
}
