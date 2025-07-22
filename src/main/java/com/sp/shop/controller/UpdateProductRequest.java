package com.sp.shop.controller;

import lombok.Data;

@Data
public class UpdateProductRequest {
    private String name;
    private String brand;
    private String category;
    private double price;
    private int quantity;
    private String imageUrl;
}