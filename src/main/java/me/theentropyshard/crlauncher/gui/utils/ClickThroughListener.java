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

package me.theentropyshard.crlauncher.gui.utils;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class ClickThroughListener extends MouseAdapter {
    private Component parent;

    public ClickThroughListener() {

    }

    public ClickThroughListener(Component parent) {
        this.parent = parent;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        this.propagateEvent(e, MouseEvent.MOUSE_CLICKED);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.propagateEvent(e, MouseEvent.MOUSE_PRESSED);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.propagateEvent(e, MouseEvent.MOUSE_RELEASED);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        this.propagateEvent(e, MouseEvent.MOUSE_ENTERED);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        this.propagateEvent(e, MouseEvent.MOUSE_EXITED);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        this.propagateEvent(e, e.getID());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.propagateEvent(e, MouseEvent.MOUSE_DRAGGED);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.propagateEvent(e, MouseEvent.MOUSE_MOVED);
    }

    private void propagateEvent(MouseEvent e, int eventType) {
        Component child = e.getComponent();

        Component parent;

        if (this.parent == null) {
            parent = child.getParent();
        } else {
            parent = this.parent;
        }

        int deltaX = child.getX() + e.getX();
        int deltaY = child.getY() + e.getY();

        MouseEvent parentMouseEvent = new MouseEvent(parent, eventType, e.getWhen(), e.getModifiersEx(),
            deltaX, deltaY, e.getClickCount(), e.isPopupTrigger());
        parent.dispatchEvent(parentMouseEvent);
    }
}