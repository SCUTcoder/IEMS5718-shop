package com.iems5718.shop.config;

import com.iems5718.shop.model.Category;
import com.iems5718.shop.model.Product;
import com.iems5718.shop.repository.CategoryRepository;
import com.iems5718.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Check if database is empty
        if (categoryRepository.count() == 0 && productRepository.count() == 0) {
            System.out.println("Initializing database with categories and products...");
            
            // Create Categories
            Category electronicsCategory = new Category();
            electronicsCategory.setName("Electronics");
            electronicsCategory = categoryRepository.save(electronicsCategory);
            System.out.println("Created category: " + electronicsCategory.getName() + " (catid: " + electronicsCategory.getCatid() + ")");
            
            Category clothingCategory = new Category();
            clothingCategory.setName("Clothing");
            clothingCategory = categoryRepository.save(clothingCategory);
            System.out.println("Created category: " + clothingCategory.getName() + " (catid: " + clothingCategory.getCatid() + ")");
            
            // Create Products for Electronics Category
            Product laptop = new Product();
            laptop.setName("Gaming Laptop");
            laptop.setDescription("High-performance gaming laptop with latest specs. Experience gaming like never before with our high-performance gaming laptop. Featuring the latest Intel Core i7 processor, NVIDIA RTX 4070 graphics card, and 16GB DDR5 RAM, this powerhouse delivers exceptional performance for gaming, content creation, and multitasking.");
            laptop.setPrice(1299.99);
            laptop.setImageUrl("images/product1.jpg");
            laptop.setThumbnailUrls("images/product1.jpg,images/product1-2.jpg,images/product1-3.jpg,images/product1-4.jpg");
            laptop.setCategory(electronicsCategory);
            laptop.setStockQuantity(15);
            laptop.setActive(true);
            productRepository.save(laptop);
            
            Product headphones = new Product();
            headphones.setName("Wireless Headphones");
            headphones.setDescription("Premium wireless headphones with noise cancellation. Immerse yourself in crystal-clear audio with advanced active noise cancellation technology. Perfect for music lovers, commuters, and professionals.");
            headphones.setPrice(249.99);
            headphones.setImageUrl("images/product2.jpg");
            headphones.setThumbnailUrls("images/product2.jpg");
            headphones.setCategory(electronicsCategory);
            headphones.setStockQuantity(50);
            headphones.setActive(true);
            productRepository.save(headphones);
            
            Product smartwatch = new Product();
            smartwatch.setName("Smart Watch");
            smartwatch.setDescription("Latest smartwatch with health monitoring features. Track your fitness goals, monitor your heart rate, sleep patterns, and stay connected with notifications. Water-resistant design perfect for any lifestyle.");
            smartwatch.setPrice(399.99);
            smartwatch.setImageUrl("images/product3.jpg");
            smartwatch.setThumbnailUrls("images/product3.jpg");
            smartwatch.setCategory(electronicsCategory);
            smartwatch.setStockQuantity(30);
            smartwatch.setActive(true);
            productRepository.save(smartwatch);
            
            Product tablet = new Product();
            tablet.setName("Tablet PC");
            tablet.setDescription("Portable tablet perfect for work and entertainment. Featuring a stunning high-resolution display, powerful processor, and all-day battery life. Whether you're working, studying, or relaxing, this tablet adapts to your needs.");
            tablet.setPrice(599.99);
            tablet.setImageUrl("images/product4.jpg");
            tablet.setThumbnailUrls("images/product4.jpg");
            tablet.setCategory(electronicsCategory);
            tablet.setStockQuantity(25);
            tablet.setActive(true);
            productRepository.save(tablet);
            
            // Create Products for Clothing Category
            Product tshirt = new Product();
            tshirt.setName("Cotton T-Shirt");
            tshirt.setDescription("Comfortable and stylish cotton t-shirt. Made from 100% organic cotton, this t-shirt offers superior comfort and durability. Perfect for everyday wear, available in multiple colors and sizes.");
            tshirt.setPrice(29.99);
            tshirt.setImageUrl("images/product1.jpg"); // Using placeholder image
            tshirt.setThumbnailUrls("images/product1.jpg");
            tshirt.setCategory(clothingCategory);
            tshirt.setStockQuantity(100);
            tshirt.setActive(true);
            productRepository.save(tshirt);
            
            Product jeans = new Product();
            jeans.setName("Classic Jeans");
            jeans.setDescription("Classic fit jeans with premium denim fabric. These timeless jeans feature a comfortable fit and durable construction. Perfect for casual wear, these jeans will become a staple in your wardrobe.");
            jeans.setPrice(79.99);
            jeans.setImageUrl("images/product2.jpg"); // Using placeholder image
            jeans.setThumbnailUrls("images/product2.jpg");
            jeans.setCategory(clothingCategory);
            jeans.setStockQuantity(75);
            jeans.setActive(true);
            productRepository.save(jeans);
            
            System.out.println("Database initialized with " + categoryRepository.count() + " categories and " + productRepository.count() + " products.");
        } else {
            System.out.println("Database already contains data. Skipping initialization.");
        }
    }
}
