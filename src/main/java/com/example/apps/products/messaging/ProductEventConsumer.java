package com.example.apps.products.messaging;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.products.documents.ProductDocument;
import com.example.apps.products.entities.Product;
import com.example.apps.products.events.ProductEvent;
import com.example.apps.products.repositories.ProductRepository;
import com.example.apps.products.repositories.search.ProductSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventConsumer {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;

    @RabbitListener(queues = "${app.rabbitmq.product.queue}")
    @Transactional // Ensure read is transactional if needed, though mostly read-only here
    public void consumeEvent(ProductEvent event) {
        log.info("Received ProductEvent: {}", event);

        try {
            if (event.getType() == ProductEvent.EventType.DELETE) {
                productSearchRepository.deleteById(event.getProductId());
                log.info("Deleted product from ES: {}", event.getProductId());
                return;
            }

            Product product = productRepository.findById(event.getProductId())
                    .orElse(null);

            if (product == null) {
                log.warn("Product not found in DB for event: {}", event);
                // If it was a CREATE/UPDATE but not found, maybe deleted rapidly?
                // Alternatively, if it's missing, ensure it's removed from ES just in case.
                productSearchRepository.deleteById(event.getProductId());
                return;
            }

            ProductDocument document = mapToDocument(product);
            productSearchRepository.save(document);
            log.info("Indexed product in ES: {}", event.getProductId());

        } catch (Exception e) {
            log.error("Error processing ProductEvent: {}", event, e);
            // Rethrow to trigger RabbitMQ Retry and eventually DLQ
            throw new RuntimeException("Failed to process product event", e);
        }
    }

    private ProductDocument mapToDocument(Product product) {
        // Collect variant info if needed, for now flat list of available colors
        List<String> colors = product.getVariants().stream()
                .filter(v -> v.getColor() != null)
                .map(v -> v.getColor().getName())
                .distinct()
                .collect(Collectors.toList());

        List<String> colorSlugs = product.getVariants().stream()
                .filter(v -> v.getColor() != null)
                .map(v -> v.getColor().getName().toLowerCase().replace(" ", "-")) // Fallback since Color entity has no
                                                                                  // slug
                .distinct()
                .collect(Collectors.toList());

        return ProductDocument.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .slug(product.getSlug())
                .mainPrice(product.getMainPrice())
                .discountRatio(product.getDiscountRatio())
                .isActive(product.getIsActive())
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .brandSlug(product.getBrand() != null ? product.getBrand().getSlug() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .categorySlug(product.getCategory() != null ? product.getCategory().getSlug() : null)
                .colors(colors)
                .colorSlugs(colorSlugs)
                .build();
    }
}
