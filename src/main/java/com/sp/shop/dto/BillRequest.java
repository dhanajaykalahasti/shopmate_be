package com.sp.shop.dto;

import java.util.List;

import lombok.Data;

@Data
public class BillRequest {
    private String customerName;
    private List<CartItem> items;

    @Data
    public static class CartItem {
        private String barcode;
        private int quantity;
    }
}
