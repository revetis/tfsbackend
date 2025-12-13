package com.example.apps.products.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.example.settings.utils.SlugUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.products.dtos.BrandDTO;
import com.example.apps.products.dtos.BrandDTOIU;
import com.example.apps.products.entities.Brand;
import com.example.apps.products.repositories.BrandRepository;
import com.example.apps.products.services.IBrandService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements IBrandService {

    private final BrandRepository brandRepository;

    @Override
    public List<BrandDTO> getAll() {
        return brandRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BrandDTO getById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));
        return mapToDTO(brand);
    }

    @Transactional
    @Override
    public BrandDTO create(BrandDTOIU brandDTOIU) {
        Brand brand = new Brand();
        mapToEntity(brand, brandDTOIU);
        Brand savedBrand = brandRepository.save(brand);
        return mapToDTO(savedBrand);
    }

    @Transactional
    @Override
    public BrandDTO update(Long id, BrandDTOIU brandDTOIU) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));
        mapToEntity(brand, brandDTOIU);
        Brand savedBrand = brandRepository.save(brand);
        return mapToDTO(savedBrand);
    }

    @Override
    public void delete(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new RuntimeException("Brand not found with id: " + id);
        }
        brandRepository.deleteById(id);
    }

    private BrandDTO mapToDTO(Brand brand) {
        if (brand == null)
            return null;
        return new BrandDTO(
                brand.getId(),
                brand.getName(),
                brand.getDescription(),
                brand.getSlug(),
                brand.getImage(),
                brand.getIsActive());
    }

    private void mapToEntity(Brand brand, BrandDTOIU dto) {
        brand.setName(dto.getName());
        brand.setDescription(dto.getDescription());
        brand.setSlug(SlugUtils.toSlug(dto.getName()));
        brand.setImage(dto.getImage());

        brand.setIsActive(dto.getIsActive());
    }
}
