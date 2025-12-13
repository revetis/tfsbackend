package com.example.apps.products.services;

import java.util.List;
import com.example.apps.products.dtos.ProductMaterialDTO;
import com.example.apps.products.dtos.ProductMaterialDTOIU;

public interface IProductMaterialService {
    List<ProductMaterialDTO> getAll();

    ProductMaterialDTO getById(Long id);

    ProductMaterialDTO create(ProductMaterialDTOIU productMaterialDTOIU);

    ProductMaterialDTO update(Long id, ProductMaterialDTOIU productMaterialDTOIU);

    void delete(Long id);
}
