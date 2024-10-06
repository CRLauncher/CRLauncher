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

package me.theentropyshard.crlauncher.java;

import me.theentropyshard.crlauncher.utils.OperatingSystem;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Predicate;

public final class JavaLocator {
    public static String getJavaPath() {
        return Paths.get(System.getProperty("java.home"), "bin", OperatingSystem.getCurrent().getJavaExecutableName()).toString();
    }

    /**
     * Detect java installation from {@code PATH} environmental variable.
     * <p>
     * This method checks each entry in the {@code PATH} environmental variable
     * if the java windowless executable is present.
     *
     * @return  the detected java installation paths
     * @see OperatingSystem#getJavaExecutableName
     * @see System#getenv
     * @see Files#exists
     */
    public static Path[] getJavaFromEnv() {
        return getJavaInFolders(
            System.getenv("PATH").split(File.pathSeparator),
            OperatingSystem.getCurrent().getJavaExecutableName(),
            Files::exists
        );
    }

    /**
     * Detect java installations from given folders.
     *
     * @param folders  the folders to check
     * @param exec  the file name of the windowless java executable
     * @param verifier  the installation verifier
     * @return  the verified java installation paths
     */
    public static Path[] getJavaInFolders(
        final String[] folders,
        final String exec,
        final Predicate<Path> verifier
    ) {
        Path[] paths = new Path[folders.length];

        int size = 0;
        for (String bin : folders) {
            final Path path = Paths.get(bin, exec);
            if (verifier.test(path)) {
                paths[size++] = path;
            }
        }

        return Arrays.copyOf(paths, size);
    }

    private JavaLocator() {
        throw new UnsupportedOperationException();
    }
}
