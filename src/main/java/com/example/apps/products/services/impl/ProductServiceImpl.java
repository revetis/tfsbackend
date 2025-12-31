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

import com.example.apps.products.dtos.MainCategoryDTO;
import com.example.apps.products.dtos.ProductDTO;
import com.example.apps.products.dtos.ProductDTOIU;
import com.example.apps.products.dtos.ProductMaterialDTO;
import com.example.apps.products.dtos.ProductMaterialDTOIU;
import com.example.apps.products.dtos.ProductVariantColorDTO;
import com.example.apps.products.dtos.ProductVariantDTO;
import com.example.apps.products.dtos.ProductVariantDTOIU;
import com.example.apps.products.dtos.ProductVariantImageDTO;
import com.example.apps.products.dtos.ProductVariantStockDTO;
import com.example.apps.products.dtos.SubCategoryDTO;
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
import com.example.apps.products.enums.Gender;
import com.example.apps.products.repositories.ProductVariantStockRepository;
import com.example.apps.products.services.IProductService;
import com.example.tfs.AppConfiguration;
import com.example.tfs.StorageService;
import com.example.apps.products.mappers.ProductMapper;

@Service
@lombok.extern.slf4j.Slf4j
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
    private com.example.apps.products.repositories.SubCategoryRepository subCategoryRepository;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private com.example.apps.products.repositories.search.ProductDocumentRepository productDocumentRepository;

    @Autowired
    private ProductMapper productMapper;

    private void syncToElasticsearch(Product product) {
        if (product == null)
            return;
        try {
            productDocumentRepository.save(productMapper.toDocument(product));
            log.info("Product synced to Elasticsearch: ID {}", product.getId());
        } catch (Exception e) {
            log.error("Failed to sync product to Elasticsearch: ID {}", product.getId(), e);
        }
    }

    private void syncToElasticsearch(Long productId) {
        if (productId == null)
            return;
        productRepository.findById(productId).ifPresent(this::syncToElasticsearch);
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTOIU productDTOIU) {
        Product product = new Product();
        BeanUtils.copyProperties(productDTOIU, product);

        product.setSubCategory(subCategoryRepository.findById(productDTOIU.getSubCategoryId())
                .orElseThrow(() -> new ProductException("Sub category not found")));

        product.setProductMaterial(productMaterialRepository.findById(productDTOIU.getMaterialId())
                .orElseThrow(() -> new ProductException("Product material not found")));

        if (productDTOIU.getGender() != null) {
            product.setGender(Gender.valueOf(productDTOIU.getGender().toUpperCase()));
        }

        product.setEnable(true);
        productRepository.save(product);

        syncToElasticsearch(product);
        eventPublisher.publishEvent(new ProductSavedEvent(product));
        return convertToDTO(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cartCheckoutCache", allEntries = true)
    public ProductDTO updateProduct(Long id, ProductDTOIU productDTOIU) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found"));

        // Manual mapping for safer updates
        product.setName(productDTOIU.getName());
        product.setDescription(productDTOIU.getDescription());
        product.setTaxRatio(productDTOIU.getTaxRatio());
        product.setBrand(productDTOIU.getBrand());
        product.setSizeChart(productDTOIU.getSizeChart());
        product.setCareInstructions(productDTOIU.getCareInstructions());
        product.setOrigin(productDTOIU.getOrigin());
        product.setQuality(productDTOIU.getQuality());
        product.setStyle(productDTOIU.getStyle());
        product.setSeason(productDTOIU.getSeason());
        product.setEnable(productDTOIU.getEnable() != null ? productDTOIU.getEnable() : product.getEnable());

        if (productDTOIU.getSubCategoryId() != null) {
            product.setSubCategory(subCategoryRepository.findById(productDTOIU.getSubCategoryId())
                    .orElseThrow(() -> new ProductException("Sub category not found")));
        }

        if (productDTOIU.getMaterialId() != null) {
            product.setProductMaterial(productMaterialRepository.findById(productDTOIU.getMaterialId())
                    .orElseThrow(() -> new ProductException("Product material not found")));
        }

        if (productDTOIU.getGender() != null) {
            try {
                product.setGender(Gender.valueOf(productDTOIU.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid gender or handle appropriately
            }
        }

        productRepository.save(product);
        syncToElasticsearch(product);
        eventPublisher.publishEvent(new ProductSavedEvent(product));

        return convertToDTO(product);
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(product, dto);

        // Manual mapping for nested DTOs
        if (product.getProductMaterial() != null) {
            ProductMaterialDTO materialDTO = new ProductMaterialDTO();
            BeanUtils.copyProperties(product.getProductMaterial(), materialDTO);
            dto.setMaterial(materialDTO);
        }

        if (product.getSubCategory() != null) {
            SubCategoryDTO subCategoryDTO = new SubCategoryDTO();
            BeanUtils.copyProperties(product.getSubCategory(), subCategoryDTO);

            if (product.getSubCategory().getMainCategory() != null) {
                MainCategoryDTO mainCategoryDTO = new MainCategoryDTO();
                BeanUtils.copyProperties(product.getSubCategory().getMainCategory(), mainCategoryDTO);
                subCategoryDTO.setMainCategory(mainCategoryDTO);
            }

            dto.setSubCategory(subCategoryDTO);
        }

        if (product.getVariants() != null) {
            dto.setVariants(product.getVariants().stream().map(this::convertVariantToDTO).toList());
        }

        // New fields
        if (product.getGender() != null) {
            dto.setGender(product.getGender().name());
        }
        dto.setSizeChart(product.getSizeChart());
        dto.setBrand(product.getBrand());
        dto.setCareInstructions(product.getCareInstructions());
        dto.setOrigin(product.getOrigin());
        dto.setQuality(product.getQuality());
        dto.setStyle(product.getStyle());
        dto.setSeason(product.getSeason());

        return dto;
    }

    private ProductVariantDTO convertVariantToDTO(ProductVariant variant) {
        ProductVariantDTO variantDTO = new ProductVariantDTO();
        BeanUtils.copyProperties(variant, variantDTO);

        // Map BigDecimal prices directly
        variantDTO.setPrice(variant.getPrice());
        variantDTO.setDiscountPrice(variant.getDiscountPrice());

        if (variant.getProduct() != null) {
            variantDTO.setProductId(variant.getProduct().getId());
            variantDTO.setProductName(variant.getProduct().getName());
        }

        if (variant.getStocks() != null) {
            variantDTO.setStocks(variant.getStocks().stream()
                    .<ProductVariantStockDTO>map(stockItem -> ProductVariantStockDTO.builder()
                            .id(stockItem.getId())
                            .quantity(stockItem.getQuantity())
                            .sku(stockItem.getSku())
                            .size(stockItem.getSize())
                            .build())
                    .collect(java.util.stream.Collectors.toList()));
        }

        if (variant.getColor() != null) {
            variantDTO.setColor(ProductVariantColorDTO.builder()
                    .id(variant.getColor().getId())
                    .name(variant.getColor().getName())
                    .hexCode(variant.getColor().getHexCode())
                    .build());
        }

        if (variant.getImages() != null) {
            String baseUrl = "http://localhost:8080";
            variantDTO.setImages(variant.getImages().stream().map(image -> {
                String imageUrl = image.getUrl();
                if (imageUrl != null && !imageUrl.startsWith("http")) {
                    imageUrl = baseUrl + (imageUrl.startsWith("/") ? "" : "/") + imageUrl;
                }
                return ProductVariantImageDTO.builder()
                        .id(image.getId())
                        .url(imageUrl)
                        .alt(image.getAlt())
                        .build();
            }).toList());
        }

        return variantDTO;
    }

    @Override
    public Page<ProductVariantDTO> getAllVariants(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProductVariant> variantsPage = productVariantRepository.findAll(pageable);
        return variantsPage.map(this::convertVariantToDTO);
    }

    @Override
    @Transactional
    public ProductVariantDTO createVariant(ProductVariantDTOIU variantDTOIU) {
        if (variantDTOIU.getProductId() == null) {
            throw new ProductVariantException("Product ID is required for variant creation");
        }
        return addVariant(variantDTOIU.getProductId(), variantDTOIU);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cartCheckoutCache", allEntries = true)
    public Boolean deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found"));

        // Cleanup images from storage for all variants
        if (product.getVariants() != null) {
            for (ProductVariant variant : product.getVariants()) {
                if (variant.getImages() != null) {
                    for (ProductVariantImage image : variant.getImages()) {
                        storageService.deleteFile(image.getUrl());
                    }
                }
            }
        }

        productRepository.delete(product);

        // Delete from Elasticsearch
        try {
            productDocumentRepository.deleteById(String.valueOf(id));
            log.info("Product deleted from Elasticsearch: ID {}", id);
        } catch (Exception e) {
            log.error("Failed to delete product from Elasticsearch: ID {}", id, e);
        }

        return true;
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found"));
        return convertToDTO(product);
    }

    @Override
    public Page<ProductDTO> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productsPage = productRepository.findAll(pageable);
        if (productsPage.isEmpty()) {
            return Page.empty();
        }

        List<ProductDTO> dtoList = productsPage.getContent().stream()
                .map(this::convertToDTO)
                .toList();

        return new PageImpl<>(dtoList, pageable, productsPage.getTotalElements());

    }

    @Override
    @Transactional
    public ProductVariantDTO addVariant(Long productId, ProductVariantDTOIU variantDTOIU) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException("Product not found"));
        if (variantDTOIU.getStocks() == null || variantDTOIU.getStocks().isEmpty()) {
            throw new ProductVariantException("At least one size/stock is required");
        }

        ProductVariant variant = new ProductVariant();
        BeanUtils.copyProperties(variantDTOIU, variant);
        variant.setProduct(product);
        variant.setEnable(variantDTOIU.getEnable());

        // Set stocks
        List<ProductVariantStock> stocks = variantDTOIU.getStocks().stream().map(stockDTOIU -> {
            ProductVariantStock stock = new ProductVariantStock();
            BeanUtils.copyProperties(stockDTOIU, stock);
            stock.setProductVariant(variant);

            // Generate SKU: PRODUCT_ID-VARIANT_NAME-SIZE-TIMESTAMP
            String sku = String.format("%d-%s-%s-%d",
                    productId,
                    variantDTOIU.getName().replaceAll("\\s+", "-").toUpperCase(),
                    stockDTOIU.getSize().name(),
                    System.currentTimeMillis());
            stock.setSku(sku);
            return stock;
        }).collect(java.util.stream.Collectors.toList());

        variant.setStocks(stocks);

        // Calculate discount price if discount ratio is set
        if (variant.getDiscountRatio() != null && variant.getDiscountRatio() > 0) {
            java.math.BigDecimal discountPrice = variant.getPrice()
                    .multiply(java.math.BigDecimal.ONE.subtract(
                            java.math.BigDecimal.valueOf(variant.getDiscountRatio())
                                    .divide(java.math.BigDecimal.valueOf(100))));
            variant.setDiscountPrice(discountPrice);
        }

        // Set color
        ProductVariantColor color = new ProductVariantColor();
        BeanUtils.copyProperties(variantDTOIU.getColor(), color);
        color.setProductVariant(variant);
        variant.setColor(color);

        // Set images
        List<ProductVariantImage> images = new java.util.ArrayList<>();
        if (variantDTOIU.getImages() != null) {
            images = variantDTOIU.getImages().stream().map(imageDTOIU -> {
                ProductVariantImage image = new ProductVariantImage();
                BeanUtils.copyProperties(imageDTOIU, image);
                image.setProductVariant(variant);
                return image;
            }).toList();
        }
        variant.setImages(images);

        // Save variant directly to ensure it gets an ID immediately
        ProductVariant savedVariant = productVariantRepository.save(variant);
        syncToElasticsearch(savedVariant.getProduct());

        // Now create stock movement with the saved variant that has an ID
        // Create stock movements for each saved variant stock
        for (ProductVariantStock stockItem : savedVariant.getStocks()) {
            ProductVariantStockMovement stockMovement = new ProductVariantStockMovement();
            stockMovement.setProductVariant(savedVariant);
            stockMovement.setQuantity(stockItem.getQuantity());
            stockMovement.setType(StockMovementType.INCREASE);
            productVariantStockMovementRepository.save(stockMovement);
        }

        ProductVariantDTO variantDTO = new ProductVariantDTO();
        BeanUtils.copyProperties(savedVariant, variantDTO);
        variantDTO.setStocks(savedVariant.getStocks().stream()
                .<ProductVariantStockDTO>map(stockItem -> ProductVariantStockDTO.builder()
                        .id(stockItem.getId())
                        .quantity(stockItem.getQuantity())
                        .sku(stockItem.getSku())
                        .size(stockItem.getSize())
                        .build())
                .collect(java.util.stream.Collectors.toList()));
        variantDTO.setColor(ProductVariantColorDTO.builder()
                .id(savedVariant.getColor().getId())
                .name(savedVariant.getColor().getName())
                .hexCode(savedVariant.getColor().getHexCode())
                .build());
        variantDTO.setImages(savedVariant.getImages().stream().map(image -> ProductVariantImageDTO.builder()
                .id(image.getId())
                .url(image.getUrl())
                .alt(image.getAlt())
                .build()).toList());

        return variantDTO;

    }

    @Override
    @Transactional
    public ProductVariantStockDTO decreaseStock(Long variantId, Long quantity,
            com.example.apps.products.enums.ProductSize size) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ProductVariantException("Product variant not found"));
        if (quantity <= 0) {
            throw new ProductVariantException("Quantity must be greater than zero");
        }

        ProductVariantStock stock = findStockBySize(variant, size);
        if (stock.getQuantity() < quantity) {
            throw new ProductVariantException("Insufficient stock for size: " + size);
        }

        ProductVariantStockMovement stockMovement = new ProductVariantStockMovement();
        stockMovement.setProductVariant(variant);
        stockMovement.setQuantity(quantity);
        stockMovement.setType(StockMovementType.DECREASE);
        productVariantStockMovementRepository.save(stockMovement);

        stock.setQuantity(stock.getQuantity() - quantity);
        productVariantStockRepository.save(stock);
        syncToElasticsearch(variant.getProduct());

        return ProductVariantStockDTO.builder()
                .id(stock.getId())
                .quantity(stock.getQuantity())
                .size(stock.getSize())
                .sku(stock.getSku())
                .build();
    }

    private ProductVariantStock findStockBySize(ProductVariant variant,
            com.example.apps.products.enums.ProductSize size) {
        if (variant.getStocks() == null || variant.getStocks().isEmpty()) {
            throw new ProductVariantException("No stock records found for this variant.");
        }

        if (size == null) {
            // If no size specified, return the first one as fallback
            log.warn("No size specified for variant {}. falling back to first available size.", variant.getId());
            return variant.getStocks().get(0);
        }

        return variant.getStocks().stream()
                .filter(s -> s.getSize() != null && s.getSize() == size)
                .findFirst()
                .orElseThrow(() -> new ProductVariantException("Stock not found for size: " + size));
    }

    @Override
    @Transactional
    public ProductVariantStockDTO increaseStock(Long variantId, Long quantity,
            com.example.apps.products.enums.ProductSize size) {
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

        ProductVariantStock stock = findStockBySize(variant, size);
        stock.setQuantity(stock.getQuantity() + quantity);
        productVariantStockRepository.save(stock);
        syncToElasticsearch(variant.getProduct());

        return ProductVariantStockDTO.builder()
                .id(stock.getId())
                .quantity(stock.getQuantity())
                .size(stock.getSize())
                .sku(stock.getSku())
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "cartCheckoutCache", allEntries = true)
    public ProductVariantDTO updateVariant(Long variantId, ProductVariantDTOIU variantDTOIU) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ProductVariantException("Product variant not found"));

        // Manual mapping for safer updates
        variant.setName(variantDTOIU.getName());
        variant.setPrice(variantDTOIU.getPrice());
        variant.setDiscountRatio(variantDTOIU.getDiscountRatio() != null ? variantDTOIU.getDiscountRatio() : 0L);
        variant.setEnable(variantDTOIU.getEnable() != null ? variantDTOIU.getEnable() : variant.getEnable());

        // Calculate discount price based on discount ratio
        if (variant.getDiscountRatio() != null && variant.getDiscountRatio() > 0) {
            java.math.BigDecimal discountPrice = variant.getPrice()
                    .multiply(java.math.BigDecimal.ONE.subtract(
                            java.math.BigDecimal.valueOf(variant.getDiscountRatio())
                                    .divide(java.math.BigDecimal.valueOf(100))));
            variant.setDiscountPrice(discountPrice);
        } else {
            variant.setDiscountPrice(null);
        }

        // Update stocks
        if (variantDTOIU.getStocks() != null) {
            variant.getStocks().clear();
            List<ProductVariantStock> stocksList = variantDTOIU.getStocks().stream().map(stockDTOIU -> {
                ProductVariantStock stockItem = new ProductVariantStock();
                stockItem.setQuantity(stockDTOIU.getQuantity());
                stockItem.setSize(stockDTOIU.getSize());
                stockItem.setSku(stockDTOIU.getSku() != null ? stockDTOIU.getSku()
                        : String.format("%d-%s-%s-%d",
                                variant.getProduct().getId(),
                                variant.getName().replaceAll("\\s+", "-").toUpperCase(),
                                stockDTOIU.getSize().name(),
                                System.currentTimeMillis()));
                stockItem.setProductVariant(variant);
                return stockItem;
            }).collect(java.util.stream.Collectors.toList());
            variant.getStocks().addAll(stocksList);
        }

        // Update color
        if (variantDTOIU.getColor() != null) {
            ProductVariantColor color = variant.getColor();
            color.setName(variantDTOIU.getColor().getName());
            color.setHexCode(variantDTOIU.getColor().getHexCode());
            color.setProductVariant(variant);
        }

        // Update images
        if (variantDTOIU.getImages() != null) {
            variant.getImages().clear();
            List<ProductVariantImage> images = variantDTOIU.getImages().stream().map(imageDTOIU -> {
                ProductVariantImage image = new ProductVariantImage();
                image.setUrl(imageDTOIU.getUrl());
                image.setAlt(imageDTOIU.getAlt());
                image.setProductVariant(variant);
                return image;
            }).toList();
            variant.getImages().addAll(images);
        }

        productVariantRepository.save(variant);
        syncToElasticsearch(variant.getProduct());

        ProductVariantDTO variantDTO = new ProductVariantDTO();
        BeanUtils.copyProperties(variant, variantDTO);
        variantDTO.setStocks(variant.getStocks().stream()
                .<ProductVariantStockDTO>map(stockItem -> ProductVariantStockDTO.builder()
                        .id(stockItem.getId())
                        .quantity(stockItem.getQuantity())
                        .sku(stockItem.getSku())
                        .size(stockItem.getSize())
                        .build())
                .collect(java.util.stream.Collectors.toList()));
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
        Long productId = variant.getProduct().getId();
        productVariantRepository.delete(variant);

        syncToElasticsearch(productId);

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
            syncToElasticsearch(image.getProductVariant().getProduct());

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

        ProductVariant variant = image.getProductVariant();
        Product product = variant.getProduct();

        // Remove from parent collection to avoid re-save by CascadeType.ALL
        if (variant.getImages() != null) {
            variant.getImages().removeIf(img -> img.getId().equals(variantImageId));
        }

        if (!storageService.deleteFile(image.getUrl())) {
            log.warn("Failed to delete physical file: {}. Continuing with database record deletion.", image.getUrl());
        }

        // Explicitly delete/orphan
        productVariantImageRepository.delete(image);
        productVariantRepository.save(variant);

        syncToElasticsearch(product);
        return true;
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
            syncToElasticsearch(variant.getProduct());

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

        return convertVariantToDTO(variant);
    }

    @Override
    public String uploadImage(MultipartFile file) {
        String filename = AppConfiguration.generateUniqueFileName(file);
        String uploadDir = "uploads/product-variant-images";

        if (storageService.uploadFile(file, uploadDir, filename)) {
            return "/" + uploadDir + "/" + filename;
        } else {
            throw new ProductVariantException("Failed to upload image");
        }
    }
}
