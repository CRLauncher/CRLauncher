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
import me.theentropyshard.crlauncher.network.HttpRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DevlogLoader extends Worker<Void, PostInfo> {
    private static final String DEVLOG_URL = "https://finalforeach.itch.io/cosmic-reach/devlog";

    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    private final DevlogView view;

    public DevlogLoader(DevlogView view) {
        super("loading devlog");

        this.view = view;
    }

    @Override
    protected Void work() throws Exception {
        Document document = this.fetchDocument();

        List<PostInfo> postInfos;

        if (document == null) {
            postInfos = Collections.emptyList();
        } else {
            postInfos = PostInfo.fromDocument(document);
        }

        for (PostInfo postInfo : postInfos) {
            this.publish(postInfo);

            Thread.sleep(20L);
        }

        return null;
    }

    @Override
    protected void process(List<PostInfo> chunks) {
        for (PostInfo postInfo : chunks) {
            this.view.addDevlogCard(new DevlogCard(postInfo, () -> this.view.loadPost(postInfo)));
        }
    }

    private Document fetchDocument() throws IOException {
        try (HttpRequest request = new HttpRequest(CRLauncher.getInstance().getHttpClient())) {
            String html = request.asString(DevlogLoader.DEVLOG_URL);

            if (request.code() == DevlogLoader.HTTP_TOO_MANY_REQUESTS) {
                return null;
            }

            return Jsoup.parse(html);
        }
    }
}
