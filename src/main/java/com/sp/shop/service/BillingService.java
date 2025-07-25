package com.sp.shop.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sp.shop.dto.BillRequest;
import com.sp.shop.entity.Bill;
import com.sp.shop.entity.BillItem;
import com.sp.shop.entity.Product;
import com.sp.shop.entity.Shop;
import com.sp.shop.repository.BillRepository;
import com.sp.shop.repository.ProductRepository;
import com.sp.shop.repository.ShopRepository;
import com.sp.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class BillingService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final BillRepository billRepository;

    public Bill createBill(BillRequest request, String username) {
        com.sp.shop.entity.User user = userRepository.findByUsername(username).orElseThrow();
        Shop shop = shopRepository.findByOwner(user).orElseThrow();

        Bill bill = new Bill();
        bill.setCustomerName(request.getCustomerName());
        bill.setCreatedAt(LocalDateTime.now());
        bill.setShop(shop);

        double total = 0;
        List<BillItem> billItems = new ArrayList<>();

        for (BillRequest.CartItem cartItem : request.getItems()) {
            Product product = productRepository.findByBarcode(cartItem.getBarcode())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for " + product.getName());
            }

            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            BillItem item = new BillItem();
            item.setProductName(product.getName());
            item.setBarcode(product.getBarcode());
            item.setQuantity(cartItem.getQuantity());
            item.setPricePerUnit(product.getPrice());
            item.setTotalPrice(product.getPrice() * cartItem.getQuantity());
            item.setBill(bill);

            total += item.getTotalPrice();
            billItems.add(item);
        }

        bill.setTotalAmount(total);
        bill.setItems(billItems);

        return billRepository.save(bill);
    }

    public List<Bill> getAllBillsForUser(String username) {
        com.sp.shop.entity.User user = userRepository.findByUsername(username).orElseThrow();
        Shop shop = shopRepository.findByOwner(user).orElseThrow();
        return billRepository.findByShop(shop);
    }
}