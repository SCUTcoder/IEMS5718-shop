package com.iems5718.shop.service;

import com.iems5718.shop.model.Category;
import com.iems5718.shop.model.Product;
import com.iems5718.shop.repository.CategoryRepository;
import com.iems5718.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private record StoredProductImage(String imageUrl, String thumbnailUrl) {}
    
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
                                         MultipartFile[] images, MultipartFile video, Boolean replaceImages,
                                         String retainedGalleryImageUrls, String retainedThumbnailUrls,
                                         Boolean clearVideo) throws Exception {
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
        
        applyUpdatedMedia(product, images, video, replaceImages, retainedGalleryImageUrls, retainedThumbnailUrls, clearVideo);
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

    private boolean applyUpdatedMedia(Product product, MultipartFile[] images, MultipartFile video,
                                      Boolean replaceImages, String retainedGalleryImageUrls,
                                      String retainedThumbnailUrls, Boolean clearVideo) throws Exception {
        boolean updated = false;

        if (Boolean.TRUE.equals(replaceImages)) {
            List<StoredProductImage> retainedImages = buildImagePairsFromCsv(retainedGalleryImageUrls, retainedThumbnailUrls);
            List<StoredProductImage> currentImages = buildStoredImagePairs(product);
            Set<String> retainedKeys = retainedImages.stream()
                    .map(this::mediaKey)
                    .collect(Collectors.toCollection(HashSet::new));

            List<String> filesToDelete = new ArrayList<>();
            for (StoredProductImage image : currentImages) {
                if (!retainedKeys.contains(mediaKey(image))) {
                    filesToDelete.add(image.imageUrl());
                    filesToDelete.add(image.thumbnailUrl());
                }
            }
            imageService.deleteProductImageFiles(filesToDelete);

            List<StoredProductImage> finalImages = new ArrayList<>(retainedImages);
            if (hasFiles(images)) {
                List<ImageService.UploadedImage> uploadedImages = imageService.uploadProductImages(images, product.getPid());
                finalImages.addAll(uploadedImages.stream()
                        .map(uploaded -> new StoredProductImage(uploaded.imageUrl(), uploaded.thumbnailUrl()))
                        .toList());
            }

            applyImagePairs(product, finalImages);
            updated = true;
        } else if (hasFiles(images)) {
            imageService.deleteProductImages(product.getImageUrl(), product.getThumbnailUrls(), product.getGalleryImageUrls());

            List<ImageService.UploadedImage> uploadedImages = imageService.uploadProductImages(images, product.getPid());
            applyImagePairs(product, uploadedImages.stream()
                    .map(uploaded -> new StoredProductImage(uploaded.imageUrl(), uploaded.thumbnailUrl()))
                    .toList());
            updated = true;
        }

        if (video != null && !video.isEmpty()) {
            imageService.deleteProductVideo(product.getVideoUrl());
            product.setVideoUrl(imageService.uploadProductVideo(video, product.getPid()));
            updated = true;
        } else if (Boolean.TRUE.equals(clearVideo) && product.getVideoUrl() != null && !product.getVideoUrl().isBlank()) {
            imageService.deleteProductVideo(product.getVideoUrl());
            product.setVideoUrl(null);
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

    private void applyImagePairs(Product product, List<StoredProductImage> imagePairs) {
        if (imagePairs == null || imagePairs.isEmpty()) {
            product.setImageUrl(null);
            product.setGalleryImageUrls(null);
            product.setThumbnailUrls(null);
            return;
        }

        product.setImageUrl(imagePairs.get(0).imageUrl());
        product.setGalleryImageUrls(imagePairs.stream()
                .map(StoredProductImage::imageUrl)
                .collect(Collectors.joining(",")));
        product.setThumbnailUrls(imagePairs.stream()
                .map(StoredProductImage::thumbnailUrl)
                .collect(Collectors.joining(",")));
    }

    private List<StoredProductImage> buildStoredImagePairs(Product product) {
        List<String> galleryImages = splitCsv(product.getGalleryImageUrls());
        List<String> thumbnailImages = splitCsv(product.getThumbnailUrls());

        if (galleryImages.size() > 1) {
            return buildImagePairs(galleryImages, thumbnailImages.size() == galleryImages.size() ? thumbnailImages : galleryImages);
        }
        if (galleryImages.size() <= 1 && thumbnailImages.size() > galleryImages.size()) {
            return buildImagePairs(thumbnailImages, thumbnailImages);
        }
        if (galleryImages.size() == 1) {
            return buildImagePairs(galleryImages, thumbnailImages.size() == 1 ? thumbnailImages : galleryImages);
        }
        if (!thumbnailImages.isEmpty()) {
            return buildImagePairs(thumbnailImages, thumbnailImages);
        }
        if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
            return buildImagePairs(List.of(product.getImageUrl().trim()), List.of(product.getImageUrl().trim()));
        }
        return new ArrayList<>();
    }

    private List<StoredProductImage> buildImagePairsFromCsv(String galleryImageUrls, String thumbnailUrls) {
        return buildImagePairs(splitCsv(galleryImageUrls), splitCsv(thumbnailUrls));
    }

    private List<StoredProductImage> buildImagePairs(List<String> galleryImages, List<String> thumbnailImages) {
        List<StoredProductImage> imagePairs = new ArrayList<>();
        for (int index = 0; index < galleryImages.size(); index++) {
            String imageUrl = galleryImages.get(index);
            String thumbnailUrl = index < thumbnailImages.size() ? thumbnailImages.get(index) : imageUrl;
            imagePairs.add(new StoredProductImage(imageUrl, thumbnailUrl));
        }
        return imagePairs;
    }

    private String mediaKey(StoredProductImage image) {
        return image.imageUrl() + "|" + image.thumbnailUrl();
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
