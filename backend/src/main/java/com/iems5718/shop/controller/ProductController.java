package com.iems5718.shop.controller;

import com.iems5718.shop.model.Product;
import com.iems5718.shop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/category/{catid}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long catid) {
        return ResponseEntity.ok(productService.getProductsByCategory(catid));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String q) {
        return ResponseEntity.ok(productService.searchProducts(q));
    }
    
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(product));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            return ResponseEntity.ok(productService.updateProduct(id, product));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{id}/weight")
    public ResponseEntity<?> updateWeight(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        try {
            Integer weight = body.get("weight");
            if (weight == null) return ResponseEntity.badRequest().body("weight is required");
            return ResponseEntity.ok(productService.updateWeight(id, weight));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> createProductWithImage(
            @RequestParam("catid") Long catid,
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam("description") String description,
            @RequestParam(value = "stockQuantity", defaultValue = "0") Integer stockQuantity,
            @RequestParam(value = "weight", defaultValue = "0") Integer weight,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "video", required = false) MultipartFile video) {
        try {
            Product product = productService.createProductWithImage(
                    catid, name, price, description, stockQuantity, weight, mergeImageFiles(images, image), video);
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/upload")
    public ResponseEntity<?> updateProductWithImage(
            @PathVariable Long id,
            @RequestParam(value = "catid", required = false) Long catid,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "price", required = false) Double price,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "stockQuantity", required = false) Integer stockQuantity,
            @RequestParam(value = "weight", required = false) Integer weight,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "video", required = false) MultipartFile video,
            @RequestParam(value = "replaceImages", defaultValue = "false") Boolean replaceImages,
            @RequestParam(value = "retainedGalleryImageUrls", required = false) String retainedGalleryImageUrls,
            @RequestParam(value = "retainedThumbnailUrls", required = false) String retainedThumbnailUrls,
            @RequestParam(value = "clearVideo", defaultValue = "false") Boolean clearVideo) {
        try {
            Product product = productService.updateProductWithImage(
                    id, catid, name, price, description, stockQuantity, weight,
                    mergeImageFiles(images, image), video, replaceImages, retainedGalleryImageUrls,
                    retainedThumbnailUrls, clearVideo);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private MultipartFile[] mergeImageFiles(MultipartFile[] images, MultipartFile singleImage) {
        List<MultipartFile> mergedFiles = new ArrayList<>();

        if (images != null) {
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    mergedFiles.add(image);
                }
            }
        }

        if (singleImage != null && !singleImage.isEmpty()) {
            mergedFiles.add(singleImage);
        }

        return mergedFiles.toArray(new MultipartFile[0]);
    }
}
