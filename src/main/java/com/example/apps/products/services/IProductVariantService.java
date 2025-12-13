package com.example.apps.products.services;

import java.util.List;
import com.example.apps.products.dtos.ProductVariantDTO;
import com.example.apps.products.dtos.ProductVariantDTOIU;

public interface IProductVariantService {
    List<ProductVariantDTO> getAll();

    ProductVariantDTO getById(Long id);

    ProductVariantDTO create(ProductVariantDTOIU productVariantDTOIU);

    ProductVariantDTO update(Long id, ProductVariantDTOIU productVariantDTOIU);

    void delete(Long id);
}
