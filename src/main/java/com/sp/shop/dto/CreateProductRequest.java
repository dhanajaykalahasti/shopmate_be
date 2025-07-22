package com.sp.shop.dto;

import lombok.Data;

@Data
public class CreateProductRequest {
    private String name;
    private String barcode;
    private String brand;
    private String category;
    private double price;
    private int quantity;
    private String imageUrl;
}
