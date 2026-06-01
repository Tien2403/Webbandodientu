package com.shopme.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.shopme.common.entity")
public class ShopmeWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopmeWebApplication.class, args);
    }

}
