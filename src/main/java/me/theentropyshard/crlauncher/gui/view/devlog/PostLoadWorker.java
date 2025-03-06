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
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.network.HttpRequest;
import org.commonmark.Extension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PostLoadWorker extends Worker<String, Void> {
    private final PostInfo postInfo;
    private final JLabel postTitlePane;
    private final DevlogPane changelogPane;

    public PostLoadWorker(PostInfo postInfo, JLabel postTitleLabel, DevlogPane devlogPane) {
        super("loading post: " + postInfo.getPostUrl());

        this.postInfo = postInfo;
        this.postTitlePane = postTitleLabel;
        this.changelogPane = devlogPane;
    }

    @Override
    protected String work() throws Exception {
        try (HttpRequest request = new HttpRequest(CRLauncher.getInstance().getHttpClient())) {
            Element section = Jsoup.parse(request.asString(this.postInfo.getPostUrl()))
                .selectXpath("/html/body/div[1]/div/div[2]/div[1]/div[1]/section[2]").get(0);

            StringBuilder markdown = new StringBuilder();

            if (section.child(0).tagName().equals("ul")) {
                markdown.append(section.child(0).toString()).append("\r\n");
            } else {
                for (Element paragraph : section.children()) {
                    if (paragraph.childrenSize() >= 1 && paragraph.child(0).tagName().equals("img")) {
                        Element imgElement = paragraph.child(0);

                        markdown.append("\r\n![img](").append(imgElement.attr("src")).append("){width=568}")
                            .append("\r\n").append("\r\n");
                    } else {
                        if (paragraph.tagName().equals("ul")) {
                            markdown.append(paragraph).append("\r\n");
                        } else {
                            String text = paragraph.ownText();

                            if (text.startsWith("* ")) {
                                markdown.append("- ").append(text.substring(2)).append("\r\n");
                            } else {
                                markdown.append("\r\n").append(text).append("\r\n");
                            }
                        }
                    }
                }
            }

            markdown.deleteCharAt(markdown.length() - 1);

            List<Extension> extensions = List.of(ImageAttributesExtension.create());

            return HtmlRenderer.builder().extensions(extensions).build().render(Parser.builder().extensions(extensions).build().parse(markdown.toString()));
        }
    }

    @Override
    protected void done() {
        String html;

        try {
            html = this.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.error("Unexpected error", e);

            this.postTitlePane.setText(this.postInfo.getTitle() + " - Error occurred, try again");

            return;
        }

        this.postTitlePane.setText(this.postInfo.getTitle());
        this.changelogPane.setHtml(html);
    }
}