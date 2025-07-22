package com.sp.shop.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sp.shop.dto.UserResponseDTO;
import com.sp.shop.entity.User;
import com.sp.shop.security.JwtUtil;
import com.sp.shop.service.UserService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/user")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // Register a new user
    @PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            UserResponseDTO responseDTO = new UserResponseDTO(
            		registeredUser.getId(),
            		registeredUser.getUsername(),
            		registeredUser.getEmail(),
            		registeredUser.getMobile(),
            		(registeredUser.getRoles() != null && !registeredUser.getRoles().isEmpty()) ? registeredUser.getRoles().iterator().next().name() : null,
            		registeredUser.isVerified()
                );
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Login user and generate JWT token
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            
            if (email == null || password == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Email and password are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            String token = userService.loginUser(email, password);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");
            
            if (email == null || code == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Email and code are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            String response = userService.verifyUser(email, code);
            
            // Check the response message to determine the appropriate status code
            if (response.contains("successful")) {
                return ResponseEntity.ok(response);
            } else if (response.contains("already verified")) {
                return ResponseEntity.ok(response);
            } else if (response.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Verification failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            String response = userService.resendVerificationCode(email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to resend verification code: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7));  // Extract username from token
        User user = userService.getUserProfile(username);

        if (user != null) {
            return ResponseEntity.ok(user);  // If user is found, return 200 with user data
        } else {
            return ResponseEntity.notFound().build();  // If not found, return 404
        }
    }
    
    @PutMapping("/update")
    public ResponseEntity<User> updateUserProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody User updatedUser
    ) {
        String username = jwtUtil.extractUsername(token.substring(7));  // Extract username from token
        User updated = userService.updateUser(username, updatedUser);

        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUserById(
            @PathVariable Long id,
            @RequestBody User updatedUser
    ) {
        try {
            User updated = userService.updateUserById(id, updatedUser);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to update user: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Delete user account
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUserProfile(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7));  // Extract username from token
        boolean deleted = userService.deleteUser(username);

        if (deleted) {
            return ResponseEntity.ok("User deleted successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable Long id) {
        boolean deleted = userService.deleteUserById(id);

        if (deleted) {
            return ResponseEntity.ok("User with ID " + id + " deleted successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        
        List<org.springframework.data.domain.Sort.Order> orders = new ArrayList<>();
        
        for (String sortParam : sort) {
            String[] sortParts = sortParam.split(",");
            if (sortParts.length == 2) {
                String property = sortParts[0];
                String direction = sortParts[1];
                
                // Validate sort property
                if (!isValidSortProperty(property)) {
                    Map<String, String> response = new HashMap<>();
                    response.put("error", "Invalid sort property: " + property);
                    return ResponseEntity.badRequest().body(response);
                }
                
                org.springframework.data.domain.Sort.Direction sortDirection = 
                    "desc".equalsIgnoreCase(direction) ? 
                    org.springframework.data.domain.Sort.Direction.DESC : 
                    org.springframework.data.domain.Sort.Direction.ASC;
                
                orders.add(new org.springframework.data.domain.Sort.Order(sortDirection, property));
            }
        }
        
        // If no valid sort orders were added, add default sort by id
        if (orders.isEmpty()) {
            orders.add(new org.springframework.data.domain.Sort.Order(
                org.springframework.data.domain.Sort.Direction.ASC, "id"));
        }
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size, 
                org.springframework.data.domain.Sort.by(orders));
            
        org.springframework.data.domain.Page<User> usersPage = userService.getAllUsers(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", usersPage.getContent());
        response.put("currentPage", usersPage.getNumber());
        response.put("totalItems", usersPage.getTotalElements());
        response.put("totalPages", usersPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(
            @RequestParam String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        
        List<org.springframework.data.domain.Sort.Order> orders = new ArrayList<>();
        
        for (String sortParam : sort) {
            String[] sortParts = sortParam.split(",");
            if (sortParts.length == 2) {
                String property = sortParts[0];
                String direction = sortParts[1];
                
                // Validate sort property
                if (!isValidSortProperty(property)) {
                    Map<String, String> response = new HashMap<>();
                    response.put("error", "Invalid sort property: " + property);
                    return ResponseEntity.badRequest().body(response);
                }
                
                org.springframework.data.domain.Sort.Direction sortDirection = 
                    "desc".equalsIgnoreCase(direction) ? 
                    org.springframework.data.domain.Sort.Direction.DESC : 
                    org.springframework.data.domain.Sort.Direction.ASC;
                
                orders.add(new org.springframework.data.domain.Sort.Order(sortDirection, property));
            }
        }
        
        // If no valid sort orders were added, add default sort by id
        if (orders.isEmpty()) {
            orders.add(new org.springframework.data.domain.Sort.Order(
                org.springframework.data.domain.Sort.Direction.ASC, "id"));
        }
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size, 
                org.springframework.data.domain.Sort.by(orders));
            
        org.springframework.data.domain.Page<User> usersPage = userService.searchUsers(search, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", usersPage.getContent());
        response.put("currentPage", usersPage.getNumber());
        response.put("totalItems", usersPage.getTotalElements());
        response.put("totalPages", usersPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    private boolean isValidSortProperty(String property) {
        // List of valid sort properties
        Set<String> validProperties = new HashSet<>(Arrays.asList(
            "id", "username", "email", "mobile", "roles", "verified"
        ));
        return validProperties.contains(property);
    }
}