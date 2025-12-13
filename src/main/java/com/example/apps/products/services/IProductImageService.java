package com.example.apps.products.services;

import java.util.List;
import com.example.apps.products.dtos.ProductImageDTO;
import com.example.apps.products.dtos.ProductImageDTOIU;

public interface IProductImageService {
    List<ProductImageDTO> getAll();

    ProductImageDTO getById(Long id);

    ProductImageDTO create(ProductImageDTOIU productImageDTOIU);

    ProductImageDTO update(Long id, ProductImageDTOIU productImageDTOIU);

    void delete(Long id);
}
