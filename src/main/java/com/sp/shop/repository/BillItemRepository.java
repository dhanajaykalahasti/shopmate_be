package com.sp.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sp.shop.entity.Bill;
import com.sp.shop.entity.BillItem;

public interface BillItemRepository extends JpaRepository<BillItem, Long> {
    List<BillItem> findByBill(Bill bill);
}
