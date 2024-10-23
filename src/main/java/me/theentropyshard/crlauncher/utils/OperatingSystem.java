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

package me.theentropyshard.crlauncher.utils;

import com.sun.jna.Platform;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.datatransfer.TransferableFile;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.datatransfer.TransferableImage;
import me.theentropyshard.crlauncher.logging.Log;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public enum OperatingSystem {
    WINDOWS,
    LINUX,
    MACOS,
    UNKNOWN;

    public String getJavaExecutableName() {
        if (this == OperatingSystem.WINDOWS) {
            return "javaw.exe";
        }

        return "java";
    }

    public static OperatingSystem getCurrent() {
        if (OperatingSystem.isWindows()) {
            return OperatingSystem.WINDOWS;
        } else if (OperatingSystem.isLinux()) {
            return OperatingSystem.LINUX;
        } else if (OperatingSystem.isMacOS()) {
            return OperatingSystem.MACOS;
        } else {
            return OperatingSystem.UNKNOWN;
        }
    }

    public static void open(Path path) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();

            if (desktop.isSupported(Desktop.Action.OPEN)) {
                try {
                    desktop.open(path.toFile());
                } catch (IllegalArgumentException e) {
                    Log.warn("File '" + path + "' does not exist");
                } catch (IOException e) {
                    Log.warn("Unable to open '" + path + "' using java.awt.Desktop: " + e.getMessage());
                }
            } else {
                Log.warn("Unable to open '" + path + "' using java.awt.Desktop: action 'OPEN' not supported");
            }
        } else {
            Log.warn("java.awt.Desktop not supported. OS: " + OperatingSystem.getCurrent());
        }
    }

    public static void browse(String uri) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();

            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(URI.create(uri));
                } catch (IllegalArgumentException e) {
                    Log.warn("URI cannot be converted to URL: " + uri);
                } catch (IOException e) {
                    Log.warn("Unable to browse '" + uri + "' using java.awt.Desktop: " + e.getMessage());
                }
            } else {
                Log.warn("Unable to browse '" + uri + "' using java.awt.Desktop: action 'BROWSE' not supported");
            }
        } else {
            Log.warn("java.awt.Desktop not supported. OS: " + OperatingSystem.getCurrent());
        }
    }

    public static boolean isMacApp() {
        return OperatingSystem.isMacOS() &&
            Files.isDirectory(CRLauncher.getInstance().getWorkDir().getParent().resolve("MacOS"));
    }

    public static boolean isWindows() {
        return Platform.isWindows();
    }

    public static boolean isLinux() {
        return Platform.isLinux();
    }

    public static boolean isMacOS() {
        return Platform.isMac();
    }

    public static boolean isArm() {
        return Platform.isARM();
    }

    public static String getName() {
        return System.getProperty("os.name");
    }

    public static String getVersion() {
        return System.getProperty("os.version");
    }

    public static String getArch() {
        if (OperatingSystem.is64Bit()) {
            return "x64";
        }

        return "x86";
    }

    public static String getBits() {
        if (OperatingSystem.is64Bit()) {
            return "64";
        }

        return "32";
    }

    public static void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }

    public static void copyToClipboard(Path file) {
        TransferableFile transferableFile = new TransferableFile(file.toFile());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferableFile, null);
    }

    public static void copyToClipboard(BufferedImage image) {
        TransferableImage transferableImage = new TransferableImage(image);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferableImage, null);
    }

    public static boolean is64Bit() {
        return Platform.is64Bit();
    }
}
