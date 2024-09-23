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

package me.theentropyshard.crlauncher.gui.view;

import me.theentropyshard.crlauncher.BuildConfig;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.utils.OperatingSystem;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;

public class AboutView extends JPanel {
    private static final String GITHUB_LINK = "https://github.com/CRLauncher/CRLauncher";

    private final JLabel line1;
    private final JLabel line2;
    private final JTextPane line3;

    public AboutView() {
        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.VERTICAL;

        Language language = CRLauncher.getInstance().getLanguage();

        this.line1 = new JLabel(language.getString("gui.aboutView.lines.line1").replace("$$LAUNCHER_VERSION$$", BuildConfig.APP_VERSION));
        gbc.gridy++;
        this.add(this.line1, gbc);

        this.line2 = new JLabel(language.getString("gui.aboutView.lines.line2"));
        gbc.gridy++;
        this.add(this.line2, gbc);

        this.line3 = new JTextPane();
        this.line3.setEditorKit(new HTMLEditorKit());
        this.line3.setContentType("text/html");
        this.line3.setEditable(false);

        String line3Text = language.getString("gui.aboutView.lines.line3");
        this.line3.setText("<html>" + line3Text.replace("$$LAUNCHER_URL$$", "<a href=\"" + AboutView.GITHUB_LINK + "\">" + AboutView.GITHUB_LINK + "</a>") + "</html>");
        this.line3.addHyperlinkListener(e -> {
            if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
                return;
            }

            OperatingSystem.browse(AboutView.GITHUB_LINK);
        });

        gbc.gridy++;
        this.add(this.line3, gbc);
    }

    public void reloadLanguage() {
        Language language = CRLauncher.getInstance().getLanguage();

        this.line1.setText(language.getString("gui.aboutView.lines.line1").replace("$$LAUNCHER_VERSION$$", BuildConfig.APP_VERSION));
        this.line2.setText(language.getString("gui.aboutView.lines.line2"));

        String line3Text = language.getString("gui.aboutView.lines.line3");
        this.line3.setText("<html>" + line3Text.replace("$$LAUNCHER_URL$$", "<a href=\"" + AboutView.GITHUB_LINK + "\">" + AboutView.GITHUB_LINK + "</a>") + "</html>");
    }
}
