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