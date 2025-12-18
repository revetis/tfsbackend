package com.example.apps.products.documents;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantDocument {

    @Field(type = FieldType.Long)
    private Long id;

    @Field(type = FieldType.Text, analyzer = "turkish")
    private String name;

    @Field(type = FieldType.Double) // ES'de BigDecimal genellikle Double/ScaledFloat tutulur
    private BigDecimal price;

    @Field(type = FieldType.Double)
    private BigDecimal discountPrice;

    @Field(type = FieldType.Long)
    private Long discountRatio;

    // Mevcut stok miktarını tek bir alan olarak tutmak arama hızı için kritiktir
    // efendim
    @Field(type = FieldType.Integer)
    private Long stockCount;

    // Renk bilgisini direkt buraya denormalize ediyoruz
    @Field(type = FieldType.Keyword)
    private String colorName;

    @Field(type = FieldType.Keyword)
    private String colorCode;

    // Sadece ana görselin linkini tutmak yeterli olacaktır
    @Field(type = FieldType.Keyword, index = false) // index=false: arama yapılmaz, sadece gösterilir
    private String mainImageUrl;
}