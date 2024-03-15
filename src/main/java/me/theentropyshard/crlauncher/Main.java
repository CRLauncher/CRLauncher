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

import com.beust.jcommander.JCommander;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.HashUtils;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Main {
    public static void main(String[] args) {
        Args theArgs = Main.parseArgs(args);
        Path workDir = Main.resolveWorkDir(theArgs);

        System.setProperty("crlauncher.logsDir", workDir.resolve("logs").toString());

        try {
            new CRLauncher(theArgs, workDir);
        } catch (Throwable t) {
            LogManager.getLogger(Main.class).error("Unable to start the launcher", t);
            System.exit(1);
        }

        /*try {
            Path hashes = Paths.get("C:\\Users\\Yura\\Desktop\\test\\hashes.txt");

            if (Files.exists(hashes)) {
                FileUtils.delete(hashes);
            }

            FileUtils.createFileIfNotExists(hashes);

            for (Path path : FileUtils.list(Paths.get("C:\\Users\\Yura\\Desktop\\test"))) {
                String hash = HashUtils.murmur3(path);
                Files.writeString(hashes,
                        path.getFileName() + " " +
                        hash + " " + Files.size(path) + System.lineSeparator(),
                        StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }

    private static Args parseArgs(String[] rawArgs) {
        Args args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(rawArgs);

        return args;
    }

    private static Path resolveWorkDir(Args args) {
        String workDirPath = args.getWorkDirPath();

        return (workDirPath == null || workDirPath.isEmpty() ?
                Paths.get(System.getProperty("user.dir")) :
                Paths.get(workDirPath)
        ).normalize().toAbsolutePath();
    }
}
