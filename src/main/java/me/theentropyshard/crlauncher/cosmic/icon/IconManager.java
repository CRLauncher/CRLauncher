package me.theentropyshard.crlauncher.cosmic.icon;

import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ListUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IconManager {


    private final Path workDir;
    private final List<CosmicIcon> icons;

    public IconManager(Path workDir) {
        this.workDir = workDir;
        this.icons = new ArrayList<>();
    }

    public void saveBuiltinIcons() throws IOException {
        String cosmicLogo = "cosmic_logo_x32.png";
        if (Files.exists(this.workDir.resolve(cosmicLogo))) {
            return;
        }

        Path tempDir = this.workDir.resolve("temp_" + System.currentTimeMillis());

        if (Files.exists(tempDir)) {
            FileUtils.delete(tempDir);
        }

        Path tempFile = tempDir.resolve(cosmicLogo);
        FileUtils.createDirectoryIfNotExists(tempFile.getParent());

        try (InputStream resource = IconManager.class.getResourceAsStream("/assets/images/icons/" + cosmicLogo)) {
            Files.write(tempFile, Objects.requireNonNull(resource).readAllBytes());
        }

        this.saveIcon(tempFile);

        FileUtils.delete(tempDir);
    }

    public void loadIcons() throws IOException {
        if (!this.icons.isEmpty()) {
            Log.warn("Tried to load icons, but they are already loaded");

            return;
        }

        for (Path iconPath : FileUtils.list(this.workDir)) {
            if (!Files.isRegularFile(iconPath)) {
                continue;
            }

            try {
                this.loadIcon(iconPath);
            } catch (IOException e) {
                Log.warn("Could not load icon from " + iconPath + ": " + e.getMessage());
            }
        }
    }

    public List<CosmicIcon> getIcons() {
        return this.icons;
    }

    public CosmicIcon getIcon(String fileName) {
        return ListUtils.search(this.icons, ico -> ico.fileName().equals(fileName));
    }

    public CosmicIcon loadIcon(Path path) throws IOException {
        BufferedImage bufferedImage;
        try (InputStream input = Files.newInputStream(path)) {
            bufferedImage = ImageIO.read(input);
        }

        if (bufferedImage == null) {
            throw new IOException("Could not read image: " + path);
        }

        if (bufferedImage.getWidth() != 32 || bufferedImage.getHeight() != 32) {
            BufferedImage scaledImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledImage.createGraphics();
            g2d.drawImage(bufferedImage.getScaledInstance(32, 32, BufferedImage.SCALE_FAST), 0, 0, null);
            g2d.dispose();

            try (OutputStream output = Files.newOutputStream(path)) {
                ImageIO.write(scaledImage, "PNG", output);
            }

            CosmicIcon cosmicIcon = new CosmicIcon(path.getFileName().toString(), new ImageIcon(scaledImage));
            this.icons.add(cosmicIcon);
            return cosmicIcon;
        } else {
            CosmicIcon cosmicIcon = new CosmicIcon(path.getFileName().toString(), new ImageIcon(bufferedImage));
            this.icons.add(cosmicIcon);
            return cosmicIcon;
        }
    }

    public CosmicIcon saveIcon(Path iconPath) throws IOException {
        Path copiedIcon = this.workDir.resolve(iconPath.getFileName());
        if (Files.exists(copiedIcon)) {
            return ListUtils.search(this.icons, ico -> ico.fileName().equals(copiedIcon.getFileName().toString()));
        }
        Files.copy(iconPath, copiedIcon);
        return this.loadIcon(copiedIcon);
    }

    public void deleteIcon(String fileName) throws IOException {
        FileUtils.delete(this.workDir.resolve(fileName));
    }
}
