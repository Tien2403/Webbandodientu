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

import com.shopme.admin.category.CategoryService;
import com.shopme.common.entity.Category;
import com.shopme.common.exception.CategoryNotFoundException;

@Component
public class ImageMigrationService implements CommandLineRunner {

  @Autowired
  private CategoryService categoryService;

  private static final String CATEGORY_IMAGES_DIR = "category-images";

  @Override
  public void run(String... args) throws Exception {
    // System.out.println("Starting image migration from filesystem to database...");
    // migrateCategoryImages();
    // System.out.println("Image migration completed!");
  }

  public void migrateCategoryImages() {
    try {
      // Get all categories
      List<Category> categories = categoryService.listAll();

      int migratedCount = 0;
      int skippedCount = 0;
      int errorCount = 0;

      for (Category category : categories) {
        try {
          if (migrateCategoryImage(category)) {
            migratedCount++;
            System.out.println("✓ Migrated image for category: " + category.getName() + " (ID: " + category.getId() + ")");
          } else {
            skippedCount++;
            System.out.println("- Skipped category: " + category.getName() + " (ID: " + category.getId() + ") - No image file found");
          }
        } catch (Exception e) {
          errorCount++;
          System.err.println("✗ Error migrating category: " + category.getName() + " (ID: " + category.getId() + ") - " + e.getMessage());
        }
      }

      System.out.println("\n=== Migration Summary ===");
      System.out.println("Total categories processed: " + categories.size());
      System.out.println("Successfully migrated: " + migratedCount);
      System.out.println("Skipped (no image file): " + skippedCount);
      System.out.println("Errors: " + errorCount);

    } catch (Exception e) {
      System.err.println("Error during migration: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private boolean migrateCategoryImage(Category category) throws IOException {
    // Skip if already has Base64 data
    if (category.getImagePath() != null && !category.getImagePath().trim().isEmpty() &&
            category.getImagePath().startsWith("data:")) {
      return false;
    }

    String imageFileName = category.getImage();
    if (imageFileName == null || imageFileName.trim().isEmpty()) {
      return false;
    }

    // Construct file path: category-images/{id}/{image_name}
    String categoryDir = CATEGORY_IMAGES_DIR + File.separator + category.getId();
    Path imagePath = Paths.get(categoryDir, imageFileName);

    // Check if file exists
    if (!Files.exists(imagePath)) {
      return false;
    }

    // Read file bytes and convert to Base64
    byte[] imageBytes = Files.readAllBytes(imagePath);
    String dataUrl = buildBase64ImageUrl(imageFileName, imageBytes);

    // Update category with Base64 data URL
    category.setImagePath(dataUrl);

    // Save to database
    categoryService.save(category);

    return true;
  }

  // Method to migrate a specific category (for manual migration)
  public boolean migrateSpecificCategory(Integer categoryId) {
    try {
      Category category = categoryService.get(categoryId);
      return migrateCategoryImage(category);
    } catch (CategoryNotFoundException e) {
      System.err.println("Category not found with ID: " + categoryId);
      return false;
    } catch (IOException e) {
      System.err.println("Error migrating category ID " + categoryId + ": " + e.getMessage());
      return false;
    }
  }

  // Method to check migration status
  public void checkMigrationStatus() {
    try {
      List<Category> categories = categoryService.listAll();

      int withBase64Data = 0;
      int withFileOnly = 0;
      int noImage = 0;

      for (Category category : categories) {
        if (category.getImagePath() != null && !category.getImagePath().trim().isEmpty() &&
                category.getImagePath().startsWith("data:")) {
          withBase64Data++;
        } else if (category.getImage() != null && !category.getImage().trim().isEmpty()) {
          withFileOnly++;
        } else {
          noImage++;
        }
      }

      System.out.println("\n=== Migration Status ===");
      System.out.println("Total categories: " + categories.size());
      System.out.println("With Base64 data: " + withBase64Data);
      System.out.println("With file only: " + withFileOnly);
      System.out.println("No image: " + noImage);
      System.out.println("Migration progress: " + String.format("%.1f%%", (double) withBase64Data / categories.size() * 100));

    } catch (Exception e) {
      System.err.println("Error checking migration status: " + e.getMessage());
    }
  }
}

// ALTER TABLE categories
// ADD COLUMN image_path LONGTEXT NULL AFTER image;