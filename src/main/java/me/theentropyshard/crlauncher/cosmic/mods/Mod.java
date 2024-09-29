package me.theentropyshard.crlauncher.cosmic.mods;

public class Mod {
    /**
     * Unique id, mods are identified by it, two mods cannot have the same id
     */
    private String id;

    /**
     * Name of a mod, two mods may have the same name
     */
    private String name;

    /**
     * Description of a mod
     */
    private String description;

    /**
     * Version of a mod
     */
    private String version;

    /**
     * Name of the mod file
     */
    private String fileName;

    /**
     * A loader, that this mod is made for
     */
    private Loader loader;

    public Mod() {

    }

    public Mod(String id, String name, String description, String version, String fileName, Loader loader) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.version = version;
        this.fileName = fileName;
        this.loader = loader;
    }

    public boolean isFabric() {
        return this.loader == Loader.FABRIC;
    }

    public boolean isQuilt() {
        return this.loader == Loader.QUILT;
    }

    public boolean isPuzzle() {
        return this.loader == Loader.PUZZLE;
    }

    /**
     * Enum of mod loaders
     */
    public enum Loader {
        FABRIC,
        QUILT,
        PUZZLE
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Loader getLoader() {
        return this.loader;
    }

    public void setLoader(Loader loader) {
        this.loader = loader;
    }
}
