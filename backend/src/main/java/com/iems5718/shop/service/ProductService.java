package com.iems5718.shop.service;

import com.iems5718.shop.model.Category;
import com.iems5718.shop.model.Product;
import com.iems5718.shop.repository.CategoryRepository;
import com.iems5718.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ImageService imageService;
    
    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrueOrderByWeightDescPidAsc();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByCategory(Long catid) {
        return productRepository.findByCategoryCatidAndActiveTrueOrderByWeightDescPidAsc(catid);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrueOrderByWeightDescPidAsc(keyword);
    }

    public Product updateWeight(Long id, Integer weight) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setWeight(weight);
        return productRepository.save(product);
    }
    
    public Product createProduct(Product product) {
        // Ensure category is loaded
        if (product.getCategory() != null && product.getCategory().getCatid() != null) {
            Category category = categoryRepository.findById(product.getCategory().getCatid())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }
        syncProductMedia(product);
        return productRepository.save(product);
    }
    
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStockQuantity(productDetails.getStockQuantity());
        if (productDetails.getWeight() != null) {
            product.setWeight(productDetails.getWeight());
        }
        if (productDetails.getImageUrl() != null) {
            product.setImageUrl(productDetails.getImageUrl());
        }
        if (productDetails.getThumbnailUrls() != null) {
            product.setThumbnailUrls(productDetails.getThumbnailUrls());
        }
        if (productDetails.getGalleryImageUrls() != null) {
            product.setGalleryImageUrls(productDetails.getGalleryImageUrls());
        }
        if (productDetails.getVideoUrl() != null) {
            product.setVideoUrl(productDetails.getVideoUrl());
        }
        if (productDetails.getActive() != null) {
            product.setActive(productDetails.getActive());
        }
        
        // Update category if provided
        if (productDetails.getCategory() != null && productDetails.getCategory().getCatid() != null) {
            Category category = categoryRepository.findById(productDetails.getCategory().getCatid())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        syncProductMedia(product);
        
        return productRepository.save(product);
    }
    
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }
    
    public Product createProductWithImage(Long catid, String name, Double price, String description,
                                         Integer stockQuantity, Integer weight, MultipartFile[] images,
                                         MultipartFile video) throws Exception {
        Category category = categoryRepository.findById(catid)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = new Product();
        product.setCategory(category);
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        product.setStockQuantity(stockQuantity);
        product.setWeight(weight != null ? weight : 0);
        product.setActive(true);
        
        // Save product first to get ID
        product = productRepository.save(product);
        
        boolean mediaUpdated = applyUploadedMedia(product, images, video);
        if (mediaUpdated) {
            product = productRepository.save(product);
        }
        
        return product;
    }
    
    public Product updateProductWithImage(Long id, Long catid, String name, Double price,
                                         String description, Integer stockQuantity, Integer weight,
                                         MultipartFile[] images, MultipartFile video) throws Exception {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (catid != null) {
            Category category = categoryRepository.findById(catid)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }
        if (name != null) product.setName(name);
        if (price != null) product.setPrice(price);
        if (description != null) product.setDescription(description);
        if (stockQuantity != null) product.setStockQuantity(stockQuantity);
        if (weight != null) product.setWeight(weight);
        
        applyUploadedMedia(product, images, video);
        syncProductMedia(product);
        
        return productRepository.save(product);
    }

    private boolean applyUploadedMedia(Product product, MultipartFile[] images, MultipartFile video) throws Exception {
        boolean updated = false;

        if (hasFiles(images)) {
            imageService.deleteProductImages(product.getImageUrl(), product.getThumbnailUrls(), product.getGalleryImageUrls());

            List<ImageService.UploadedImage> uploadedImages = imageService.uploadProductImages(images, product.getPid());
            if (!uploadedImages.isEmpty()) {
                product.setImageUrl(uploadedImages.get(0).imageUrl());
                product.setGalleryImageUrls(uploadedImages.stream()
                        .map(ImageService.UploadedImage::imageUrl)
                        .collect(Collectors.joining(",")));
                product.setThumbnailUrls(uploadedImages.stream()
                        .map(ImageService.UploadedImage::thumbnailUrl)
                        .collect(Collectors.joining(",")));
                updated = true;
            }
        }

        if (video != null && !video.isEmpty()) {
            imageService.deleteProductVideo(product.getVideoUrl());
            product.setVideoUrl(imageService.uploadProductVideo(video, product.getPid()));
            updated = true;
        }

        return updated;
    }

    private boolean hasFiles(MultipartFile[] files) {
        if (files == null) {
            return false;
        }
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void syncProductMedia(Product product) {
        List<String> galleryImages = splitCsv(product.getGalleryImageUrls());
        List<String> thumbnails = splitCsv(product.getThumbnailUrls());

        if ((product.getImageUrl() == null || product.getImageUrl().isBlank())) {
            if (!galleryImages.isEmpty()) {
                product.setImageUrl(galleryImages.get(0));
            } else if (!thumbnails.isEmpty()) {
                product.setImageUrl(thumbnails.get(0));
            }
        }

        if ((product.getGalleryImageUrls() == null || product.getGalleryImageUrls().isBlank())
                && product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
            product.setGalleryImageUrls(product.getImageUrl());
        }

        if ((product.getThumbnailUrls() == null || product.getThumbnailUrls().isBlank())
                && product.getGalleryImageUrls() != null && !product.getGalleryImageUrls().isBlank()) {
            product.setThumbnailUrls(product.getGalleryImageUrls());
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
}
