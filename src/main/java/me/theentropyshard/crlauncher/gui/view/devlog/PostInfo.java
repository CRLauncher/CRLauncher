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

package me.theentropyshard.crlauncher.gui.view.devlog;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostInfo {
    private final String title;
    private final String date;
    private final String author;
    private final String summary;
    private final String imageUrl;
    private final String postUrl;
    private final int likes;

    public PostInfo(String title, String date, String author, String summary, String imageUrl, String postUrl, int likes) {
        this.title = title;
        this.date = date;
        this.author = author;
        this.summary = summary;
        this.imageUrl = imageUrl;
        this.postUrl = postUrl;
        this.likes = likes;
    }

    public static List<PostInfo> fromDocument(Document document) {
        Elements unorderedLists = document.selectXpath("/html/body/div[1]/div/div[2]/div/ul");

        if (unorderedLists.isEmpty()) {
            return Collections.emptyList();
        }

        List<PostInfo> postInfos = new ArrayList<>();

        for (Element postElement : unorderedLists.get(0).children()) {
            Element aElement = postElement.child(0).child(0);
            Element postContent = postElement.child(1).child(0);
            Element metaRow = postContent.child(0);

            String title = aElement.ownText();
            String date = metaRow.child(0).child(0).attr("title");
            String author = metaRow.child(0).child(1).ownText();
            String summary = postContent.child(1).child(0).child(0).ownText();
            String imageUrl = postElement.child(1).childrenSize() == 2 ? postElement.child(1).child(1).attr("src") : null;
            String postUrl = aElement.attr("abs:href");
            int likes = metaRow.childrenSize() == 2 ? Integer.parseInt(metaRow.child(1).ownText()) : 0;

            postInfos.add(new PostInfo(title, date, author, summary, imageUrl, postUrl, likes));
        }

        return postInfos;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDate() {
        return this.date;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getSummary() {
        return this.summary;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public String getPostUrl() {
        return this.postUrl;
    }

    public int getLikes() {
        return this.likes;
    }
}
