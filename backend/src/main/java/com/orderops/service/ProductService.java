package com.orderops.service;

import com.orderops.dto.product.ProductCreateRequest;
import com.orderops.dto.product.ProductResponse;
import com.orderops.entity.Product;
import com.orderops.exception.ConflictException;
import com.orderops.exception.NotFoundException;
import com.orderops.repository.ProductRepository;
import com.orderops.dto.common.PageResponse;
import com.orderops.dto.product.ProductUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse create(ProductCreateRequest req) {
        if (productRepository.existsBySku(req.getSku())) {
            throw new ConflictException("SKU already exists");
        }

        Product p = new Product();
        p.setSku(req.getSku().trim());
        p.setName(req.getName().trim());
        p.setPriceCents(req.getPriceCents());
        p.setStockQty(req.getStockQty());
        p.setLowStockThreshold(req.getLowStockThreshold());

        Product saved = productRepository.save(p);
        return toResponse(saved);
    }

    public ProductResponse getById(UUID id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return toResponse(p);
    }

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .sku(p.getSku())
                .name(p.getName())
                .priceCents(p.getPriceCents())
                .stockQty(p.getStockQty())
                .lowStockThreshold(p.getLowStockThreshold())
                .build();
    }

    public PageResponse<ProductResponse> list(Pageable pageable) {
    Page<Product> page = productRepository.findAll(pageable);
    return PageResponse.<ProductResponse>builder()
            .content(page.getContent().stream().map(this::toResponse).toList())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .build();
}

@Transactional
public ProductResponse update(UUID id, ProductUpdateRequest req) {
    Product p = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found"));

    String sku = req.getSku().trim();
    if (productRepository.existsBySkuAndIdNot(sku, id)) {
        throw new ConflictException("SKU already exists");
    }

    p.setSku(sku);
    p.setName(req.getName().trim());
    p.setPriceCents(req.getPriceCents());
    p.setStockQty(req.getStockQty());
    p.setLowStockThreshold(req.getLowStockThreshold());

    Product saved = productRepository.save(p);
    return toResponse(saved);
}

@Transactional
public void delete(UUID id) {
    try {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        productRepository.delete(p);
    } catch (DataIntegrityViolationException ex) {
        // If referenced by order_items FK
        throw new ConflictException("Product cannot be deleted because it is referenced by orders");
    }
}
}