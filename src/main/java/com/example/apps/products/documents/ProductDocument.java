package com.example.apps.products.documents;

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
@Document(indexName = "products") // İndeks adını burada belirliyoruz efendim
public class ProductDocument {

    @Id
    private String id; // ES için String id daha esnektir, JPA id'sini buraya map ederiz

    @Field(type = FieldType.Text, analyzer = "turkish") // Türkçe arama desteği için
    private String name;

    @Field(type = FieldType.Text, analyzer = "turkish")
    private String description;

    @Field(type = FieldType.Long)
    private Long mainCategoryId;

    @Field(type = FieldType.Keyword)
    private String material;

    // ManyToOne ilişkiyi düzleştiriyoruz veya bir alt obje olarak tutuyoruz
    @Field(type = FieldType.Object)
    private SubCategoryDocument subCategory;

    // OneToMany ilişkiyi 'Nested' olarak tutmak, alt veriler içinde doğru arama
    // yapmanızı sağlar
    @Field(type = FieldType.Nested)
    private List<ProductVariantDocument> variants;

    @Field(type = FieldType.Boolean)
    private Boolean enable;
    @Field(type = FieldType.Keyword)
    private List<String> colors; // ["Kırmızı", "Mavi"]
    @Field(type = FieldType.Keyword)
    private List<String> sizes; // ["S", "M", "L"]

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updatedAt;
}