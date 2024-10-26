package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.java;

import me.theentropyshard.crlauncher.utils.OperatingSystem;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public final class JavaExecFileFilter extends FileFilter {
    public static final JavaExecFileFilter UNIX = new JavaExecFileFilter("java");

    public static final JavaExecFileFilter WINDOWS = new JavaExecFileFilter("javaw.exe");

    public static JavaExecFileFilter current() {
        return OperatingSystem.isWindows() ? JavaExecFileFilter.WINDOWS : JavaExecFileFilter.UNIX;
    }

    private final String targetName;

    private JavaExecFileFilter(final String targetName) {
        this.targetName = targetName;
    }

    @Override
    public boolean accept(final File file) {
        return !file.isFile() || file.getName().equals(this.targetName);
    }

    @Override
    public String getDescription() {
        return "Java Wrapper (" + this.targetName + ")";
    }
}
