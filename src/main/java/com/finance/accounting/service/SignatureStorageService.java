package com.finance.accounting.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SignatureStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("png", "jpg", "jpeg", "gif", "webp", "pdf");

    private final Path uploadRoot;
    private final long maxBytes;

    public SignatureStorageService(
            @Value("${app.signatures.upload-dir}") String uploadDir,
            @Value("${app.signatures.max-bytes:5242880}") long maxBytes) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
    }

    /**
     * Stores an uploaded signature file under {@code {upload-dir}/{tenantId}/} and returns a
     * relative path (tenantId/fileName) suitable for {@code User.signatureFilePath}.
     */
    public Optional<String> storeSignature(long tenantId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Optional.empty();
        }
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException(
                    "Signature file is too large (max " + (maxBytes / 1024 / 1024) + " MB).");
        }
        String ext = extensionOf(file.getOriginalFilename());
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException(
                    "Signature must be an image (PNG, JPEG, GIF, WebP) or PDF.");
        }
        String safeName = UUID.randomUUID() + "." + ext;
        Path tenantDir = uploadRoot.resolve(Long.toString(tenantId)).normalize();
        if (!tenantDir.startsWith(uploadRoot)) {
            throw new IllegalStateException("Invalid upload path.");
        }
        Files.createDirectories(tenantDir);
        Path dest = tenantDir.resolve(safeName).normalize();
        if (!dest.startsWith(tenantDir)) {
            throw new IllegalStateException("Invalid file name.");
        }
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        return Optional.of(tenantId + "/" + safeName);
    }

    private static String extensionOf(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return null;
        }
        String name = originalFilename.trim();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return null;
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
