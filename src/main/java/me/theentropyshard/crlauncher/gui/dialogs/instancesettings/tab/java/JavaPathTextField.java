package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.java;

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.utils.OperatingSystem;

import javax.swing.*;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JavaPathTextField extends JTextField {
    public JavaPathTextField() {
        Language language = CRLauncher.getInstance().getLanguage();

        this.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
            language.getString("gui.instanceSettingsDialog.javaTab.javaInstallation.textFieldPlaceholder")
                .replace("$$JAVAW$$", OperatingSystem.getCurrent().getJavaExecutableName()));

        JavaPathPopupMenu popupMenu = new JavaPathPopupMenu(this::setText);

        // Popup menu on 'action': pressing enter for desktop
        this.addActionListener(event -> popupMenu.showBelow(this));

        // Popup menu when pressing the key 'show context menu'
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(final KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
                    popupMenu.showBelow(JavaPathTextField.this);
                    event.consume();
                }
            }
        });

        // Popup menu when clicked
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent event) {
                popupMenu.showBelow(JavaPathTextField.this);
            }
        });

        // Auto-close popup menu based on heuristics
        popupMenu.addMenuKeyListener(new MenuKeyListener() {
            private void helper(final MenuKeyEvent event) {
                // Don't close when movement, modifier, or selection keys are used
                // Mostly want to close when a character is written on this field.
                switch (event.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_SHIFT:
                    case KeyEvent.VK_CONTROL:
                    case KeyEvent.VK_ESCAPE:
                    case KeyEvent.VK_SPACE:
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_META:
                    case KeyEvent.VK_KP_LEFT:
                    case KeyEvent.VK_KP_UP:
                    case KeyEvent.VK_KP_RIGHT:
                    case KeyEvent.VK_KP_DOWN:
                    case KeyEvent.VK_ALT_GRAPH:
                        return;
                }

                // Ignore selection keys for KEY_TYPED
                switch (event.getKeyChar()) {
                    case '\n':
                    case ' ':
                        return;
                }

                event.consume();

                // NOTE: needed for manually dispatched key events to work
                SwingUtilities.invokeLater(() -> popupMenu.setVisible(false));

                JavaPathTextField.this.dispatchEvent(new KeyEvent(
                    JavaPathTextField.this,
                    event.getID(),
                    event.getWhen(),
                    event.getModifiersEx(),
                    event.getKeyCode(),
                    event.getKeyChar(),
                    event.getKeyLocation()
                ));
            }

            @Override
            public void menuKeyTyped(final MenuKeyEvent event) {
                this.helper(event);
            }

            @Override
            public void menuKeyPressed(final MenuKeyEvent event) {
                this.helper(event);
            }

            @Override
            public void menuKeyReleased(final MenuKeyEvent event) {
                this.helper(event);
            }
        });
    }
}
