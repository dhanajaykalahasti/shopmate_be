package com.sp.shop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sp.shop.controller.UpdateProductRequest;
import com.sp.shop.dto.CreateProductRequest;
import com.sp.shop.entity.Product;
import com.sp.shop.entity.Shop;
import com.sp.shop.repository.ProductRepository;
import com.sp.shop.repository.ShopRepository;
import com.sp.shop.repository.UserRepository;
import com.sp.shop.entity.User;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    public Product addProduct(CreateProductRequest request, String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Shop shop = shopRepository.findByOwner(user).orElseThrow();

        Product product = new Product();
        product.setName(request.getName());
        product.setBarcode(request.getBarcode());
        product.setBrand(request.getBrand());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setShop(shop);

        return productRepository.save(product);
    }

    public List<Product> getProductsForUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Shop shop = shopRepository.findByOwner(user).orElseThrow();
        return productRepository.findByShop(shop);
    }

    public Optional<Product> getByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode);
    }

    public Product updateProduct(Long productId, UpdateProductRequest request, String username) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        User user = userRepository.findByUsername(username).orElseThrow();
        
        // Only shop owner who owns the product's shop can update
        if (!product.getShop().getOwner().getUsername().equals(user.getUsername())) {
            throw new AccessDeniedException("Unauthorized to edit this product");
        }
    
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setImageUrl(request.getImageUrl());
    
        return productRepository.save(product);
    }

    public void deleteProduct(Long productId, String username) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        User user = userRepository.findByUsername(username).orElseThrow();
    
        if (!product.getShop().getOwner().getUsername().equals(user.getUsername())) {
            throw new AccessDeniedException("Unauthorized to delete this product");
        }
    
        productRepository.delete(product);
    }

    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCaseOrBrandContainingIgnoreCase(query, query);
    }

    
    
}
