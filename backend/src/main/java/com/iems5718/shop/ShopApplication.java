package com.iems5718.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
        System.out.println("\n=================================");
        System.out.println("IEMS5718 Shop Backend Started!");
        System.out.println("API available at: http://localhost:8080/api");
        System.out.println("=================================\n");
    }
}
