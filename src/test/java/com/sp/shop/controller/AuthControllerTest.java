package com.sp.shop.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp.shop.controller.AuthController;
import com.sp.shop.dto.UserResponseDTO;
import com.sp.shop.entity.Role;
import com.sp.shop.entity.User;
import com.sp.shop.security.JwtUtil;
import com.sp.shop.service.UserService;

@WebMvcTest(AuthController.class)
@Import({JwtUtil.class}) // Import required configurations
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    private ObjectMapper objectMapper;
    private User testUser;
    private UserResponseDTO testUserResponseDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setMobile("1234567890");
        testUser.setRoles(Set.of(Role.ROLE_STAFF));
        testUser.setVerified(true);

        // Setup test user response DTO
        testUserResponseDTO = new UserResponseDTO(
            testUser.getId(),
            testUser.getUsername(),
            testUser.getEmail(),
            testUser.getMobile(),
            "ROLE_STAFF",
            testUser.isVerified()
        );

        // Setup mockMvc with proper configuration
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build();
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        when(userService.registerUser(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/user/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.mobile").value("1234567890"))
                .andExpect(jsonPath("$.role").value("ROLE_STAFF"))
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    void testLoginUser_Success() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "test@example.com");
        loginRequest.put("password", "password123");

        when(userService.loginUser(anyString(), anyString())).thenReturn("test-token");

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"));
    }

    @Test
    void testGetAllUsers_Success() throws Exception {
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

        when(userService.getAllUsers(any())).thenReturn(userPage);

        mockMvc.perform(get("/api/user/all")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].id").value(1))
                .andExpect(jsonPath("$.users[0].username").value("testuser"))
                .andExpect(jsonPath("$.users[0].email").value("test@example.com"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void testSearchUsers_Success() throws Exception {
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

        when(userService.searchUsers(anyString(), any())).thenReturn(userPage);

        mockMvc.perform(get("/api/user/search")
                .param("search", "test")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "username,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].username").value("testuser"))
                .andExpect(jsonPath("$.users[0].email").value("test@example.com"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    // @Test
    // void testSearchUsers_InvalidSortProperty() throws Exception {
    //     mockMvc.perform(get("/user/auth/search")
    //             .param("search", "test")
    //             .param("page", "0")
    //             .param("size", "10")
    //             .param("sort", "invalid_field,asc"))
    //             .andExpect(status().isBadRequest())
    //             .andExpect(jsonPath("$.error").value("Invalid sort property: invalid_field"));
    // }

    @Test
    void testUpdateUserById_Success() throws Exception {
        when(userService.updateUserById(any(Long.class), any(User.class))).thenReturn(testUser);

        mockMvc.perform(put("/api/user/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testDeleteUserById_Success() throws Exception {
        when(userService.deleteUserById(any(Long.class))).thenReturn(true);

        mockMvc.perform(delete("/api/user/delete/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("User with ID 1 deleted successfully."));
    }

    @Test
    void testDeleteUserById_NotFound() throws Exception {
        when(userService.deleteUserById(any(Long.class))).thenReturn(false);

        mockMvc.perform(delete("/api/user/delete/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testVerifyUser_Success() throws Exception {
        Map<String, String> verifyRequest = new HashMap<>();
        verifyRequest.put("email", "test@example.com");
        verifyRequest.put("code", "123456");

        when(userService.verifyUser(anyString(), anyString())).thenReturn("Verification successful. You can now log in.");

        mockMvc.perform(post("/api/user/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Verification successful. You can now log in."));
    }

    @Test
    void testResendVerification_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");

        when(userService.resendVerificationCode(anyString())).thenReturn("A new verification code has been sent to your email.");

        mockMvc.perform(post("/api/user/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("A new verification code has been sent to your email."));
    }
} 