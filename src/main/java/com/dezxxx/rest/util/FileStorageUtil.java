package com.dezxxx.rest.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class FileStorageUtil {

    private FileStorageUtil() {
    }

    public static String save(InputStream content, String originalName, String storagePath) throws IOException {
        String fileName = UUID.randomUUID() + "_" + sanitize(originalName);
        Path destination = Paths.get(storagePath, fileName);
        Files.createDirectories(destination.getParent());
        Files.copy(content, destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toString();
    }

    public static InputStream read(String filePath) throws IOException {
        return Files.newInputStream(Paths.get(filePath));
    }

    public static void delete(String filePath) throws IOException {
        Files.deleteIfExists(Paths.get(filePath));
    }

    private static String sanitize(String name) {
        if (name == null || name.isBlank()) return "unnamed";
        return name.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }
}
