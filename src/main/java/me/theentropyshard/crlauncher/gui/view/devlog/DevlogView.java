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
import me.theentropyshard.crlauncher.gui.FlatSmoothScrollPaneUI;
import me.theentropyshard.crlauncher.gui.utils.ScrollablePanel;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.network.HttpRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class DevlogView extends JPanel {
    private final JPanel devlogCardsPanel;

    public DevlogView() {
        super(new BorderLayout());

        this.setBorder(new EmptyBorder(10, 10, 10, 10));

        this.devlogCardsPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        this.devlogCardsPanel.setBorder(new EmptyBorder(0, 0, 0, 10));

        JPanel borderPanel = new ScrollablePanel(new BorderLayout());
        borderPanel.add(this.devlogCardsPanel, BorderLayout.PAGE_START);

        JScrollPane modCardsScrollPane = new JScrollPane(borderPanel);
        modCardsScrollPane.setUI(new FlatSmoothScrollPaneUI());
        modCardsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        modCardsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(modCardsScrollPane, BorderLayout.CENTER);

        new Worker<Void, PostInfo>("loading devlogs") {
            @Override
            protected Void work() throws Exception {
                Document document = this.fetchDocument();

                List<PostInfo> postInfos = PostInfo.fromDocument(document);

                for (PostInfo postInfo : postInfos) {
                    this.publish(postInfo);
                }

                return null;
            }

            @Override
            protected void process(List<PostInfo> chunks) {
                for (PostInfo postInfo : chunks) {
                    DevlogView.this.addDevlogCard(new DevlogCard(postInfo));
                }
            }

            private Document fetchDocument() throws IOException {
                try (HttpRequest request = new HttpRequest(CRLauncher.getInstance().getHttpClient())) {
                    String html = request.asString("https://finalforeach.itch.io/cosmic-reach/devlog");

                    return Jsoup.parse(html);
                }
            }
        }.execute();
    }

    public void addDevlogCard(DevlogCard card) {
        this.devlogCardsPanel.add(card);
    }

    public void reloadLanguage() {

    }
}
