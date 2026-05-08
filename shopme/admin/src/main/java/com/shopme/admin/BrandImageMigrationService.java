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

import com.shopme.admin.brand.BrandService;
import com.shopme.common.entity.Brand;
import com.shopme.admin.brand.BrandNotFoundException;

@Component
public class BrandImageMigrationService implements CommandLineRunner {

  @Autowired
  private BrandService brandService;

  private static final String BRAND_LOGOS_DIR = "brand-logos";

  @Override
  public void run(String... args) throws Exception {
    // System.out.println("Starting brand image migration from filesystem to database...");
    // migrateBrandImages();
    // System.out.println("Brand image migration completed!");
  }

  public void migrateBrandImages() {
    try {
      // Get all brands
      List<Brand> brands = brandService.listAll();

      int migratedCount = 0;
      int skippedCount = 0;
      int errorCount = 0;

      for (Brand brand : brands) {
        try {
          if (migrateBrandImage(brand)) {
            migratedCount++;
            System.out.println("✓ Migrated image for brand: " + brand.getName() + " (ID: " + brand.getId() + ")");
          } else {
            skippedCount++;
            System.out.println("- Skipped brand: " + brand.getName() + " (ID: " + brand.getId() + ") - No image file found");
          }
        } catch (Exception e) {
          errorCount++;
          System.err.println("✗ Error migrating brand: " + brand.getName() + " (ID: " + brand.getId() + ") - " + e.getMessage());
        }
      }

      System.out.println("\n=== Brand Migration Summary ===");
      System.out.println("Total brands processed: " + brands.size());
      System.out.println("Successfully migrated: " + migratedCount);
      System.out.println("Skipped (no image file): " + skippedCount);
      System.out.println("Errors: " + errorCount);

    } catch (Exception e) {
      System.err.println("Error during brand migration: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private boolean migrateBrandImage(Brand brand) throws IOException {
    // Skip if already has Base64 data
    if (brand.getImagePath() != null && !brand.getImagePath().trim().isEmpty() &&
            brand.getImagePath().startsWith("data:")) {
      return false;
    }

    String logoFileName = brand.getLogo();
    if (logoFileName == null || logoFileName.trim().isEmpty()) {
      return false;
    }

    // Construct file path: brand-logos/{id}/{logo_name}
    String brandDir = BRAND_LOGOS_DIR + File.separator + brand.getId();
    Path imagePath = Paths.get(brandDir, logoFileName);

    // Check if file exists
    if (!Files.exists(imagePath)) {
      return false;
    }

    // Read file bytes and convert to Base64
    byte[] imageBytes = Files.readAllBytes(imagePath);
    String dataUrl = buildBase64ImageUrl(logoFileName, imageBytes);

    // Update brand with Base64 data URL
    brand.setImagePath(dataUrl);

    // Save to database
    brandService.save(brand);

    return true;
  }

  // Method to migrate a specific brand (for manual migration)
  public boolean migrateSpecificBrand(Integer brandId) {
    try {
      Brand brand = brandService.get(brandId);
      return migrateBrandImage(brand);
    } catch (BrandNotFoundException e) {
      System.err.println("Brand not found with ID: " + brandId);
      return false;
    } catch (IOException e) {
      System.err.println("Error migrating brand ID " + brandId + ": " + e.getMessage());
      return false;
    }
  }

  // Method to check migration status
  public void checkMigrationStatus() {
    try {
      List<Brand> brands = brandService.listAll();

      int withBase64Data = 0;
      int withFileOnly = 0;
      int noImage = 0;

      for (Brand brand : brands) {
        if (brand.getImagePath() != null && !brand.getImagePath().trim().isEmpty() &&
                brand.getImagePath().startsWith("data:")) {
          withBase64Data++;
        } else if (brand.getLogo() != null && !brand.getLogo().trim().isEmpty()) {
          withFileOnly++;
        } else {
          noImage++;
        }
      }

      System.out.println("\n=== Brand Migration Status ===");
      System.out.println("Total brands: " + brands.size());
      System.out.println("With Base64 data: " + withBase64Data);
      System.out.println("With file only: " + withFileOnly);
      System.out.println("No image: " + noImage);
      System.out.println("Migration progress: " + String.format("%.1f%%", (double) withBase64Data / brands.size() * 100));

    } catch (Exception e) {
      System.err.println("Error checking brand migration status: " + e.getMessage());
    }
  }
} 