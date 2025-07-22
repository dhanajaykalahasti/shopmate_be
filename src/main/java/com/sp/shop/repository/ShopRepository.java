package com.sp.shop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sp.shop.entity.User;
import com.sp.shop.entity.Shop;


public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByOwner(User owner);
}

