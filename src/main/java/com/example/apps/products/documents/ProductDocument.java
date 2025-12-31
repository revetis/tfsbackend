package com.example.apps.products.documents;

import com.example.apps.products.enums.ProductSize;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "products")
public class ProductDocument {

    @Id
    private String id;

    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "turkish"), otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword)
    })
    private String name;

    @Field(type = FieldType.Text, analyzer = "turkish")
    private String description;

    @Field(type = FieldType.Long)
    private Long mainCategoryId;

    @Field(type = FieldType.Keyword)
    private String material;

    @Field(type = FieldType.Object)
    private SubCategoryDocument subCategory;

    @Field(type = FieldType.Nested)
    private List<ProductVariantDocument> variants;

    @Field(type = FieldType.Text, analyzer = "turkish")
    private String careInstructions;
    @Field(type = FieldType.Keyword)
    private String origin;
    @Field(type = FieldType.Keyword)
    private String quality;
    @Field(type = FieldType.Keyword)
    private String style;
    @Field(type = FieldType.Keyword)
    private String season;

    @Field(type = FieldType.Boolean)
    private Boolean enable;
    @Field(type = FieldType.Keyword)
    private List<String> colors;
    @Field(type = FieldType.Keyword)
    private List<ProductSize> sizes;

    @Field(type = FieldType.Keyword)
    private String gender;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updatedAt;
}