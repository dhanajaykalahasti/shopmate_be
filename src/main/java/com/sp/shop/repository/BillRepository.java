package com.sp.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sp.shop.entity.Bill;
import com.sp.shop.entity.Shop;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByShop(Shop shop);
}