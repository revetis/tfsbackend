package com.example.apps.products.mappers;

import org.springframework.stereotype.Component;

import com.example.apps.products.documents.ProductDocument;
import com.example.apps.products.documents.ProductVariantDocument;
import com.example.apps.products.documents.SubCategoryDocument;
import com.example.apps.products.entities.Product;
import com.example.apps.products.entities.ProductVariant;
import com.example.apps.products.entities.SubCategory;

@Component
public class ProductMapper {

    public ProductDocument toDocument(Product entity) {
        if (entity == null)
            return null;

        return ProductDocument.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .description(entity.getDescription())
                .enable(entity.getEnable())
                .createdAt(entity.getCreatedAt())
                .subCategory(toSubCategoryDoc(entity.getSubCategory()))
                .variants(entity.getVariants().stream().map(this::toVariantDoc).toList())
                .build();
    }

    private SubCategoryDocument toSubCategoryDoc(SubCategory subCategory) {
        if (subCategory == null)
            return null;
        return SubCategoryDocument.builder()
                .id(subCategory.getId())
                .name(subCategory.getName())
                .mainCategoryName(subCategory.getMainCategory().getName()) // Denormalizasyon efendim
                .build();
    }

    private ProductVariantDocument toVariantDoc(ProductVariant variant) {
        return ProductVariantDocument.builder()
                .id(variant.getId())
                .name(variant.getName())
                .price(variant.getPrice())
                .discountPrice(variant.getDiscountPrice())
                .discountRatio(variant.getDiscountRatio())
                .colorName(variant.getColor() != null ? variant.getColor().getName() : null)
                .stockCount(variant.getStock() != null ? variant.getStock().getQuantity() : 0)
                .build();
    }
}