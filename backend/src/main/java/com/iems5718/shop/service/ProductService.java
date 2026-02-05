package com.iems5718.shop.service;

import com.iems5718.shop.model.Category;
import com.iems5718.shop.model.Product;
import com.iems5718.shop.repository.CategoryRepository;
import com.iems5718.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ImageService imageService;
    
    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrue();
    }
    
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    public List<Product> getProductsByCategory(Long catid) {
        return productRepository.findByCategoryCatidAndActiveTrue(catid);
    }
    
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword);
    }
    
    public Product createProduct(Product product) {
        // Ensure category is loaded
        if (product.getCategory() != null && product.getCategory().getCatid() != null) {
            Category category = categoryRepository.findById(product.getCategory().getCatid())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }
        return productRepository.save(product);
    }
    
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setImageUrl(productDetails.getImageUrl());
        product.setThumbnailUrls(productDetails.getThumbnailUrls());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setActive(productDetails.getActive());
        
        // Update category if provided
        if (productDetails.getCategory() != null && productDetails.getCategory().getCatid() != null) {
            Category category = categoryRepository.findById(productDetails.getCategory().getCatid())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }
        
        return productRepository.save(product);
    }
    
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }
    
    public Product createProductWithImage(Long catid, String name, Double price, String description, 
                                         Integer stockQuantity, MultipartFile image) throws Exception {
        // Create product
        Category category = categoryRepository.findById(catid)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        Product product = new Product();
        product.setCategory(category);
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        product.setStockQuantity(stockQuantity);
        product.setActive(true);
        
        // Save product first to get ID
        product = productRepository.save(product);
        
        // Upload image if provided
        if (image != null && !image.isEmpty()) {
            String[] imagePaths = imageService.uploadProductImage(image, product.getPid());
            product.setImageUrl(imagePaths[0]);
            product.setThumbnailUrls(imagePaths[1]);
            product = productRepository.save(product);
        }
        
        return product;
    }
    
    public Product updateProductWithImage(Long id, Long catid, String name, Double price, 
                                         String description, Integer stockQuantity, MultipartFile image) throws Exception {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Update fields if provided
        if (catid != null) {
            Category category = categoryRepository.findById(catid)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }
        if (name != null) product.setName(name);
        if (price != null) product.setPrice(price);
        if (description != null) product.setDescription(description);
        if (stockQuantity != null) product.setStockQuantity(stockQuantity);
        
        // Upload new image if provided
        if (image != null && !image.isEmpty()) {
            // Delete old images
            imageService.deleteProductImages(product.getImageUrl(), product.getThumbnailUrls());
            
            // Upload new image
            String[] imagePaths = imageService.uploadProductImage(image, product.getPid());
            product.setImageUrl(imagePaths[0]);
            product.setThumbnailUrls(imagePaths[1]);
        }
        
        return productRepository.save(product);
    }
}
