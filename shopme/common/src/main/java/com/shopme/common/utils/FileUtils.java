package com.shopme.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {
  
  public static String getFileExtension(String originalFileName) {
    if (originalFileName == null) {
      return null;
    }
    int dotPos = originalFileName.lastIndexOf(".");
    
    if (dotPos < 0) {
      throw new IllegalArgumentException("Not a valid file name");
    }
    
    return originalFileName.substring(dotPos + 1);
  }
  
  public static MimeType getMimeType(String extension) {
    return switch (extension) {
      case "png" -> MimeTypeUtils.IMAGE_PNG;
      case "jpeg", "jpg" -> MimeTypeUtils.IMAGE_JPEG;
      default -> MimeTypeUtils.APPLICATION_OCTET_STREAM;
    };
  }
  
  public static String getContentType(String extension) {
    MimeType mimeType = getMimeType(extension);
    
    if (mimeType == null) {
      return null;
    }
    
    return mimeType.toString();
  }
  
}
