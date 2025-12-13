package com.example.apps.products.services;

import java.util.List;
import com.example.apps.products.dtos.ColorDTO;
import com.example.apps.products.dtos.ColorDTOIU;

public interface IColorService {
    List<ColorDTO> getAll();

    ColorDTO getById(Long id);

    ColorDTO create(ColorDTOIU colorDTOIU);

    ColorDTO update(Long id, ColorDTOIU colorDTOIU);

    void delete(Long id);
}
