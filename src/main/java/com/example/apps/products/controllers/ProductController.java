package com.example.apps.products.controllers;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.apps.products.enums.ProductSize;

import com.example.apps.products.dtos.ProductDTO;
import com.example.apps.products.dtos.ProductDTOIU;
import com.example.apps.products.dtos.ProductMaterialDTO;
import com.example.apps.products.dtos.ProductMaterialDTOIU;
import com.example.apps.products.dtos.ProductVariantDTO;
import com.example.apps.products.dtos.ProductVariantDTOIU;
import com.example.apps.products.services.IProductService;
import com.example.tfs.maindto.ApiErrorTemplate;
import com.example.tfs.maindto.ApiTemplate;

@RestController
@Validated
@RequestMapping("/rest/api/admin/products")
public class ProductController {

        @Autowired
        private IProductService productService;

        @PostMapping("/upload-image")
        public ResponseEntity<?> uploadImage(
                        @RequestParam("file") MultipartFile file) {

                if (file.isEmpty()) {
                        return ResponseEntity.badRequest()
                                        .body(ApiTemplate.apiTemplateGenerator(
                                                        false,
                                                        HttpStatus.BAD_REQUEST.value(),
                                                        "/rest/api/admin/products/upload-image",
                                                        "File is empty",
                                                        null));
                }

                String url = productService.uploadImage(file);

                if (url == null) {
                        return ResponseEntity.internalServerError()
                                        .body(ApiTemplate.apiTemplateGenerator(
                                                        false,
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                        "/rest/api/admin/products/upload-image",
                                                        "Failed to upload image",
                                                        null));
                }

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products/upload-image",
                                                null,
                                                url));
        }

        @PostMapping("/create")
        public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTOIU request) {
                ProductDTO response = productService.createProduct(request);

                if (response == null) {
                        return ResponseEntity.internalServerError()
                                        .body(ApiErrorTemplate.apiErrorTemplateGenerator(
                                                        false,
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                        "/rest/api/admin/products/create",
                                                        "Error creating product"));
                }

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.CREATED.value(),
                                                "/rest/api/admin/products/create",
                                                null,
                                                response));
        }

        @PutMapping("/update")
        public ResponseEntity<?> updateProduct(
                        @RequestParam Long id,
                        @Valid @RequestBody ProductDTOIU request) {

                ProductDTO response = productService.updateProduct(id, request);

                if (response == null) {
                        return ResponseEntity.internalServerError()
                                        .body(ApiErrorTemplate.apiErrorTemplateGenerator(
                                                        false,
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                        "/rest/api/admin/products/update",
                                                        "Error updating product"));
                }

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products/update",
                                                null,
                                                response));
        }

        @PostMapping("/{productId}/variants")
        public ResponseEntity<?> addVariant(
                        @PathVariable Long productId,
                        @Valid @RequestBody ProductVariantDTOIU request) {

                ProductVariantDTO response = productService.addVariant(productId, request);

                if (response == null) {
                        return ResponseEntity.internalServerError()
                                        .body(ApiErrorTemplate.apiErrorTemplateGenerator(
                                                        false,
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                        "/rest/api/admin/products/" + productId + "/variants",
                                                        "Error adding variant"));
                }

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.CREATED.value(),
                                                "/rest/api/admin/products/" + productId + "/variants",
                                                null,
                                                response));
        }

        @GetMapping("/variants/{variantId}")
        public ResponseEntity<?> getVariantById(@PathVariable Long variantId) {
                ProductVariantDTO response = productService.getVariantById(variantId);

                if (response == null) {
                        return ResponseEntity.notFound().build();
                }

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products/variants/" + variantId,
                                                null,
                                                response));
        }

        @PutMapping("/variants/{variantId}")
        public ResponseEntity<?> updateVariant(
                        @PathVariable Long variantId,
                        @Valid @RequestBody ProductVariantDTOIU request) {

                ProductVariantDTO response = productService.updateVariant(variantId, request);

                if (response == null) {
                        return ResponseEntity.internalServerError()
                                        .body(ApiErrorTemplate.apiErrorTemplateGenerator(
                                                        false,
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                        "/rest/api/admin/products/variants/" + variantId,
                                                        "Error updating variant"));
                }

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products/variants/" + variantId,
                                                null,
                                                response));
        }

        @DeleteMapping("/variants/{variantId}")
        public ResponseEntity<?> deleteVariant(@PathVariable Long variantId) {
                Boolean response = productService.deleteVariant(variantId);

                if (!response) {
                        return ResponseEntity.internalServerError()
                                        .body(ApiErrorTemplate.apiErrorTemplateGenerator(
                                                        false,
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                        "/rest/api/admin/products/variants/" + variantId,
                                                        "Error deleting variant"));
                }

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products/variants/" + variantId,
                                                null,
                                                "Variant deleted successfully"));
        }

        @DeleteMapping("/{productId}")
        public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
                Boolean response = productService.deleteProduct(productId);

                if (!response) {
                        return ResponseEntity.internalServerError()
                                        .body(ApiErrorTemplate.apiErrorTemplateGenerator(
                                                        false,
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                        "/rest/api/admin/products/" + productId,
                                                        "Error deleting product"));
                }

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products/" + productId,
                                                null,
                                                "Product deleted successfully"));
        }

        @PostMapping("/variants/{variantId}/images")
        public ResponseEntity<?> addVariantImage(
                        @PathVariable Long variantId,
                        @RequestParam("file") MultipartFile file,
                        @RequestParam(required = false) String alt) {

                if (file.isEmpty()) {
                        return ResponseEntity.badRequest()
                                        .body(ApiTemplate.apiTemplateGenerator(
                                                        false,
                                                        400,
                                                        "/rest/api/admin/products/variants/" + variantId + "/images",
                                                        "File is empty",
                                                        null));
                }

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.CREATED.value(),
                                                "/rest/api/admin/products/variants/" + variantId + "/images",
                                                null,
                                                productService.addVariantImage(file, variantId, alt)));
        }

        @PutMapping("/variants/images/{variantImageId}")
        public ResponseEntity<?> updateVariantImage(
                        @PathVariable Long variantImageId,
                        @RequestParam("file") MultipartFile file,
                        @RequestParam(required = false) String alt) {

                if (file.isEmpty()) {
                        return ResponseEntity.badRequest()
                                        .body(ApiTemplate.apiTemplateGenerator(
                                                        false,
                                                        HttpStatus.BAD_REQUEST.value(),
                                                        "/rest/api/admin/products/variants/images/" + variantImageId,
                                                        "File is empty",
                                                        null));
                }

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products/variants/images/" + variantImageId,
                                                null,
                                                productService.updateVariantImage(file, variantImageId, alt)));
        }

        @DeleteMapping("/variants/images/{variantImageId}")
        public ResponseEntity<?> deleteVariantImage(@PathVariable Long variantImageId) {
                Boolean response = productService.deleteVariantImage(variantImageId);

                if (!response) {
                        return ResponseEntity.internalServerError()
                                        .body(ApiErrorTemplate.apiErrorTemplateGenerator(
                                                        false,
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                        "/rest/api/admin/products/variants/images/" + variantImageId,
                                                        "Error deleting variant image"));
                }

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products/variants/images/" + variantImageId,
                                                null,
                                                "Variant image deleted successfully"));
        }

        @PutMapping("/variants/{variantId}/stock/decrease")
        public ResponseEntity<?> decreaseStock(
                        @PathVariable Long variantId,
                        @RequestParam Long quantity,
                        @RequestParam(required = false) ProductSize size) {

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products/variants/" + variantId + "/stock/decrease",
                                                null,
                                                productService.decreaseStock(variantId, quantity, size)));
        }

        @PutMapping("/variants/{variantId}/stock/increase")
        public ResponseEntity<?> increaseStock(
                        @PathVariable Long variantId,
                        @RequestParam Long quantity,
                        @RequestParam(required = false) ProductSize size) {

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products/variants/" + variantId + "/stock/increase",
                                                null,
                                                productService.increaseStock(variantId, quantity, size)));
        }

        @GetMapping("/{productId}")
        public ResponseEntity<?> getProductById(@PathVariable Long productId) {
                ProductDTO response = productService.getProductById(productId);

                if (response == null) {
                        return ResponseEntity.internalServerError()
                                        .body(ApiErrorTemplate.apiErrorTemplateGenerator(
                                                        false,
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                        "/rest/api/admin/products/" + productId,
                                                        "Error retrieving product"));
                }

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products/" + productId,
                                                null,
                                                response));
        }

        @GetMapping
        public ResponseEntity<?> getAllProducts(
                        @RequestParam int page,
                        @RequestParam int size) {

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products",
                                                null,
                                                productService.getAllProducts(page, size)));
        }

        @GetMapping("/variants")
        public ResponseEntity<?> getAllVariants(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products/variants",
                                                null,
                                                productService.getAllVariants(page, size)));
        }

        @PostMapping("/variants")
        public ResponseEntity<?> createVariant(@Valid @RequestBody ProductVariantDTOIU request) {
                ProductVariantDTO response = productService.createVariant(request);

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.CREATED.value(),
                                                "/rest/api/admin/products/variants",
                                                null,
                                                response));
        }

        @GetMapping("/variants/{productVariantId}/discount-price")

        public ResponseEntity<?> calculateDiscountPrice(
                        @PathVariable Long productVariantId) {

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products/variants/" + productVariantId
                                                                + "/discount-price",
                                                null,
                                                productService.calculateDiscountPrice(productVariantId)));
        }

        @PostMapping("/materials")
        public ResponseEntity<?> createProductMaterial(@Valid @RequestBody ProductMaterialDTOIU request) {
                ProductMaterialDTO response = productService.createProductMaterial(request);

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.CREATED.value(),
                                                "/rest/api/admin/products/materials",
                                                null,
                                                response));
        }

        @PutMapping("/materials/{id}")
        public ResponseEntity<?> updateProductMaterial(
                        @PathVariable Long id,
                        @Valid @RequestBody ProductMaterialDTOIU request) {

                ProductMaterialDTO response = productService.updateProductMaterial(id, request);

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products/materials/" + id,
                                                null,
                                                response));
        }

        @DeleteMapping("/materials/{id}")
        public ResponseEntity<?> deleteProductMaterial(@PathVariable Long id) {
                Boolean response = productService.deleteProductMaterial(id);

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products/materials/" + id,
                                                null,
                                                response));
        }

        @GetMapping("/materials/{id}")
        public ResponseEntity<?> getProductMaterialById(@PathVariable Long id) {
                ProductMaterialDTO response = productService.getProductMaterialById(id);

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products/materials/" + id,
                                                null,
                                                response));
        }

        @GetMapping("/materials")
        public ResponseEntity<?> getAllProductMaterials(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/products/materials",
                                                null,
                                                productService.getAllProductMaterials(page, size)));
        }
}
