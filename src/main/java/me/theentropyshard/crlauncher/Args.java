/*
 * CRLauncher - https://github.com/CRLauncher/CRLauncher
 * Copyright (C) 2024-2026 CRLauncher
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
import com.beust.jcommander.Parameter;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Args {
    @Parameter(names = {"--workDir"})
    private String workDirPath;

    @Parameter(names = {"--useXdgDataHome"})
    private boolean useXdgDataHome;

    @Parameter(names = {"--useParentDir"})
    private boolean useParentDir;

    @Parameter(names = {"--useJarLocation"})
    private boolean useJarLocation;

    @Parameter(names = {"--crLoaderPath"})
    private String customCRLoaderPath;

    private final List<String> unknownOptions;

    private Args() {
        this.unknownOptions = new ArrayList<>();
    }

    public static Args parse(String[] rawArgs) {
        Args args = new Args();

        JCommander commander = JCommander.newBuilder()
                .acceptUnknownOptions(true)
                .programName("CRLauncher")
                .addObject(args)
                .build();

        commander.parse(rawArgs);

        args.getUnknownOptions().addAll(commander.getUnknownOptions());

        return args;
    }

    public boolean hasUnknownOptions() {
        return this.unknownOptions.size() > 0;
    }

    public List<String> getUnknownOptions() {
        return this.unknownOptions;
    }

    public Path getWorkDir() {
        Path workDir;

        if (this.useJarLocation) {
            workDir = Paths.get(URI.create(Args.class.getProtectionDomain().getCodeSource().getLocation().toString()))
                    .getParent();
        } else if (this.useXdgDataHome) {
            workDir = Paths.get(System.getProperty("XDG_DATA_HOME", ".local/share/"));
        } else {
            workDir = this.workDirPath == null || this.workDirPath.isEmpty() ?
                    Paths.get(System.getProperty("user.dir")) :
                    Paths.get(this.workDirPath);
        }

        if (this.useParentDir){
            workDir = Paths.get(workDir.toString(), BuildConfig.APP_NAME);
        }

        return workDir.normalize().toAbsolutePath();
    }

    public String getCustomCRLoaderPath() {
        return this.customCRLoaderPath;
    }
}
