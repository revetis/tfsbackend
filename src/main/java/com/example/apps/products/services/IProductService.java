package com.example.apps.products.services;

import java.util.List;
import com.example.apps.products.dtos.ProductDTO;
import com.example.apps.products.dtos.ProductDTOIU;

public interface IProductService {
    List<ProductDTO> getAll();

    ProductDTO getById(Long id);

    ProductDTO create(ProductDTOIU productDTOIU);

    ProductDTO update(Long id, ProductDTOIU productDTOIU);

    void delete(Long id);
}
