package com.example.ecommerce.media;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final Path uploadDirectory;

    public UploadController(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDirectory = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @PostMapping
    public UploadResponse upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Upload file is empty");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only JPG, PNG, WEBP, and GIF images are allowed");
        }

        Files.createDirectories(uploadDirectory);
        String extension = extensionFor(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;
        file.transferTo(uploadDirectory.resolve(fileName));
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(fileName)
                .toUriString();
        return new UploadResponse(url);
    }

    private String extensionFor(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".jpg";
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase(Locale.ROOT);
        return extension.matches("\\.(jpg|jpeg|png|webp|gif)") ? extension : ".jpg";
    }
}
