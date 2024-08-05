package me.theentropyshard.crlauncher.cosmic.launcher;

public interface CosmicLauncher {
    int launch(LogConsumer log, boolean exitAfterLaunch) throws Exception;
}
