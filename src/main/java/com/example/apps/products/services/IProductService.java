package com.example.apps.products.services;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.example.apps.products.dtos.ProductDTO;
import com.example.apps.products.dtos.ProductDTOIU;
import com.example.apps.products.dtos.ProductMaterialDTO;
import com.example.apps.products.dtos.ProductMaterialDTOIU;
import com.example.apps.products.dtos.ProductVariantDTO;
import com.example.apps.products.dtos.ProductVariantDTOIU;
import com.example.apps.products.dtos.ProductVariantImageDTO;
import com.example.apps.products.dtos.ProductVariantStockDTO;

public interface IProductService {
    ProductDTO createProduct(ProductDTOIU productDTOIU);

    ProductDTO updateProduct(Long id, ProductDTOIU productDTOIU);

    Boolean deleteProduct(Long id);

    ProductDTO getProductById(Long id);

    Page<ProductDTO> getAllProducts(int page, int size);

    ProductVariantDTO addVariant(Long productId, ProductVariantDTOIU variantDTOIU);

    ProductVariantDTO updateVariant(Long variantId, ProductVariantDTOIU variantDTOIU);

    Boolean deleteVariant(Long variantId);

    ProductVariantDTO getVariantById(Long variantId);

    Long calculateDiscountPrice(Long productVariantId);

    ProductVariantImageDTO updateVariantImage(MultipartFile file, Long variantImageId, String alt);

    Boolean deleteVariantImage(Long variantImageId);

    ProductVariantImageDTO addVariantImage(MultipartFile file, Long variantId, String alt);

    ProductVariantStockDTO decreaseStock(Long variantId, Long quantity);

    ProductVariantStockDTO increaseStock(Long variantId, Long quantity);

    ProductMaterialDTO createProductMaterial(ProductMaterialDTOIU productMaterialDTO);

    ProductMaterialDTO updateProductMaterial(Long id, ProductMaterialDTOIU productMaterialDTOIU);

    Boolean deleteProductMaterial(Long id);

    ProductMaterialDTO getProductMaterialById(Long id);

    Page<ProductMaterialDTO> getAllProductMaterials(int page, int size);
}
