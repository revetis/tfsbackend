package com.example.apps.products.services;

import java.util.List;
import com.example.apps.products.dtos.BrandDTO;
import com.example.apps.products.dtos.BrandDTOIU;

public interface IBrandService {
    List<BrandDTO> getAll();

    BrandDTO getById(Long id);

    BrandDTO create(BrandDTOIU brandDTOIU);

    BrandDTO update(Long id, BrandDTOIU brandDTOIU);

    void delete(Long id);
}
