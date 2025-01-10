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

package me.theentropyshard.crlauncher.gui.console;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

public class ConsoleLinesLimiter implements DocumentListener {
    private int maximumLines;

    public ConsoleLinesLimiter(int maximumLines) {
        this.maximumLines = maximumLines;
    }


    public void insertUpdate(final DocumentEvent e) {
        SwingUtilities.invokeLater(() -> {
            try {
                Document document = e.getDocument();
                Element root = document.getDefaultRootElement();

                while (root.getElementCount() > this.maximumLines) {
                    document.remove(0, root.getElement(0).getEndOffset());
                }
            } catch (BadLocationException ignored) {

            }
        });
    }

    public void removeUpdate(DocumentEvent e) {

    }

    public void changedUpdate(DocumentEvent e) {

    }

    public int getMaximumLines() {
        return this.maximumLines;
    }

    public void setMaximumLines(int maximumLines) {
        this.maximumLines = maximumLines;
    }
}