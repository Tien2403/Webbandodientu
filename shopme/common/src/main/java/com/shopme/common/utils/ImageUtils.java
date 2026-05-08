package com.shopme.common.utils;

import static com.shopme.common.utils.FileUtils.getContentType;
import static com.shopme.common.utils.FileUtils.getFileExtension;

import java.util.Base64;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageUtils {
  
  private static final String BASE64_IMAGE_URL_PATTERN = "data:%s;base64,%s";
  
  public static String buildBase64ImageUrl(String fileName, byte[] data) {
    String extension = getFileExtension(fileName);
    String contentType = getContentType(extension);
    String base64Image = Base64.getEncoder().encodeToString(data);
    
    // The pattern: data:{contentType};base64,{base64Image}
    return String.format(BASE64_IMAGE_URL_PATTERN, contentType, base64Image);
  }
}
