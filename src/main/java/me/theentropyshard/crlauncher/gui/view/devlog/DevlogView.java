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

import me.theentropyshard.crlauncher.gui.FlatSmoothScrollPaneUI;
import me.theentropyshard.crlauncher.gui.components.SmoothScrollPane;
import me.theentropyshard.crlauncher.gui.utils.CardLayoutPanel;
import me.theentropyshard.crlauncher.gui.utils.ScrollablePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DevlogView extends JPanel {
    private final CardLayoutPanel cardPanel;
    private final JPanel devlogCardsPanel;
    private final DevlogPostView devlogPostView;

    public DevlogView() {
        super(new BorderLayout());

        this.setBorder(new EmptyBorder(10, 10, 10, 10));

        this.cardPanel = new CardLayoutPanel();
        this.add(this.cardPanel, BorderLayout.CENTER);

        this.devlogCardsPanel = new JPanel(new GridLayout(0, 1, 0, 10)) {
            @Override
            public void scrollRectToVisible(Rectangle aRect) {

            }
        };
        this.devlogCardsPanel.setBorder(new EmptyBorder(0, 0, 0, 10));

        JPanel borderPanel = new ScrollablePanel(new BorderLayout());
        borderPanel.add(this.devlogCardsPanel, BorderLayout.PAGE_START);

        SmoothScrollPane scrollPane = new SmoothScrollPane(borderPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        this.cardPanel.addComponent(scrollPane, "cards");
        this.cardPanel.showComponent("cards");

        this.devlogPostView = new DevlogPostView(() -> {
            this.cardPanel.showComponent("cards");
        });
        this.cardPanel.addComponent(this.devlogPostView, "postView");

        new DevlogLoader(this).execute();
    }

    public void addDevlogCard(DevlogCard card) {
        this.devlogCardsPanel.add(card);
    }

    public void loadPost(PostInfo postInfo) {
        this.devlogPostView.load(postInfo);
        this.cardPanel.showComponent("postView");
    }

    public void reloadLanguage() {

    }
}
