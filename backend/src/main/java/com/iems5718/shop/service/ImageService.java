package com.iems5718.shop.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {
    
    private static final String UPLOAD_DIR = "images/products/";
    private static final int THUMBNAIL_WIDTH = 300;
    private static final int THUMBNAIL_HEIGHT = 300;
    private static final int LARGE_IMAGE_WIDTH = 800;
    private static final int LARGE_IMAGE_HEIGHT = 800;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    public ImageService() {
        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create upload directory: " + e.getMessage());
        }
    }
    
    public String[] uploadProductImage(MultipartFile file, Long productId) throws IOException {
        // Validate file
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
        
        // Generate unique filename based on product ID
        String extension = getFileExtension(file.getOriginalFilename());
        String baseFilename = "product_" + productId + "_" + UUID.randomUUID().toString();
        
        String largeFilename = baseFilename + "." + extension;
        String thumbnailFilename = baseFilename + "_thumb." + extension;
        
        Path largePath = Paths.get(UPLOAD_DIR + largeFilename);
        Path thumbnailPath = Paths.get(UPLOAD_DIR + thumbnailFilename);
        
        // Save original image (resized if too large)
        Thumbnails.of(file.getInputStream())
                .size(LARGE_IMAGE_WIDTH, LARGE_IMAGE_HEIGHT)
                .keepAspectRatio(true)
                .toFile(largePath.toFile());
        
        // Generate thumbnail
        Thumbnails.of(file.getInputStream())
                .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                .keepAspectRatio(true)
                .toFile(thumbnailPath.toFile());
        
        // Return relative paths
        return new String[] {
            UPLOAD_DIR + largeFilename,
            UPLOAD_DIR + thumbnailFilename
        };
    }
    
    public void deleteProductImages(String imageUrl, String thumbnailUrl) {
        try {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Files.deleteIfExists(Paths.get(imageUrl));
            }
            if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                Files.deleteIfExists(Paths.get(thumbnailUrl));
            }
        } catch (IOException e) {
            System.err.println("Failed to delete images: " + e.getMessage());
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
