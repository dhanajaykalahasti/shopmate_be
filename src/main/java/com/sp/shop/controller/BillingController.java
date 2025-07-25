package com.sp.shop.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sp.shop.dto.BillRequest;
import com.sp.shop.entity.Bill;
import com.sp.shop.service.BillingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_SHOP_OWNER', 'ROLE_STAFF')")
    public ResponseEntity<Bill> createBill(@RequestBody BillRequest request, Principal principal) {
        return ResponseEntity.ok(billingService.createBill(request, principal.getName()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Bill>> getBills(Principal principal) {
        return ResponseEntity.ok(billingService.getAllBillsForUser(principal.getName()));
    }

}
