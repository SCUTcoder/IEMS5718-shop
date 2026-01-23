package com.iems5718.shop.config;

import com.iems5718.shop.model.Product;
import com.iems5718.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Check if database is empty
        if (productRepository.count() == 0) {
            System.out.println("Initializing database with sample products...");
            
            // Product 1: Gaming Laptop
            Product laptop = new Product();
            laptop.setName("Gaming Laptop");
            laptop.setDescription("High-performance gaming laptop with latest specs. Experience gaming like never before with our high-performance gaming laptop. Featuring the latest Intel Core i7 processor, NVIDIA RTX 4070 graphics card, and 16GB DDR5 RAM, this powerhouse delivers exceptional performance for gaming, content creation, and multitasking.");
            laptop.setPrice(1299.99);
            laptop.setImageUrl("images/product1.jpg");
            laptop.setThumbnailUrls("images/product1.jpg,images/product1-2.jpg,images/product1-3.jpg,images/product1-4.jpg");
            laptop.setCategory("Electronics");
            laptop.setStockQuantity(15);
            laptop.setActive(true);
            productRepository.save(laptop);
            
            // Product 2: Wireless Headphones
            Product headphones = new Product();
            headphones.setName("Wireless Headphones");
            headphones.setDescription("Premium wireless headphones with noise cancellation. Immerse yourself in crystal-clear audio with advanced active noise cancellation technology. Perfect for music lovers, commuters, and professionals.");
            headphones.setPrice(249.99);
            headphones.setImageUrl("images/product2.jpg");
            headphones.setThumbnailUrls("images/product2.jpg");
            headphones.setCategory("Electronics");
            headphones.setStockQuantity(50);
            headphones.setActive(true);
            productRepository.save(headphones);
            
            // Product 3: Smart Watch
            Product smartwatch = new Product();
            smartwatch.setName("Smart Watch");
            smartwatch.setDescription("Latest smartwatch with health monitoring features. Track your fitness goals, monitor your heart rate, sleep patterns, and stay connected with notifications. Water-resistant design perfect for any lifestyle.");
            smartwatch.setPrice(399.99);
            smartwatch.setImageUrl("images/product3.jpg");
            smartwatch.setThumbnailUrls("images/product3.jpg");
            smartwatch.setCategory("Electronics");
            smartwatch.setStockQuantity(30);
            smartwatch.setActive(true);
            productRepository.save(smartwatch);
            
            // Product 4: Tablet PC
            Product tablet = new Product();
            tablet.setName("Tablet PC");
            tablet.setDescription("Portable tablet perfect for work and entertainment. Featuring a stunning high-resolution display, powerful processor, and all-day battery life. Whether you're working, studying, or relaxing, this tablet adapts to your needs.");
            tablet.setPrice(599.99);
            tablet.setImageUrl("images/product4.jpg");
            tablet.setThumbnailUrls("images/product4.jpg");
            tablet.setCategory("Electronics");
            tablet.setStockQuantity(25);
            tablet.setActive(true);
            productRepository.save(tablet);
            
            System.out.println("Database initialized with " + productRepository.count() + " products.");
        } else {
            System.out.println("Database already contains data. Skipping initialization.");
        }
    }
}
