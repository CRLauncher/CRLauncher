package me.theentropyshard.crlauncher.cosmic.mods.vanilla;

import me.theentropyshard.crlauncher.cosmic.mods.Mod;

public class DataMod implements Mod {
    private String folderName;
    private boolean active;

    public DataMod(String folderName, boolean active) {
        this.folderName = folderName;
        this.active = active;
    }

    @Override
    public String getName() {
        return this.folderName;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getFileName() {
        return this.folderName;
    }

    @Override
    public void setFileName(String fileName) {
        this.folderName = fileName;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }
}
