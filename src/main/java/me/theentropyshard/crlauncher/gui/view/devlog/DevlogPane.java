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

import me.theentropyshard.crlauncher.gui.BrowseHyperlinkListener;
import me.theentropyshard.crlauncher.gui.utils.ColorUtils;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;

public class DevlogPane extends JTextPane {
    private String postHtml;

    public DevlogPane() {
        this.addHyperlinkListener(new BrowseHyperlinkListener());
        this.setFont(this.getFont().deriveFont(14.0f));
        this.setContentType("text/html");
        this.setEditorKit(new HTMLEditorKit());
        this.setEditable(false);
    }

    @Override
    public void updateUI() {
        super.updateUI();

        this.setHtml(this.postHtml);
    }

    public void setHtml(String html) {
        this.postHtml = html;

        String fullHtml = "<html>" +
            "<head>" +
            "<style>" +
            "code {" +
            "background: " + ColorUtils.colorToHex(UIManager.getColor("InstanceItem.defaultColor")) + ";" +
            "}" +
            "</head>" +
            "<body>" +
            html +
            "</body>" +
            "</html>";

        this.setText(fullHtml);
    }
}
