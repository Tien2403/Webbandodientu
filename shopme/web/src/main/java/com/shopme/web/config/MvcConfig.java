package com.shopme.web.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// Expose static directories for images and logos
		exposeDirectory("category-images", registry);
		exposeDirectory("brand-logos", registry);
		exposeDirectory("product-images", registry);
		exposeDirectory("site-logo", registry);
	}
	
	private void exposeDirectory(String dirName, ResourceHandlerRegistry registry) {
		Path uploadDir = Paths.get(dirName);
		String uploadPath = uploadDir.toFile().getAbsolutePath();
		
		String logicalPath = "/" + dirName + "/**";
				
		registry.addResourceHandler(logicalPath)
			.addResourceLocations("file:" + uploadPath + "/");		
	}
}
