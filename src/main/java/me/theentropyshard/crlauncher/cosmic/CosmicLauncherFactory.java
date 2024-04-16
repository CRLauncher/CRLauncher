package me.theentropyshard.crlauncher.cosmic;

import java.nio.file.Path;

public class CosmicLauncherFactory {
    public static CosmicLauncher getLauncher(LaunchType type, Path runDir, Path gameFilesLocation, Path clientPath) {
        if (type == LaunchType.VANILLA) {
            return new VanillaCosmicLauncher(runDir, gameFilesLocation, clientPath);
        }

        throw new IllegalArgumentException("Unknown launch type: " + type);
    }
}
