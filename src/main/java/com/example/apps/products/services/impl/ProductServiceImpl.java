package com.example.apps.products.services.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.apps.products.dtos.ProductDTO;
import com.example.apps.products.dtos.ProductDTOIU;
import com.example.apps.products.dtos.ProductMaterialDTO;
import com.example.apps.products.dtos.ProductMaterialDTOIU;
import com.example.apps.products.dtos.ProductVariantColorDTO;
import com.example.apps.products.dtos.ProductVariantDTO;
import com.example.apps.products.dtos.ProductVariantDTOIU;
import com.example.apps.products.dtos.ProductVariantImageDTO;
import com.example.apps.products.dtos.ProductVariantStockDTO;
import com.example.apps.products.dtos.search.ProductSavedEvent;
import com.example.apps.products.entities.Product;
import com.example.apps.products.entities.ProductMaterial;
import com.example.apps.products.entities.ProductVariant;
import com.example.apps.products.entities.ProductVariantColor;
import com.example.apps.products.entities.ProductVariantImage;
import com.example.apps.products.entities.ProductVariantStock;
import com.example.apps.products.entities.ProductVariantStockMovement;
import com.example.apps.products.enums.StockMovementType;
import com.example.apps.products.exceptions.ProductException;
import com.example.apps.products.exceptions.ProductVariantException;
import com.example.apps.products.repositories.ProductMaterialRepository;
import com.example.apps.products.repositories.ProductRepository;
import com.example.apps.products.repositories.ProductVariantImageRepository;
import com.example.apps.products.repositories.ProductVariantRepository;
import com.example.apps.products.repositories.ProductVariantStockMovementRepository;
import com.example.apps.products.repositories.ProductVariantStockRepository;
import com.example.apps.products.services.IProductService;
import com.example.tfs.AppConfiguration;
import com.example.tfs.StorageService;

