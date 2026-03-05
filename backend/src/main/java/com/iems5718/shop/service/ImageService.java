package com.iems5718.shop.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${app.image.storage-dir:../images/products/}")
    private String storageDir;

    @Value("${app.image.url-prefix:images/products/}")
    private String urlPrefix;

    private static final int THUMBNAIL_WIDTH = 300;
    private static final int THUMBNAIL_HEIGHT = 300;
    private static final int LARGE_IMAGE_WIDTH = 800;
    private static final int LARGE_IMAGE_HEIGHT = 800;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private void ensureStorageDir() {
        try {
            Files.createDirectories(Paths.get(storageDir));
        } catch (IOException e) {
            System.err.println("Failed to create upload directory: " + e.getMessage());
        }
    }
    
    public String[] uploadProductImage(MultipartFile file, Long productId) throws IOException {
        ensureStorageDir();

        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum limit of 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("File must be an image");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        String baseFilename = "product_" + productId + "_" + UUID.randomUUID().toString();

        String largeFilename = baseFilename + "." + extension;
        String thumbnailFilename = baseFilename + "_thumb." + extension;

        Path largePath = Paths.get(storageDir + largeFilename);
        Path thumbnailPath = Paths.get(storageDir + thumbnailFilename);

        byte[] imageBytes = file.getBytes();

        Thumbnails.of(new ByteArrayInputStream(imageBytes))
                .size(LARGE_IMAGE_WIDTH, LARGE_IMAGE_HEIGHT)
                .keepAspectRatio(true)
                .toFile(largePath.toFile());

        Thumbnails.of(new ByteArrayInputStream(imageBytes))
                .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                .keepAspectRatio(true)
                .toFile(thumbnailPath.toFile());

        System.out.println("Image saved to: " + largePath.toAbsolutePath());

        return new String[] {
            urlPrefix + largeFilename,
            urlPrefix + thumbnailFilename
        };
    }

    public void deleteProductImages(String imageUrl, String thumbnailUrl) {
        deleteFile(imageUrl);
        deleteFile(thumbnailUrl);
    }

    private void deleteFile(String url) {
        if (url == null || url.isEmpty()) return;
        try {
            // Try resolving relative to storageDir parent
            if (url.startsWith(urlPrefix)) {
                String filename = url.substring(urlPrefix.length());
                Files.deleteIfExists(Paths.get(storageDir + filename));
            } else {
                Files.deleteIfExists(Paths.get(url));
            }
        } catch (IOException e) {
            System.err.println("Failed to delete image: " + e.getMessage());
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "jpg";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "jpg";
    }
}
