package com.school.app.document;

import com.school.app.exception.FileStorageException;
import com.school.app.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    @Value("${app.file.allowed-types}")
    private String allowedTypesCsv;

    @Value("${app.file.max-size-bytes}")
    private long maxSizeBytes;

    private Path root;

    private Path root() {
        if (root == null) {
            root = Paths.get(uploadDir).toAbsolutePath().normalize();
            try {
                Files.createDirectories(root);
            } catch (IOException e) {
                throw new FileStorageException("Could not create upload directory", e);
            }
        }
        return root;
    }

    private List<String> allowedTypes() {
        return Arrays.asList(allowedTypesCsv.split(","));
    }

    /** Validates, then stores the file under a random name (never the user-supplied name), returning the stored filename. */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("No file provided");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new InvalidRequestException("File exceeds maximum allowed size");
        }
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes().contains(contentType)) {
            throw new InvalidRequestException("Unsupported file type: " + contentType);
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        if (originalName.contains("..")) {
            throw new InvalidRequestException("Invalid filename");
        }
        String extension = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            extension = originalName.substring(dot); // keep original extension only, e.g. ".pdf"
        }
        // Never trust/execute the file: store under a random UUID name, extension only used for content-type hints.
        String storedFilename = UUID.randomUUID() + sanitizeExtension(extension);

        try {
            Path target = root().resolve(storedFilename).normalize();
            if (!target.getParent().equals(root())) {
                throw new InvalidRequestException("Invalid storage path");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file", e);
        }
        return storedFilename;
    }

    private String sanitizeExtension(String extension) {
        // Only allow a small known-safe set of extensions regardless of what was uploaded.
        List<String> allowedExt = List.of(".pdf", ".jpg", ".jpeg", ".png");
        String lower = extension.toLowerCase();
        return allowedExt.contains(lower) ? lower : "";
    }

    public InputStream loadAsStream(String storedFilename) {
        try {
            Path file = root().resolve(storedFilename).normalize();
            if (!file.getParent().equals(root()) || !Files.exists(file)) {
                throw new FileStorageException("File not found on disk: " + storedFilename);
            }
            return Files.newInputStream(file);
        } catch (IOException e) {
            throw new FileStorageException("Failed to read file", e);
        }
    }

    public void delete(String storedFilename) {
        try {
            Path file = root().resolve(storedFilename).normalize();
            if (file.getParent().equals(root())) {
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file", e);
        }
    }
}
