package com.alexandre.Barbearia_Api.infra.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    static {
        ImageIO.scanForPlugins();
    }

    private final Path baseDir = Paths.get("uploads");

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    @Value("${app.upload.max-mb:5}")
    private long maxUploadMb;

    public String storeServicoImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        if (file.getSize() > maxUploadBytes()) {
            throw new IllegalArgumentException("Arquivo excede o tamanho máximo permitido.");
        }
        BufferedImage image;
        try (InputStream input = file.getInputStream()) {
            image = ImageIO.read(input);
        }
        if (image == null) {
            throw new IllegalArgumentException("Arquivo de imagem inválido.");
        }

        String filename = UUID.randomUUID() + ".webp";
        Path targetDir = baseDir.resolve("servicos");
        Files.createDirectories(targetDir);
        Path targetFile = targetDir.resolve(filename);

        try (OutputStream output = Files.newOutputStream(targetFile)) {
            if (!ImageIO.write(image, "webp", output)) return storeOriginal(file);
        }
        return "/uploads/servicos/" + filename;
    }

    private String storeOriginal(MultipartFile file) throws IOException {
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null) {
            int idx = original.lastIndexOf('.');
            if (idx >= 0) ext = original.substring(idx);
        }
        ext = ext.toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            ext = ".img";
        }
        String filename = UUID.randomUUID() + ext;
        Path targetDir = baseDir.resolve("servicos");
        Files.createDirectories(targetDir);
        Path targetFile = targetDir.resolve(filename);
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, targetFile);
        }
        return "/uploads/servicos/" + filename;
    }

    private long maxUploadBytes() {
        long safeMb = Math.max(1, maxUploadMb);
        return safeMb * 1024 * 1024;
    }
}
