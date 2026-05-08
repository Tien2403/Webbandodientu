package com.shopme.admin;

import static com.shopme.common.utils.ImageUtils.buildBase64ImageUrl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.shopme.admin.product.ProductService;
import com.shopme.common.entity.product.Product;
import com.shopme.common.exception.ProductNotFoundException;

@Component
public class ProductImageMigrationService implements CommandLineRunner {

  @Autowired
  private ProductService productService;

  private static final String PRODUCT_IMAGES_DIR = "product-images";

  @Override
  public void run(String... args) throws Exception {
    // System.out.println("Starting product image migration from filesystem to database...");
    // migrateProductImages();
    // System.out.println("Product image migration completed!");
  }

  public void migrateProductImages() {
    try {
      // Get all products
      List<Product> products = productService.listAll();

      int migratedCount = 0;
      int skippedCount = 0;
      int errorCount = 0;

      for (Product product : products) {
        try {
          if (migrateProductImage(product)) {
            migratedCount++;
            System.out.println("✓ Migrated image for product: " + product.getName() + " (ID: " + product.getId() + ")");
          } else {
            skippedCount++;
            System.out.println("- Skipped product: " + product.getName() + " (ID: " + product.getId() + ") - No image file found");
          }
        } catch (Exception e) {
          errorCount++;
          System.err.println("✗ Error migrating product: " + product.getName() + " (ID: " + product.getId() + ") - " + e.getMessage());
        }
      }

      System.out.println("\n=== Product Migration Summary ===");
      System.out.println("Total products processed: " + products.size());
      System.out.println("Successfully migrated: " + migratedCount);
      System.out.println("Skipped (no image file): " + skippedCount);
      System.out.println("Errors: " + errorCount);

    } catch (Exception e) {
      System.err.println("Error during product migration: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private boolean migrateProductImage(Product product) throws IOException {
    // Skip if already has Base64 data
    if (product.getImagePath() != null && !product.getImagePath().trim().isEmpty() &&
            product.getImagePath().startsWith("data:")) {
      return false;
    }

    String mainImageFileName = product.getMainImage();
    if (mainImageFileName == null || mainImageFileName.trim().isEmpty()) {
      return false;
    }

    // Construct file path: product-images/{id}/{main_image_name}
    String productDir = PRODUCT_IMAGES_DIR + File.separator + product.getId();
    Path imagePath = Paths.get(productDir, mainImageFileName);

    // Check if file exists
    if (!Files.exists(imagePath)) {
      return false;
    }

    // Read file bytes and convert to Base64
    byte[] imageBytes = Files.readAllBytes(imagePath);
    String dataUrl = buildBase64ImageUrl(mainImageFileName, imageBytes);

    // Update product with Base64 data URL
    product.setImagePath(dataUrl);

    // Save to database
    productService.save(product);

    return true;
  }

  // Method to migrate a specific product (for manual migration)
  public boolean migrateSpecificProduct(Integer productId) {
    try {
      Product product = productService.get(productId);
      return migrateProductImage(product);
    } catch (ProductNotFoundException e) {
      System.err.println("Product not found with ID: " + productId);
      return false;
    } catch (IOException e) {
      System.err.println("Error migrating product ID " + productId + ": " + e.getMessage());
      return false;
    }
  }

  // Method to check migration status
  public void checkMigrationStatus() {
    try {
      List<Product> products = productService.listAll();

      int withBase64Data = 0;
      int withFileOnly = 0;
      int noImage = 0;

      for (Product product : products) {
        if (product.getImagePath() != null && !product.getImagePath().trim().isEmpty() &&
                product.getImagePath().startsWith("data:")) {
          withBase64Data++;
        } else if (product.getMainImage() != null && !product.getMainImage().trim().isEmpty()) {
          withFileOnly++;
        } else {
          noImage++;
        }
      }

      System.out.println("\n=== Product Migration Status ===");
      System.out.println("Total products: " + products.size());
      System.out.println("With Base64 data: " + withBase64Data);
      System.out.println("With file only: " + withFileOnly);
      System.out.println("No image: " + noImage);
      System.out.println("Migration progress: " + String.format("%.1f%%", (double) withBase64Data / products.size() * 100));

    } catch (Exception e) {
      System.err.println("Error checking product migration status: " + e.getMessage());
    }
  }
} 