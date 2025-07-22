package com.sp.shop.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sp.shop.entity.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);  // Find user by username
    Optional<User> findByEmail(String email);  // Find user by email
    
    Page<User> findByUsernameContainingOrEmailContainingOrMobileContaining(
        String username, String email, String mobile, Pageable pageable);
}