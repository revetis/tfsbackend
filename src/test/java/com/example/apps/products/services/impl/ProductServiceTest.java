package com.example.apps.products.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.example.apps.products.dtos.ProductDTO;
import com.example.apps.products.dtos.ProductDTOIU;
import com.example.apps.products.dtos.ProductVariantDTO;
import com.example.apps.products.dtos.ProductVariantDTOIU;
import com.example.apps.products.dtos.ProductVariantStockDTOIU;
import com.example.apps.products.dtos.ProductVariantColorDTOIU;
import com.example.apps.products.entities.Product;
import com.example.apps.products.entities.ProductMaterial;
import com.example.apps.products.entities.ProductVariant;
import com.example.apps.products.entities.ProductVariantColor;
import com.example.apps.products.entities.ProductVariantStock;
import com.example.apps.products.entities.SubCategory;
import com.example.apps.products.enums.Gender;
import com.example.apps.products.exceptions.ProductException;
import com.example.apps.products.repositories.ProductMaterialRepository;
import com.example.apps.products.repositories.ProductRepository;
import com.example.apps.products.repositories.ProductVariantRepository;
import com.example.apps.products.repositories.ProductVariantStockMovementRepository;
import com.example.apps.products.repositories.SubCategoryRepository;
import com.example.apps.products.repositories.ProductVariantStockRepository;
import com.example.apps.products.enums.ProductSize;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductMaterialRepository productMaterialRepository;

    @Mock
    private SubCategoryRepository subCategoryRepository;

    @Mock
    private ProductVariantStockMovementRepository productVariantStockMovementRepository;

    @Mock
    private ProductVariantStockRepository productVariantStockRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDTOIU productDTOIU;
    private ProductMaterial material;
    private SubCategory subCategory;

    @BeforeEach
    void setUp() {
        material = new ProductMaterial();
        material.setId(1L);
        material.setName("Cotton");

        subCategory = new SubCategory();
        subCategory.setId(1L);
        subCategory.setName("T-Shirts");

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setBrand("Test Brand");
        product.setGender(Gender.MAN);
        product.setProductMaterial(material);
        product.setSubCategory(subCategory);
        product.setTaxRatio(18.0);
        product.setEnable(true);

        productDTOIU = new ProductDTOIU();
        productDTOIU.setName("Test Product");
        productDTOIU.setBrand("Test Brand");
        productDTOIU.setGender("MAN");
        productDTOIU.setMaterialId(1L);
        productDTOIU.setSubCategoryId(1L);
        productDTOIU.setTaxRatio(18.0);
        productDTOIU.setEnable(true);
    }

    @Test
    void createProduct_Success() {
        when(subCategoryRepository.findById(anyLong())).thenReturn(Optional.of(subCategory));
        when(productMaterialRepository.findById(anyLong())).thenReturn(Optional.of(material));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO result = productService.createProduct(productDTOIU);

        assertNotNull(result);
        assertEquals(productDTOIU.getName(), result.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getProductById_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductException.class, () -> productService.getProductById(1L));
    }

    @Test
    void addVariant_Success() {
        ProductVariantDTOIU variantDTOIU = new ProductVariantDTOIU();
        variantDTOIU.setName("White XL");
        variantDTOIU.setPrice(new BigDecimal("100.00"));
        variantDTOIU.setDiscountRatio(0L);
        variantDTOIU.setEnable(true);

        ProductVariantStockDTOIU stockDTOIU = new ProductVariantStockDTOIU();
        stockDTOIU.setQuantity(50L);
        stockDTOIU.setSize(ProductSize.XL);
        variantDTOIU.setStocks(List.of(stockDTOIU));

        ProductVariantColorDTOIU colorDTOIU = new ProductVariantColorDTOIU();
        colorDTOIU.setName("White");
        colorDTOIU.setHexCode("#FFFFFF");
        variantDTOIU.setColor(colorDTOIU);
        variantDTOIU.setImages(new ArrayList<>());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productVariantRepository.save(any(ProductVariant.class))).thenAnswer(invocation -> {
            ProductVariant v = invocation.getArgument(0);
            v.setId(1L);
            if (v.getStocks() != null && !v.getStocks().isEmpty()) {
                v.getStocks().get(0).setId(1L);
            }
            v.getColor().setId(1L);
            return v;
        });

        ProductVariantDTO result = productService.addVariant(1L, variantDTOIU);

        assertNotNull(result);
        assertEquals("White XL", result.getName());
        verify(productVariantRepository).save(any(ProductVariant.class));
        verify(productVariantStockMovementRepository).save(any());
    }
}
