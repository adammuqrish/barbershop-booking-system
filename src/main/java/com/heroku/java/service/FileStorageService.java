package com.heroku.java.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Stores user uploads in an EXTERNAL directory (outside the jar/source tree)
 * and serves them via the resource handler mapped to /resources/uploads/**.
 * Filenames are sanitized and validated before anything touches disk.
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private final Path uploadDir;

    public Path getUploadDir() {
        return uploadDir;
    }

    public FileStorageService(@Value("${app.upload.dir:${user.dir}/uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory: " + this.uploadDir, e);
        }
    }

    /**
     * Validates and stores an uploaded image. Returns the generated filename
     * to persist in the database.
     *
     * @throws IllegalArgumentException if the file fails validation
     * @throws IOException              if writing to disk fails
     */
    public String storeImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds the 5 MB size limit.");
        }

        String original = file.getOriginalFilename();
        String extension = getExtension(original);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Only JPG, PNG, GIF and WEBP images are allowed.");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Uploaded file is not an image.");
        }

        // Generated name only - the client-supplied name never reaches the filesystem
        String fileName = UUID.randomUUID() + "." + extension;

        Path target = uploadDir.resolve(fileName).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new IOException("Resolved path escapes the upload directory.");
        }
        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return fileName;
    }

    private String getExtension(String original) {
        if (original == null)
            return "";
        int dot = original.lastIndexOf('.');
        if (dot < 0 || dot == original.length() - 1)
            return "";
        return original.substring(dot + 1).toLowerCase();
    }
}
