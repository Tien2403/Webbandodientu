package com.shopme.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlUtils {
  
  public static boolean isResourceURL(String url) {
    return url.endsWith(".css") || url.endsWith(".js") || url.endsWith(".png") || url.endsWith(".jpg");
  }
}
