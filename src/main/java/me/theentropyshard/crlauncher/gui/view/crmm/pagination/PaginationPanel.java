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

package me.theentropyshard.crlauncher.gui.view.crmm.pagination;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

public class PaginationPanel extends JPanel {
    private final ButtonGroup buttonGroup;
    private final Set<PageSelectedListener> listeners;

    private final NextPrevPageButton prevPageButton;
    private final NextPrevPageButton nextPageButton;

    public PaginationPanel() {
        this.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));

        this.buttonGroup = new ButtonGroup();
        this.listeners = new HashSet<>();

        this.prevPageButton = new NextPrevPageButton("<");
        this.nextPageButton = new NextPrevPageButton(">");

        this.add(this.prevPageButton);
        this.add(this.nextPageButton);
    }

    public void addButton(PageButton button) {
        this.buttonGroup.add(button);
        this.add(button, this.getComponentCount() - 1);
        button.addActionListener(e -> {
            if (button.isSelected()) {
                this.listeners.forEach(listener -> listener.onPageSelected(button.getPage()));
            }
        });
        this.revalidate();
    }

    public void selectPage(int page) {
        this.buttonGroup.getElements().asIterator().forEachRemaining(button -> {
            if (((PageButton) button).getPage() == page) {
                this.buttonGroup.setSelected(button.getModel(), true);
                this.revalidate();
            }
        });
    }

    public void clearAll() {
        this.buttonGroup.getElements().asIterator().forEachRemaining(this.buttonGroup::remove);
        this.removeAll();
        this.add(this.prevPageButton);
        this.add(this.nextPageButton);
        this.revalidate();
    }

    public void addPageSelectedListener(PageSelectedListener listener) {
        this.listeners.add(listener);
    }

    public void addPrevPageButtonListener(ActionListener listener) {
        this.prevPageButton.addActionListener(listener);
    }

    public void addNextPageButtonListener(ActionListener listener) {
        this.nextPageButton.addActionListener(listener);
    }

    public interface PageSelectedListener {
        void onPageSelected(int page);
    }
}
