package com.iems5718.shop.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${app.image.storage-dir:../images/products/}")
    private String storageDir;

    @Value("${app.image.url-prefix:images/products/}")
    private String urlPrefix;

    @Value("${app.video.storage-dir:../videos/products/}")
    private String videoStorageDir;

    @Value("${app.video.url-prefix:videos/products/}")
    private String videoUrlPrefix;

    private static final int THUMBNAIL_WIDTH = 300;
    private static final int THUMBNAIL_HEIGHT = 300;
    private static final int LARGE_IMAGE_WIDTH = 800;
    private static final int LARGE_IMAGE_HEIGHT = 800;
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024;

    public record UploadedImage(String imageUrl, String thumbnailUrl) {}

    private void ensureStorageDir(String directory) {
        try {
            Files.createDirectories(Paths.get(directory));
        } catch (IOException e) {
            System.err.println("Failed to create upload directory: " + e.getMessage());
        }
    }
    
    public UploadedImage uploadProductImage(MultipartFile file, Long productId) throws IOException {
        ensureStorageDir(storageDir);

        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IOException("File size exceeds maximum limit of 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("File must be an image");
        }

        String extension = getFileExtension(file.getOriginalFilename(), "jpg");
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

        return new UploadedImage(urlPrefix + largeFilename, urlPrefix + thumbnailFilename);
    }

    public List<UploadedImage> uploadProductImages(MultipartFile[] files, Long productId) throws IOException {
        List<UploadedImage> uploadedImages = new ArrayList<>();
        if (files == null) {
            return uploadedImages;
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            uploadedImages.add(uploadProductImage(file, productId));
        }

        return uploadedImages;
    }

    public String uploadProductVideo(MultipartFile file, Long productId) throws IOException {
        ensureStorageDir(videoStorageDir);

        if (file == null || file.isEmpty()) {
            throw new IOException("Video file is empty");
        }

        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new IOException("Video size exceeds maximum limit of 100MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IOException("File must be a video");
        }

        String extension = getFileExtension(file.getOriginalFilename(), "mp4");
        String filename = "product_" + productId + "_" + UUID.randomUUID() + "." + extension;
        Path videoPath = Paths.get(videoStorageDir + filename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, videoPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return videoUrlPrefix + filename;
    }

    public void deleteProductImages(String imageUrl, String thumbnailUrls, String galleryImageUrls) {
        Set<String> filesToDelete = new LinkedHashSet<>();
        filesToDelete.addAll(splitCsv(galleryImageUrls));
        filesToDelete.addAll(splitCsv(thumbnailUrls));
        if (imageUrl != null && !imageUrl.isBlank()) {
            filesToDelete.add(imageUrl.trim());
        }

        for (String url : filesToDelete) {
            deleteManagedFile(url, urlPrefix, storageDir);
        }
    }

    public void deleteProductImageFiles(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        Set<String> filesToDelete = new LinkedHashSet<>(imageUrls);
        for (String url : filesToDelete) {
            deleteManagedFile(url, urlPrefix, storageDir);
        }
    }

    public void deleteProductVideo(String videoUrl) {
        deleteManagedFile(videoUrl, videoUrlPrefix, videoStorageDir);
    }

    private void deleteManagedFile(String url, String managedPrefix, String managedStorageDir) {
        if (url == null || url.isBlank() || !url.startsWith(managedPrefix)) {
            return;
        }
        try {
            String filename = url.substring(managedPrefix.length());
            Files.deleteIfExists(Paths.get(managedStorageDir + filename));
        } catch (IOException e) {
            System.err.println("Failed to delete media: " + e.getMessage());
        }
    }
    
    private List<String> splitCsv(String csv) {
        List<String> values = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return values;
        }

        for (String value : csv.split(",")) {
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                values.add(trimmed);
            }
        }

        return values;
    }

    private String getFileExtension(String filename, String defaultExtension) {
        if (filename == null || filename.isEmpty()) {
            return defaultExtension;
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : defaultExtension;
    }
}
