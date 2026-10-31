package com.stockpilot.backend.product;

import com.stockpilot.backend.product.dto.ProductRequest;
import com.stockpilot.backend.product.dto.ProductResponse;
import com.stockpilot.backend.product.dto.ProductUpdateRequest;
import com.stockpilot.backend.user.AppUser;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) { this.productService = productService; }

    @GetMapping
    public List<ProductResponse> list(
            @AuthenticationPrincipal AppUser actor,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) ProductStatus status) {
        return productService.list(actor, search, category, status);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable UUID id, @AuthenticationPrincipal AppUser actor) {
        return productService.get(id, actor);
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductRequest request, @AuthenticationPrincipal AppUser actor) {
        ProductResponse created = productService.create(request, actor);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ProductResponse update(@PathVariable UUID id,
                                  @Valid @RequestBody ProductUpdateRequest request,
                                  @AuthenticationPrincipal AppUser actor) {
        return productService.update(id, request, actor);
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('OWNER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(@PathVariable UUID id, @AuthenticationPrincipal AppUser actor) {
        productService.archive(id, actor);
    }
}
