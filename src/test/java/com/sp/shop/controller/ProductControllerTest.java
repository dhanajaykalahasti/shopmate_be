package com.sp.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp.shop.dto.CreateProductRequest;
import com.sp.shop.entity.Product;
import com.sp.shop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private com.sp.shop.security.JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Product product;
    private CreateProductRequest createProductRequest;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setBarcode("123456");
        // set other fields as needed

        createProductRequest = new CreateProductRequest();
        createProductRequest.setName("Test Product");
        createProductRequest.setBarcode("123456");
        // set other fields as needed
    }

    @Test
    @WithMockUser(roles = {"SHOP_OWNER"})
    void addProduct_shouldReturnProduct() throws Exception {
        Mockito.when(productService.addProduct(any(CreateProductRequest.class), anyString()))
                .thenReturn(product);

        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createProductRequest))
                        .principal(() -> "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.barcode").value("123456"));
    }

    @Test
    @WithMockUser
    void getProducts_shouldReturnProductList() throws Exception {
        Mockito.when(productService.getProductsForUser(anyString()))
                .thenReturn(List.of(product));

        mockMvc.perform(get("/api/products")
                        .principal(() -> "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    @WithMockUser
    void getProductByBarcode_shouldReturnProduct() throws Exception {
        Mockito.when(productService.getByBarcode(eq("123456")))
                .thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/products/barcode/123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.barcode").value("123456"));
    }

    @Test
    @WithMockUser
    void getProductByBarcode_shouldReturnNotFound() throws Exception {
        Mockito.when(productService.getByBarcode(eq("notfound")))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/barcode/notfound"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"SHOP_OWNER"})
    void updateProduct_shouldReturnUpdatedProduct() throws Exception {
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Product");
        updatedProduct.setBarcode("123456");

        // You may need to create a mock UpdateProductRequest class or use a Map
        // Here, assuming UpdateProductRequest has a setName method
        com.sp.shop.controller.UpdateProductRequest updateRequest = new com.sp.shop.controller.UpdateProductRequest();
        updateRequest.setName("Updated Product");

        Mockito.when(productService.updateProduct(eq(1L), any(), anyString()))
                .thenReturn(updatedProduct);

        mockMvc.perform(put("/api/products/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .principal(() -> "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"));
    }

    @Test
    @WithMockUser(roles = {"SHOP_OWNER"})
    void deleteProduct_shouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(productService).deleteProduct(eq(1L), anyString());

        mockMvc.perform(delete("/api/products/1")
                        .with(csrf())
                        .principal(() -> "testuser"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void searchProducts_shouldReturnProductList() throws Exception {
        Mockito.when(productService.searchProducts(eq("Test")))
                .thenReturn(List.of(product));

        mockMvc.perform(get("/api/products/search")
                        .param("q", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }
}
