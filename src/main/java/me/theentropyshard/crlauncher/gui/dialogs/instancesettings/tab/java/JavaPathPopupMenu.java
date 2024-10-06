package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.java;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.java.JavaLocator;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class JavaPathPopupMenu extends JPopupMenu {
    public JavaPathPopupMenu(final Consumer<? super String> consumer) {
        Language language = CRLauncher.getInstance().getLanguage();

        JMenuItem currentJavaPathMenuItem = new JMenuItem();
        String currentJavaPath = JavaLocator.getJavaPath();
        currentJavaPathMenuItem.setText(
            language.getString("gui.instanceSettingsDialog.javaTab.javaInstallation.current")
                .replace("$$CURRENT_INSTALLATION$$", currentJavaPath)
        );
        currentJavaPathMenuItem.addActionListener(event -> consumer.accept(currentJavaPath));
        this.add(currentJavaPathMenuItem);

        this.addSeparator();

        for (Path path : JavaLocator.getJavaFromEnv()) {
            JMenuItem menuItem = new JMenuItem();
            menuItem.setText(path.toString());
            menuItem.addActionListener(event -> {
                // Avoid using variables from for-loop (lambda closures)
                consumer.accept(((JMenuItem) event.getSource()).getText());
            });
            this.add(menuItem);
        }

        this.addSeparator();

        JMenuItem selectJavaPathMenuItem = new JMenuItem();
        selectJavaPathMenuItem.setText(language.getString("gui.instanceSettingsDialog.javaTab.javaInstallation.browse"));
        selectJavaPathMenuItem.addActionListener(event -> new Worker<String, Void>("browse java installation path") {
            @Override
            protected String work() throws Exception {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(JavaExecFileFilter.current());
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(false);

                if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(selectJavaPathMenuItem)) {
                    return fileChooser.getSelectedFile().toString();
                } else {
                    return null;
                }
            }

            @Override
            protected void done() {
                String result;
                try {
                    result = this.get();
                } catch (InterruptedException | ExecutionException ex) {
                    Log.error(ex);
                    return;
                }

                if (result != null) {
                    consumer.accept(result);
                }
            }
        }.execute());
        this.add(selectJavaPathMenuItem);
    }

    public void showBelow(final Component component) {
        this.show(component, 0, component.getHeight());
    }
}
