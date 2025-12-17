package com.example.apps.products.services.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.products.dtos.ProductImageDTO;
import com.example.apps.products.dtos.ProductImageDTOIU;
import com.example.apps.products.entities.ProductImage;
import com.example.apps.products.entities.ProductVariant;
import com.example.apps.products.repositories.ProductImageRepository;
import com.example.apps.products.repositories.ProductVariantRepository;
import com.example.apps.products.services.IProductImageService;
import com.example.tfs.utils.SlugUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements IProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public List<ProductImageDTO> getAll() {
        return productImageRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductImageDTO getById(Long id) {
        ProductImage image = productImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductImage not found with id: " + id));
        return mapToDTO(image);
    }

    @Transactional
    @Override
    public ProductImageDTO create(ProductImageDTOIU dto) {
        ProductImage image = new ProductImage();

        if (dto.getFile() == null || dto.getFile().isEmpty()) {
            throw new IllegalArgumentException("File is required for creation");
        }
        String imagePath = uploadFile(dto.getFile());

        mapToEntity(image, dto);
        image.setImagePath(imagePath);

        ProductImage savedImage = productImageRepository.save(image);
        return mapToDTO(savedImage);
    }

    @Transactional
    @Override
    public ProductImageDTO update(Long id, ProductImageDTOIU dto) {
        ProductImage image = productImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductImage not found with id: " + id));
        mapToEntity(image, dto);

        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            String imagePath = uploadFile(dto.getFile());
            image.setImagePath(imagePath);
        }

        ProductImage savedImage = productImageRepository.save(image);
        return mapToDTO(savedImage);
    }

    private String uploadFile(MultipartFile file) {
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds 5 MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") || contentType.equals("image/png")
                        || contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("Only JPG, PNG and WEBP files are allowed");
        }

        // Security: Check Magic Bytes
        try {
            byte[] magicBytes = file.getInputStream().readNBytes(4);
            if (!isValidImageHeader(magicBytes, contentType)) {
                throw new IllegalArgumentException("Invalid file content for type: " + contentType);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file stream for validation", e);
        }

        // Security: Sanitize Filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "unknown";
        }
        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        String filename = UUID.randomUUID() + "_" + sanitizedFilename;
        java.nio.file.Path uploadPath = Paths.get("uploads/products");

        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (Exception e) {
                throw new RuntimeException("Error creating upload directory", e);
            }
        }

        try {
            Files.copy(file.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("Error saving file", ex);
        }

        return "/uploads/products/" + filename;
    }

    private boolean isValidImageHeader(byte[] header, String contentType) {
        if (header == null || header.length < 4)
            return false;

        // JPEG: FF D8 FF
        if (contentType.equals("image/jpeg")) {
            return header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF;
        }
        // PNG: 89 50 4E 47
        if (contentType.equals("image/png")) {
            return header[0] == (byte) 0x89 && header[1] == (byte) 0x50 && header[2] == (byte) 0x4E
                    && header[3] == (byte) 0x47;
        }
        // WEBP: RIFF ... WEBP (checking first 4 bytes is RIFF 'R' 'I' 'F' 'F' -> 52 49
        // 46 46)
        if (contentType.equals("image/webp")) {
            return header[0] == (byte) 0x52 && header[1] == (byte) 0x49 && header[2] == (byte) 0x46
                    && header[3] == (byte) 0x46;
        }
        return false;
    }

    @Override
    public void delete(Long id) {
        if (!productImageRepository.existsById(id)) {
            throw new RuntimeException("ProductImage not found with id: " + id);
        }
        productImageRepository.deleteById(id);
    }

    private ProductImageDTO mapToDTO(ProductImage image) {
        if (image == null)
            return null;
        return new ProductImageDTO(
                image.getId(),
                image.getName(),
                image.getDescription(),
                image.getSlug(),
                image.getImagePath(),
                image.getProductVariant() != null ? image.getProductVariant().getId() : null,
                image.getIsActive());
    }

    private void mapToEntity(ProductImage image, ProductImageDTOIU dto) {
        image.setName(dto.getName());
        image.setDescription(dto.getDescription());
        image.setSlug(SlugUtils.toSlug(dto.getName()));
        // ImagePath is handled separately via file upload
        image.setIsActive(dto.getIsActive());

        if (dto.getProductVariantId() != null) {
            ProductVariant variant = productVariantRepository.findById(dto.getProductVariantId())
                    .orElseThrow(() -> new RuntimeException(
                            "ProductVariant not found with id: " + dto.getProductVariantId()));
            image.setProductVariant(variant);
        }
    }
}
