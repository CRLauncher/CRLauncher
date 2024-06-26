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

package me.theentropyshard.crlauncher.gui;

import me.theentropyshard.crlauncher.utils.OperatingSystem;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.WindowListener;

public class LauncherConsole {
    private static final int DEFAULT_X = 80;
    private static final int DEFAULT_Y = 80;

    private final JTextPane textPane;
    private final JFrame frame;

    public static LauncherConsole instance;

    public LauncherConsole() {
        this.textPane = new JTextPane();
        this.textPane.setPreferredSize(new Dimension(480, 280));
        this.textPane.setFont(this.textPane.getFont().deriveFont(14.0f));
        this.textPane.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(this.textPane);

        JButton copyLogButton = new JButton("Copy log to clipboard");
        copyLogButton.addActionListener(e -> {
            OperatingSystem.copyToClipboard(this.textPane.getText());
        });

        this.frame = new JFrame("CRLauncher console");
        this.frame.add(scrollPane, BorderLayout.CENTER);
        this.frame.add(copyLogButton, BorderLayout.SOUTH);
        this.frame.pack();
        this.frame.setLocation(LauncherConsole.DEFAULT_X, LauncherConsole.DEFAULT_Y);
    }

    public void setVisible(boolean visibility) {
        this.frame.setVisible(visibility);
    }

    public void addWindowListener(WindowListener listener) {
        this.frame.addWindowListener(listener);
    }

    public void addLine(String line) {
        Document document = this.textPane.getDocument();
        try {
            int length = document.getLength();

            if (!line.endsWith("\n")) {
                line += "\n";
            }

            document.insertString(length, line, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
