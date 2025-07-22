package com.sp.shop.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sp.shop.dto.CreateProductRequest;
import com.sp.shop.entity.Product;
import com.sp.shop.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_SHOP_OWNER', 'ROLE_STAFF')")
    public ResponseEntity<Product> addProduct(
            @RequestBody CreateProductRequest request,
            Principal principal) {
        Product product = productService.addProduct(request, principal.getName());
        return ResponseEntity.ok(product);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Product>> getProducts(Principal principal) {
        List<Product> products = productService.getProductsForUser(principal.getName());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/barcode/{barcode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Product> getProductByBarcode(@PathVariable String barcode) {
        return productService.getByBarcode(barcode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ROLE_SHOP_OWNER')")
    public ResponseEntity<Product> updateProduct(
        @PathVariable Long productId,
        @RequestBody UpdateProductRequest request,
        Principal principal) {
        Product updated = productService.updateProduct(productId, request, principal.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ROLE_SHOP_OWNER')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId, Principal principal) {
        productService.deleteProduct(productId, principal.getName());
      return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam("q") String query) {
        return ResponseEntity.ok(productService.searchProducts(query));
    }
}
