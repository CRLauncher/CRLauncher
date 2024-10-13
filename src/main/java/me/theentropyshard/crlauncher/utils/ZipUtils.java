package me.theentropyshard.crlauncher.utils;

import net.lingala.zip4j.model.FileHeader;

import java.util.List;

public final class ZipUtils {
    public static String findTopLevelDirectory(List<FileHeader> fileHeaders) {
        String topLevelDir = null;

        for (FileHeader fileHeader : fileHeaders) {
            String fileName = fileHeader.getFileName();
            if (!fileName.substring(0, fileName.length() - 1).contains("/")) {
                topLevelDir = fileName;

                break;
            }
        }

        return topLevelDir;
    }

    private ZipUtils() {
        throw new UnsupportedOperationException();
    }
}
