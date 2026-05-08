package com.shopme.common.utils;

import org.junit.jupiter.api.Test;
import org.springframework.util.MimeTypeUtils;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {
  
  @Test
  void testGetFileExtension_validFile() {
    assertEquals("png", FileUtils.getFileExtension("image.png"));
    assertEquals("jpeg", FileUtils.getFileExtension("photo.jpeg"));
    assertEquals("jpg", FileUtils.getFileExtension("pic.jpg"));
  }
  
  @Test
  void testGetFileExtension_noExtension() {
    Exception exception = assertThrows(IllegalArgumentException.class, () ->
                                                                           FileUtils.getFileExtension("invalidfile")
    );
    assertEquals("Not a valid file name", exception.getMessage());
  }
  
  @Test
  void testGetFileExtension_nullInput() {
    assertNull(FileUtils.getFileExtension(null));
  }
  
  @Test
  void testGetMimeType_png() {
    assertEquals(MimeTypeUtils.IMAGE_PNG, FileUtils.getMimeType("png"));
  }
  
  @Test
  void testGetMimeType_jpeg() {
    assertEquals(MimeTypeUtils.IMAGE_JPEG, FileUtils.getMimeType("jpeg"));
    assertEquals(MimeTypeUtils.IMAGE_JPEG, FileUtils.getMimeType("jpg"));
  }
  
  @Test
  void testGetMimeType_unknownExtension() {
    assertEquals(MimeTypeUtils.APPLICATION_OCTET_STREAM, FileUtils.getMimeType("exe"));
  }
  
  @Test
  void testGetContentType_png() {
    assertEquals("image/png", FileUtils.getContentType("png"));
  }
}
