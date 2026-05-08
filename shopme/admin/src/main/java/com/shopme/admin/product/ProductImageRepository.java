package com.shopme.admin.product;

import com.shopme.common.entity.product.ProductImage;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ProductImageRepository extends PagingAndSortingRepository<ProductImage, Integer> {
}
