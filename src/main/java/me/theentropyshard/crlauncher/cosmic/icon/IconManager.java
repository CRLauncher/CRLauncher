package me.theentropyshard.crlauncher.cosmic.icon;

import me.theentropyshard.crlauncher.utils.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class IconManager {
    private static final Logger LOG = LogManager.getLogger(IconManager.class);

    private final Path workDir;
    private final List<Icon> icons;

    public IconManager(Path workDir) {
        this.workDir = workDir;
        this.icons = new ArrayList<>();
    }

    public void saveBuiltinIcons() throws IOException {
        String iconsDir = "/assets/images/icons";

        try {
            URI uri = Objects.requireNonNull(IconManager.class.getResource(iconsDir)).toURI();
            Path iconsDirPath;
            if (uri.getScheme().equals("jar")) {
                try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                    iconsDirPath = fileSystem.getPath(iconsDir);
                }
            } else {
                iconsDirPath = Paths.get(uri);
            }

            for (Path iconPath : FileUtils.list(iconsDirPath)) {
                this.saveIcon(iconPath);
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    public void loadIcons() throws IOException {
        if (!this.icons.isEmpty()) {
            LOG.warn("Tried to load icons, but they are already loaded");

            return;
        }

        for (Path iconPath : FileUtils.list(this.workDir)) {
            try {
                this.loadIcon(iconPath);
            } catch (IOException e) {
                LOG.warn("Could not load icon from {}", iconPath, e);
            }
        }
    }

    public void loadIcon(Path path) throws IOException {
        this.icons.add(new ImageIcon(ImageIO.read(Files.newInputStream(path))));
    }

    public void saveIcon(Path iconPath) throws IOException {
        Path copiedIcon = this.workDir.resolve(iconPath.getFileName());
        if (Files.exists(copiedIcon)) {
            return;
        }
        Files.copy(iconPath, copiedIcon);
        this.loadIcon(copiedIcon);
    }

    public void deleteIcon(String fileName) throws IOException {
        FileUtils.delete(this.workDir.resolve(fileName));
    }
}
