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

package me.theentropyshard.crlauncher;

import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.OperatingSystem;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Updater {
    public static void main(String[] args) {
        String oldPath = args[0];
        String newPath = args[1];
        String[] otherArgs = Arrays.copyOfRange(args, 2, args.length);

        Path oldLauncherFile = Paths.get(oldPath);
        Path newLauncherFile = Paths.get(newPath);

        try {
            try {
                FileUtils.delete(oldLauncherFile);
            } catch (IOException e) {

            }

            Files.copy(newLauncherFile, oldLauncherFile, StandardCopyOption.REPLACE_EXISTING);

            new File(oldLauncherFile.toAbsolutePath().toString()).setExecutable(true);

            List<String> arguments = new ArrayList<>();

            if (OperatingSystem.isMacApp()) {
                arguments.add("open");
                arguments.add("-n");
                arguments.add(Paths.get(System.getProperty("user.dir")).getParent().getParent().toAbsolutePath().toString());
                arguments.add("--args");
            } else {
                String path = System.getProperty("java.home") + File.separator + "bin" + File.separator +
                    OperatingSystem.getCurrent().getJavaExecutableName();
                arguments.add(path);
                arguments.add("-Djna.nosys=true");
                arguments.add("-jar");
                arguments.add(oldLauncherFile.toString());
            }

            arguments.addAll(Arrays.asList(otherArgs));

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.directory(new File(System.getProperty("user.dir")));
            processBuilder.command(arguments);

            processBuilder.start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            e.printStackTrace();
        }

        System.exit(0);
    }
}
