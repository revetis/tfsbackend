package com.example.apps.products.mappers;

import org.springframework.stereotype.Component;
import com.example.apps.products.enums.ProductSize;

import com.example.apps.products.documents.ProductDocument;
import com.example.apps.products.documents.ProductVariantDocument;
import com.example.apps.products.documents.SubCategoryDocument;
import com.example.apps.products.entities.Product;
import com.example.apps.products.entities.ProductVariant;
import com.example.apps.products.entities.ProductVariantStock;
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
                .material(entity.getProductMaterial() != null ? entity.getProductMaterial().getName() : null)
                .colors(entity.getVariants().stream()
                        .map(v -> v.getColor() != null ? v.getColor().getName() : null)
                        .filter(c -> c != null)
                        .distinct()
                        .toList())
                .sizes(entity.getVariants().stream()
                        .flatMap(v -> v.getStocks().stream())
                        .map(ProductVariantStock::getSize)
                        .filter(s -> s != null)
                        .distinct()
                        .toList())
                .variants(entity.getVariants().stream().map(this::toVariantDoc).toList())
                .gender(entity.getGender() != null ? entity.getGender().name() : null)
                .careInstructions(entity.getCareInstructions())
                .origin(entity.getOrigin())
                .quality(entity.getQuality())
                .style(entity.getStyle())
                .season(entity.getSeason())
                .mainCategoryId(entity.getSubCategory() != null && entity.getSubCategory().getMainCategory() != null
                        ? entity.getSubCategory().getMainCategory().getId()
                        : null)
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
        String mainImageUrl = null;
        if (variant.getImages() != null && !variant.getImages().isEmpty()) {
            mainImageUrl = variant.getImages().get(0).getUrl();
        }

        long totalStock = variant.getStocks().stream()
                .mapToLong(ProductVariantStock::getQuantity)
                .sum();

        return ProductVariantDocument.builder()
                .id(variant.getId())
                .name(variant.getName())
                .price(variant.getPrice())
                .discountPrice(variant.getDiscountPrice())
                .discountRatio(variant.getDiscountRatio())
                .colorName(variant.getColor() != null ? variant.getColor().getName() : null)
                .stockCount(totalStock)
                .mainImageUrl(mainImageUrl)
                .build();
    }
}