@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductVariantImageRepository productVariantImageRepository;

    @Autowired
    private ProductVariantStockRepository productVariantStockRepository;

    @Autowired
    private ProductVariantStockMovementRepository productVariantStockMovementRepository;

    @Autowired
    private ProductMaterialRepository productMaterialRepository;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTOIU productDTOIU) {
        Product product = new Product();
        BeanUtils.copyProperties(productDTOIU, product); // Variant yok suan
        product.setEnable(true);
        productRepository.save(product);
        eventPublisher.publishEvent(new ProductSavedEvent(product));
        ProductDTO productDTO = new ProductDTO();
        BeanUtils.copyProperties(product, productDTO);
        return productDTO;
    }

    @Override
    @Transactional
    @CacheEvict(value = "cartCheckoutCache", allEntries = true)
    public ProductDTO updateProduct(Long id, ProductDTOIU productDTOIU) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found"));
        BeanUtils.copyProperties(productDTOIU, product);// Sadece Name ve desription degisti diger alanlar ayrica
                                                        // degistirilmeli
        productRepository.save(product);

        eventPublisher.publishEvent(new ProductSavedEvent(product));

        ProductDTO productDTO = new ProductDTO();
        BeanUtils.copyProperties(product, productDTO);
        return productDTO;
    }

    @Override
    @Transactional
    @CacheEvict(value = "cartCheckoutCache", allEntries = true)
    public Boolean deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found"));
        productRepository.delete(product);

        eventPublisher.publishEvent(new ProductSavedEvent(product));

        return true;
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found"));
        ProductDTO productDTO = new ProductDTO();
        BeanUtils.copyProperties(product, productDTO);
        return productDTO;
    }

    @Override
    public Page<ProductDTO> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productsPage = productRepository.findAll(pageable);
        if (productsPage.isEmpty()) {
            return Page.empty();
        }

        List<ProductDTO> dtoList = productsPage.getContent().stream().map(product -> {
            ProductDTO dto = new ProductDTO();
            BeanUtils.copyProperties(product, dto);
            return dto;
        }).toList();

        return new PageImpl<>(dtoList, pageable, productsPage.getTotalElements());
    }

    @Override
    @Transactional
    public ProductVariantDTO addVariant(Long productId, ProductVariantDTOIU variantDTOIU) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException("Product not found"));
        if (variantDTOIU.getStock().getQuantity() <= 0) {
            throw new ProductVariantException("Initial stock must be greater than zero");
        }

        ProductVariant variant = new ProductVariant();
        BeanUtils.copyProperties(variantDTOIU, variant);
        variant.setProduct(product);
        variant.setEnable(variantDTOIU.getEnable());

        // Set stock
        ProductVariantStock stock = new ProductVariantStock();
        BeanUtils.copyProperties(variantDTOIU.getStock(), stock);
        stock.setProductVariant(variant);
        variant.setStock(stock);

        // Set color
        ProductVariantColor color = new ProductVariantColor();
        BeanUtils.copyProperties(variantDTOIU.getColor(), color);
        color.setProductVariant(variant);
        variant.setColor(color);

        // Set images
        List<ProductVariantImage> images = variantDTOIU.getImages().stream().map(imageDTOIU -> {
            ProductVariantImage image = new ProductVariantImage();
            BeanUtils.copyProperties(imageDTOIU, image);
            image.setProductVariant(variant);
            return image;
        }).toList();
        variant.setImages(images);

        product.getVariants().add(variant);
        productRepository.save(product);

        ProductVariantStockMovement stockMovement = new ProductVariantStockMovement();
        stockMovement.setProductVariant(variant);
        stockMovement.setQuantity(variantDTOIU.getStock().getQuantity());
        stockMovement.setType(StockMovementType.INCREASE);
        productVariantStockMovementRepository.save(stockMovement);

        ProductVariantDTO variantDTO = new ProductVariantDTO();
        BeanUtils.copyProperties(variant, variantDTO);
        variantDTO.setStock(ProductVariantStockDTO.builder()
                .id(variant.getStock().getId())
                .quantity(variant.getStock().getQuantity())
                .build());
        variantDTO.setColor(ProductVariantColorDTO.builder()
                .id(variant.getColor().getId())
                .name(variant.getColor().getName())
                .hexCode(variant.getColor().getHexCode())
                .build());
        variantDTO.setImages(variant.getImages().stream().map(image -> ProductVariantImageDTO.builder()
                .id(image.getId())
                .url(image.getUrl())
                .alt(image.getAlt())
                .build()).toList());

        return variantDTO;

    }

    @Override
    @Transactional
    public ProductVariantStockDTO decreaseStock(Long variantId, Long quantity) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ProductVariantException("Product variant not found"));
        if (quantity <= 0) {
            throw new ProductVariantException("Quantity must be greater than zero");
        }

        ProductVariantStock stock = variant.getStock();
        if (stock.getQuantity() < quantity) {
            throw new ProductVariantException("Insufficient stock");
        }

        ProductVariantStockMovement stockMovement = new ProductVariantStockMovement();
        stockMovement.setProductVariant(variant);
        stockMovement.setQuantity(quantity);
        stockMovement.setType(StockMovementType.DECREASE);
        productVariantStockMovementRepository.save(stockMovement);

        stock.setQuantity(stock.getQuantity() - quantity);
        productVariantStockRepository.save(stock);

        return ProductVariantStockDTO.builder()
                .id(stock.getId())
                .quantity(stock.getQuantity())
                .build();
    }

    @Override
    @Transactional
    public ProductVariantStockDTO increaseStock(Long variantId, Long quantity) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ProductVariantException("Product variant not found"));
        if (quantity <= 0) {
            throw new ProductVariantException("Quantity must be greater than zero");
        }

        ProductVariantStockMovement stockMovement = new ProductVariantStockMovement();
        stockMovement.setProductVariant(variant);
        stockMovement.setQuantity(quantity);
        stockMovement.setType(StockMovementType.INCREASE);
        productVariantStockMovementRepository.save(stockMovement);

        ProductVariantStock stock = variant.getStock();
        stock.setQuantity(stock.getQuantity() + quantity);
        productVariantStockRepository.save(stock);

        return ProductVariantStockDTO.builder()
                .id(stock.getId())
                .quantity(stock.getQuantity())
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "cartCheckoutCache", allEntries = true)
    public ProductVariantDTO updateVariant(Long variantId, ProductVariantDTOIU variantDTOIU) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ProductVariantException("Product variant not found"));

        BeanUtils.copyProperties(variantDTOIU, variant);
        variant.setEnable(variantDTOIU.getEnable());

        // Update stock
        ProductVariantStock stock = variant.getStock();
        BeanUtils.copyProperties(variantDTOIU.getStock(), stock);
        stock.setProductVariant(variant); // Ensure bidirectional relationship
        variant.setStock(stock);

        // Update color
        ProductVariantColor color = variant.getColor();
        BeanUtils.copyProperties(variantDTOIU.getColor(), color);
        color.setProductVariant(variant); // Ensure bidirectional relationship
        variant.setColor(color);

        // Update images
        // Clear existing images and add new ones to handle changes
        variant.getImages().clear();
        List<ProductVariantImage> images = variantDTOIU.getImages().stream().map(imageDTOIU -> {
            ProductVariantImage image = new ProductVariantImage();
            BeanUtils.copyProperties(imageDTOIU, image);
            image.setProductVariant(variant); // Ensure bidirectional relationship
            return image;
        }).toList();
        variant.getImages().addAll(images);

        productVariantRepository.save(variant);

        ProductVariantDTO variantDTO = new ProductVariantDTO();
        BeanUtils.copyProperties(variant, variantDTO);
        variantDTO.setStock(ProductVariantStockDTO.builder()
                .id(variant.getStock().getId())
                .quantity(variant.getStock().getQuantity())
                .build());
        variantDTO.setColor(ProductVariantColorDTO.builder()
                .id(variant.getColor().getId())
                .name(variant.getColor().getName())
                .hexCode(variant.getColor().getHexCode())
                .build());
        variantDTO.setImages(variant.getImages().stream().map(image -> ProductVariantImageDTO.builder()
                .id(image.getId())
                .url(image.getUrl())
                .alt(image.getAlt())
                .build()).toList());

        return variantDTO;

    }

    @Override
    @Transactional
    @CacheEvict(value = "cartCheckoutCache", allEntries = true)
    public Boolean deleteVariant(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ProductVariantException("Product variant not found"));
        for (ProductVariantImage image : variant.getImages()) {
            storageService.deleteFile(image.getUrl());
        }
        productVariantRepository.delete(variant);

        return true;

    }

    @Override
    public Long calculateDiscountPrice(Long productVariantId) {
        ProductVariant variant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new ProductVariantException("Product variant not found"));
        return Long.parseLong(variant.getDiscountPrice().toString());
    }

    @Override
    @Transactional
    public ProductVariantImageDTO updateVariantImage(MultipartFile file, Long variantImageId, String alt) {
        ProductVariantImage image = productVariantImageRepository.findById(variantImageId)
                .orElseThrow(() -> new ProductVariantException("Product variant image not found"));

        String oldImageUrl = image.getUrl();
        String filename = AppConfiguration.generateUniqueFileName(file);
        String uploadDir = "uploads/product-variant-images";

        if (storageService.uploadFile(file, uploadDir, filename)) {
            // Delete old image file if upload is successful
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                storageService.deleteFile(oldImageUrl);
            }

            image.setUrl("/" + uploadDir + "/" + filename);
            image.setAlt(alt);
            productVariantImageRepository.save(image);

            return ProductVariantImageDTO.builder()
                    .id(image.getId())
                    .url(image.getUrl())
                    .alt(image.getAlt())
                    .build();
        } else {
            throw new ProductVariantException("Failed to upload image");
        }
    }

    @Override
    @Transactional
    public Boolean deleteVariantImage(Long variantImageId) {
        ProductVariantImage image = productVariantImageRepository.findById(variantImageId)
                .orElseThrow(() -> new ProductVariantException("Product variant image not found"));

        if (storageService.deleteFile(image.getUrl())) {
            productVariantImageRepository.delete(image);
            return true;
        } else {
            throw new ProductVariantException("Failed to delete image file");
        }
    }

    @Override
    @Transactional
    public ProductVariantImageDTO addVariantImage(MultipartFile file, Long variantId, String alt) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ProductVariantException("Product variant not found"));

        String filename = AppConfiguration.generateUniqueFileName(file);
        String uploadDir = "uploads/product-variant-images";

        if (storageService.uploadFile(file, uploadDir, filename)) {
            ProductVariantImage image = new ProductVariantImage();
            image.setUrl("/" + uploadDir + "/" + filename);
            image.setAlt(alt);
            image.setProductVariant(variant);
            productVariantImageRepository.save(image);

            return ProductVariantImageDTO.builder()
                    .id(image.getId())
                    .url(image.getUrl())
                    .alt(image.getAlt())
                    .build();
        } else {
            throw new ProductVariantException("Failed to upload image");
        }
    }

    @Override
    @Transactional
    public ProductMaterialDTO createProductMaterial(ProductMaterialDTOIU productMaterialDTO) {
        ProductMaterial productMaterial = new ProductMaterial();
        BeanUtils.copyProperties(productMaterialDTO, productMaterial);
        productMaterialRepository.save(productMaterial);
        ProductMaterialDTO dto = new ProductMaterialDTO();
        BeanUtils.copyProperties(productMaterial, dto);
        return dto;
    }

    @Override
    @Transactional
    public ProductMaterialDTO updateProductMaterial(Long id, ProductMaterialDTOIU productMaterialDTOIU) {
        ProductMaterial productMaterial = productMaterialRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product material not found"));
        BeanUtils.copyProperties(productMaterialDTOIU, productMaterial);
        productMaterialRepository.save(productMaterial);
        ProductMaterialDTO dto = new ProductMaterialDTO();
        BeanUtils.copyProperties(productMaterial, dto);
        return dto;

    }

    @Override
    @Transactional
    public Boolean deleteProductMaterial(Long id) {
        ProductMaterial productMaterial = productMaterialRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product material not found"));
        productMaterialRepository.delete(productMaterial);
        return true;
    }

    @Override
    public ProductMaterialDTO getProductMaterialById(Long id) {
        ProductMaterial productMaterial = productMaterialRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product material not found"));
        ProductMaterialDTO dto = new ProductMaterialDTO();
        BeanUtils.copyProperties(productMaterial, dto);
        return dto;
    }

    @Override
    public Page<ProductMaterialDTO> getAllProductMaterials(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProductMaterial> materialsPage = productMaterialRepository.findAll(pageable);
        if (materialsPage.isEmpty()) {
            return Page.empty();
        }

        List<ProductMaterialDTO> dtoList = materialsPage.getContent().stream().map(material -> {
            ProductMaterialDTO dto = new ProductMaterialDTO();
            BeanUtils.copyProperties(material, dto);
            return dto;
        }).toList();

        return new PageImpl<>(dtoList, pageable, materialsPage.getTotalElements());
    }

    @Override
    public ProductVariantDTO getVariantById(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ProductVariantException("Product variant not found"));

        ProductVariantDTO variantDTO = new ProductVariantDTO();
        BeanUtils.copyProperties(variant, variantDTO);
        variantDTO.setStock(ProductVariantStockDTO.builder()
                .id(variant.getStock().getId())
                .quantity(variant.getStock().getQuantity())
                .build());
        variantDTO.setColor(ProductVariantColorDTO.builder()
                .id(variant.getColor().getId())
                .name(variant.getColor().getName())
                .hexCode(variant.getColor().getHexCode())
                .build());
        variantDTO.setImages(variant.getImages().stream().map(image -> ProductVariantImageDTO.builder()
                .id(image.getId())
                .url(image.getUrl())
                .alt(image.getAlt())
                .build()).toList());
        return variantDTO;

    }

}
