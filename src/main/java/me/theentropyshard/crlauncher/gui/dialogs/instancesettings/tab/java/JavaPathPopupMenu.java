package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.java;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.java.JavaLocator;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.function.Consumer;

public class JavaPathPopupMenu extends JPopupMenu {
    public JavaPathPopupMenu(final Consumer<? super String> consumer) {
        final Language language = CRLauncher.getInstance().getLanguage();

        final JMenuItem currentJavaPathMenuItem = new JMenuItem();
        final String currentJavaPath = JavaLocator.getJavaPath();
        currentJavaPathMenuItem.setText(
            language.getString("gui.instanceSettingsDialog.javaTab.javaInstallation.current")
                .replace("$$CURRENT_INSTALLATION$$", currentJavaPath)
        );
        currentJavaPathMenuItem.addActionListener(event -> consumer.accept(currentJavaPath));
        this.add(currentJavaPathMenuItem);

        this.addSeparator();

        for (final Path path : JavaLocator.getJavaFromEnv()) {
            final JMenuItem menuItem = new JMenuItem();
            menuItem.setText(path.toString());
            menuItem.addActionListener(event -> {
                consumer.accept(((JMenuItem) event.getSource()).getText());
            });
            this.add(menuItem);
        }

        this.addSeparator();

        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(JavaExecFileFilter.current());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        final JMenuItem selectJavaPathMenuItem = new JMenuItem();
        selectJavaPathMenuItem.setText(language.getString("gui.instanceSettingsDialog.javaTab.javaInstallation.browse"));
        selectJavaPathMenuItem.addActionListener(event -> {
            if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(selectJavaPathMenuItem)) {
                consumer.accept(fileChooser.getSelectedFile().toString());
            }
        });
        this.add(selectJavaPathMenuItem);
    }

    public void showBelow(final Component component) {
        this.show(component, 0, component.getHeight());
    }
}
