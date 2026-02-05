-- Database initialization script for IEMS5718 Shop
-- This script can be used to recreate the database structure and initial data

-- Create categories table
CREATE TABLE IF NOT EXISTS categories (
    catid INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    pid INTEGER PRIMARY KEY AUTOINCREMENT,
    catid INTEGER NOT NULL,
    name TEXT NOT NULL,
    price REAL NOT NULL,
    description TEXT,
    image_url TEXT,
    thumbnail_urls TEXT,
    stock_quantity INTEGER,
    active INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (catid) REFERENCES categories(catid)
);

-- Insert sample categories
INSERT INTO categories (name) VALUES ('Electronics');
INSERT INTO categories (name) VALUES ('Clothing');

-- Insert sample products
-- Electronics products
INSERT INTO products (catid, name, price, description, image_url, thumbnail_urls, stock_quantity, active) 
VALUES (1, 'Gaming Laptop', 1299.99, 
        'High-performance gaming laptop with latest specs. Experience gaming like never before with our high-performance gaming laptop. Featuring the latest Intel Core i7 processor, NVIDIA RTX 4070 graphics card, and 16GB DDR5 RAM, this powerhouse delivers exceptional performance for gaming, content creation, and multitasking.',
        'images/product1.jpg', 'images/product1.jpg,images/product1-2.jpg,images/product1-3.jpg,images/product1-4.jpg', 15, 1);

INSERT INTO products (catid, name, price, description, image_url, thumbnail_urls, stock_quantity, active) 
VALUES (1, 'Wireless Headphones', 249.99, 
        'Premium wireless headphones with noise cancellation. Immerse yourself in crystal-clear audio with advanced active noise cancellation technology. Perfect for music lovers, commuters, and professionals.',
        'images/product2.jpg', 'images/product2.jpg', 50, 1);

INSERT INTO products (catid, name, price, description, image_url, thumbnail_urls, stock_quantity, active) 
VALUES (1, 'Smart Watch', 399.99, 
        'Latest smartwatch with health monitoring features. Track your fitness goals, monitor your heart rate, sleep patterns, and stay connected with notifications. Water-resistant design perfect for any lifestyle.',
        'images/product3.jpg', 'images/product3.jpg', 30, 1);

INSERT INTO products (catid, name, price, description, image_url, thumbnail_urls, stock_quantity, active) 
VALUES (1, 'Tablet PC', 599.99, 
        'Portable tablet perfect for work and entertainment. Featuring a stunning high-resolution display, powerful processor, and all-day battery life. Whether you''re working, studying, or relaxing, this tablet adapts to your needs.',
        'images/product4.jpg', 'images/product4.jpg', 25, 1);

-- Clothing products
INSERT INTO products (catid, name, price, description, image_url, thumbnail_urls, stock_quantity, active) 
VALUES (2, 'Cotton T-Shirt', 29.99, 
        'Comfortable and stylish cotton t-shirt. Made from 100% organic cotton, this t-shirt offers superior comfort and durability. Perfect for everyday wear, available in multiple colors and sizes.',
        'images/product1.jpg', 'images/product1.jpg', 100, 1);

INSERT INTO products (catid, name, price, description, image_url, thumbnail_urls, stock_quantity, active) 
VALUES (2, 'Classic Jeans', 79.99, 
        'Classic fit jeans with premium denim fabric. These timeless jeans feature a comfortable fit and durable construction. Perfect for casual wear, these jeans will become a staple in your wardrobe.',
        'images/product2.jpg', 'images/product2.jpg', 75, 1);
