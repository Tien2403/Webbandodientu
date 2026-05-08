package com.shopme.admin;

import static com.shopme.common.utils.ImageUtils.buildBase64ImageUrl;

import com.shopme.admin.product.ProductImageRepository;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.shopme.admin.product.ProductService;
import com.shopme.common.entity.product.Product;
import com.shopme.common.entity.product.ProductImage;
import com.shopme.common.exception.ProductNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductExtraImageMigrationService implements CommandLineRunner {

  @Autowired
  private ProductService productService;
  
  @Autowired
  private ProductImageRepository productImageRepository;

  private static final String PRODUCT_IMAGES_DIR = "product-images";

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    // System.out.println("Starting product extra image migration from filesystem to database...");
    // migrateProductExtraImages();
    // System.out.println("Product extra image migration completed!");
  }
  
  public void migrateProductExtraImages() {
    try {
      // Get all products
      List<Product> products = productService.listAll();

      int totalExtraImages = 0;
      int migratedCount = 0;
      int skippedCount = 0;
      int errorCount = 0;

      for (Product product : products) {
        try {
          Set<ProductImage> extraImages = product.getImages();
          if (extraImages != null && !extraImages.isEmpty()) {
            totalExtraImages += extraImages.size();
            
            for (ProductImage extraImage : extraImages) {
              try {
                if (migrateProductExtraImage(extraImage, product.getId())) {
                  migratedCount++;
                  System.out.println("✓ Migrated extra image: " + extraImage.getName() + " for product: " + product.getName() + " (ID: " + product.getId() + ")");
                } else {
                  skippedCount++;
                  System.out.println("- Skipped extra image: " + extraImage.getName() + " for product: " + product.getName() + " (ID: " + product.getId() + ") - No image file found");
                }
              } catch (Exception e) {
                errorCount++;
                System.err.println("✗ Error migrating extra image: " + extraImage.getName() + " for product: " + product.getName() + " (ID: " + product.getId() + ") - " + e.getMessage());
              }
            }
          }
        } catch (Exception e) {
          System.err.println("✗ Error processing product: " + product.getName() + " (ID: " + product.getId() + ") - " + e.getMessage());
        }
      }

      System.out.println("\n=== Product Extra Image Migration Summary ===");
      System.out.println("Total products processed: " + products.size());
      System.out.println("Total extra images found: " + totalExtraImages);
      System.out.println("Successfully migrated: " + migratedCount);
      System.out.println("Skipped (no image file): " + skippedCount);
      System.out.println("Errors: " + errorCount);

    } catch (Exception e) {
      System.err.println("Error during product extra image migration: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private boolean migrateProductExtraImage(ProductImage extraImage, Integer productId) throws IOException, ProductNotFoundException {
    // Skip if already has Base64 data
    if (extraImage.getImagePath() != null && !extraImage.getImagePath().trim().isEmpty() &&
            extraImage.getImagePath().startsWith("data:")) {
      return false;
    }

    String extraImageFileName = extraImage.getName();
    if (extraImageFileName == null || extraImageFileName.trim().isEmpty()) {
      return false;
    }

    // Construct file path: product-images/{id}/extras/{extra_image_name}
    String extrasDir = PRODUCT_IMAGES_DIR + File.separator + productId + File.separator + "extras";
    Path imagePath = Paths.get(extrasDir, extraImageFileName);

    // Check if file exists
    if (!Files.exists(imagePath)) {
      return false;
    }

    // Read file bytes and convert to Base64
    byte[] imageBytes = Files.readAllBytes(imagePath);
    String dataUrl = buildBase64ImageUrl(extraImageFileName, imageBytes);

    // Update extra image with Base64 data URL
    extraImage.setImagePath(dataUrl);
    productImageRepository.save(extraImage);

    // Save to database by updating the product
    // Product product = productService.get(productId);
    
    // productService.save(product);

    return true;
  }

  // Method to migrate extra images for a specific product (for manual migration)
  public boolean migrateSpecificProductExtraImages(Integer productId) {
    try {
      Product product = productService.get(productId);
      Set<ProductImage> extraImages = product.getImages();
      
      if (extraImages == null || extraImages.isEmpty()) {
        System.out.println("No extra images found for product ID: " + productId);
        return false;
      }

      int migratedCount = 0;
      int totalCount = extraImages.size();

      for (ProductImage extraImage : extraImages) {
        try {
          if (migrateProductExtraImage(extraImage, productId)) {
            migratedCount++;
          }
        } catch (IOException e) {
          System.err.println("Error migrating extra image: " + extraImage.getName() + " - " + e.getMessage());
        }
      }

      System.out.println("Migrated " + migratedCount + " out of " + totalCount + " extra images for product ID: " + productId);
      return migratedCount > 0;

    } catch (ProductNotFoundException e) {
      System.err.println("Product not found with ID: " + productId);
      return false;
    } catch (Exception e) {
      System.err.println("Error migrating extra images for product ID " + productId + ": " + e.getMessage());
      return false;
    }
  }

  // Method to migrate a specific extra image by product ID and image name
  public boolean migrateSpecificExtraImage(Integer productId, String imageName) {
    try {
      Product product = productService.get(productId);
      Set<ProductImage> extraImages = product.getImages();
      
      if (extraImages == null || extraImages.isEmpty()) {
        System.out.println("No extra images found for product ID: " + productId);
        return false;
      }

      for (ProductImage extraImage : extraImages) {
        if (extraImage.getName().equals(imageName)) {
          try {
            return migrateProductExtraImage(extraImage, productId);
          } catch (IOException e) {
            System.err.println("Error migrating extra image: " + imageName + " - " + e.getMessage());
            return false;
          }
        }
      }

      System.out.println("Extra image not found: " + imageName + " for product ID: " + productId);
      return false;

    } catch (ProductNotFoundException e) {
      System.err.println("Product not found with ID: " + productId);
      return false;
    } catch (Exception e) {
      System.err.println("Error migrating extra image for product ID " + productId + ": " + e.getMessage());
      return false;
    }
  }

  // Method to check migration status
  public void checkMigrationStatus() {
    try {
      List<Product> products = productService.listAll();

      int totalExtraImages = 0;
      int withBase64Data = 0;
      int withFileOnly = 0;
      int noImage = 0;

      for (Product product : products) {
        Set<ProductImage> extraImages = product.getImages();
        if (extraImages != null && !extraImages.isEmpty()) {
          totalExtraImages += extraImages.size();
          
          for (ProductImage extraImage : extraImages) {
            if (extraImage.getImagePath() != null && !extraImage.getImagePath().trim().isEmpty() &&
                    extraImage.getImagePath().startsWith("data:")) {
              withBase64Data++;
            } else if (extraImage.getName() != null && !extraImage.getName().trim().isEmpty()) {
              withFileOnly++;
            } else {
              noImage++;
            }
          }
        }
      }

      System.out.println("\n=== Product Extra Image Migration Status ===");
      System.out.println("Total products: " + products.size());
      System.out.println("Total extra images: " + totalExtraImages);
      System.out.println("With Base64 data: " + withBase64Data);
      System.out.println("With file only: " + withFileOnly);
      System.out.println("No image: " + noImage);
      if (totalExtraImages > 0) {
        System.out.println("Migration progress: " + String.format("%.1f%%", (double) withBase64Data / totalExtraImages * 100));
      }

    } catch (Exception e) {
      System.err.println("Error checking product extra image migration status: " + e.getMessage());
    }
  }
} 