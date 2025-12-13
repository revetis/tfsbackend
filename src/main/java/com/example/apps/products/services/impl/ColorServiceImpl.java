package com.example.apps.products.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.products.dtos.ColorDTO;
import com.example.apps.products.dtos.ColorDTOIU;
import com.example.apps.products.entities.Color;
import com.example.apps.products.repositories.ColorRepository;
import com.example.apps.products.services.IColorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ColorServiceImpl implements IColorService {

    private final ColorRepository colorRepository;

    @Override
    public List<ColorDTO> getAll() {
        return colorRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ColorDTO getById(Long id) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Color not found with id: " + id));
        return mapToDTO(color);
    }

    @Transactional
    @Override
    public ColorDTO create(ColorDTOIU colorDTOIU) {
        Color color = new Color();
        mapToEntity(color, colorDTOIU);
        Color savedColor = colorRepository.save(color);
        return mapToDTO(savedColor);
    }

    @Transactional
    @Override
    public ColorDTO update(Long id, ColorDTOIU colorDTOIU) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Color not found with id: " + id));
        mapToEntity(color, colorDTOIU);
        Color savedColor = colorRepository.save(color);
        return mapToDTO(savedColor);
    }

    @Override
    public void delete(Long id) {
        if (!colorRepository.existsById(id)) {
            throw new RuntimeException("Color not found with id: " + id);
        }
        colorRepository.deleteById(id);
    }

    private ColorDTO mapToDTO(Color color) {
        if (color == null)
            return null;
        return new ColorDTO(
                color.getId(),
                color.getName(),
                color.getCode(),
                color.getImage(),
                color.getIsActive());
    }

    private void mapToEntity(Color color, ColorDTOIU dto) {
        color.setName(dto.getName());
        color.setCode(dto.getCode());
        color.setImage(dto.getImage());

        color.setIsActive(dto.getIsActive());
    }
}
