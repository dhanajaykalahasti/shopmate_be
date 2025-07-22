package com.sp.shop.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sp.shop.entity.Role;
import com.sp.shop.entity.User;
import com.sp.shop.repository.UserRepository;
import com.sp.shop.security.JwtUtil;

import java.util.UUID;

@Service
public class UserService {

	@Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private EmailService emailService;
    

    @Autowired
    private WhatsAppService whatsAppService;
    
    // Register new user
    public User registerUser(User user) {
        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        
        // Check if username already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already taken");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));  // Encrypt password
        user.setRoles(Set.of(Role.ROLE_STAFF));  // Default user role
        user.setVerified(false);  // Not verified yet

        // Generate a verification code
        String verificationCode = UUID.randomUUID().toString().substring(0, 6);
        user.setVerificationCode(verificationCode);

        User savedUser = userRepository.save(user);


        // Send email notification
        emailService.sendVerificationEmail(user.getEmail(), verificationCode);
         //Send email notification
//        emailService.sendVerificationEmail(user.getEmail(), verificationCode);
        String mobile = user.getMobile();
        if (mobile != null && !mobile.startsWith("+")) {
            mobile = "+91" + mobile;
        }

        String message = "Hi " + user.getUsername() + ", your verification code is: " + verificationCode;
        whatsAppService.sendMessage(mobile, message);
        return savedUser;
    }
    
    public String verifyUser(String email, String code) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user.isVerified()) {
                return "User is already verified.";
            }

            if (user.getVerificationCode() == null || user.getVerificationCode().isEmpty()) {
                return "No verification code found. Please request a new one.";
            }

            if (user.getVerificationCode().equals(code)) {
                user.setVerified(true);  // Activate the user
                user.setVerificationCode(null);  // Remove code
                userRepository.save(user);

                // Send success email
                emailService.sendSuccessEmail(user.getEmail(), user.getUsername());

                return "Verification successful. You can now log in.";
            } else {
                return "Invalid verification code.";
            }
        } else {
            return "User not found.";
        }
    }

    public String resendVerificationCode(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user.isVerified()) {
                return "User is already verified. No need to resend the code.";
            }

            // Generate a new verification code
            String newCode = UUID.randomUUID().toString().substring(0, 6);
            user.setVerificationCode(newCode);
            userRepository.save(user);  // Update the user in the database

            // Send the new code via email
            emailService.sendVerificationEmail(user.getEmail(), newCode);

            return "A new verification code has been sent to your email.";
        } else {
            return "User not found.";
        }
    }

    public User getUserProfile(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public String loginUser(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (!user.isVerified()) {
                throw new RuntimeException("Please verify your email before logging in.");
            }
            if (passwordEncoder.matches(password, user.getPassword())) {
                return jwtUtil.generateToken(user.getUsername());  // Return JWT if credentials match
            }
        }
        throw new RuntimeException("Invalid email or password!");
    }
    
    public User updateUser(String username, User updatedUser) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();

            // Update only allowed fields
            existingUser.setUsername(updatedUser.getUsername());  // Fixed this line
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setMobile(updatedUser.getMobile());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }

            return userRepository.save(existingUser);
        }
        return null;
    }
    
    public User updateUserById(Long id, User updatedUser) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();

            // Update only allowed fields
            if (updatedUser.getUsername() != null && !updatedUser.getUsername().isEmpty()) {
                // Check if the new username is already taken by another user
                Optional<User> existingUsername = userRepository.findByUsername(updatedUser.getUsername());
                if (existingUsername.isPresent() && !existingUsername.get().getId().equals(id)) {
                    throw new RuntimeException("Username is already taken");
                }
                existingUser.setUsername(updatedUser.getUsername());
            }
            
            if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
                // Check if the new email is already taken by another user
                Optional<User> existingEmail = userRepository.findByEmail(updatedUser.getEmail());
                if (existingEmail.isPresent() && !existingEmail.get().getId().equals(id)) {
                    throw new RuntimeException("Email is already registered");
                }
                existingUser.setEmail(updatedUser.getEmail());
            }
            
            if (updatedUser.getMobile() != null && !updatedUser.getMobile().isEmpty()) {
                existingUser.setMobile(updatedUser.getMobile());
            }
            
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }

            return userRepository.save(existingUser);
        }
        return null;
    }

    public boolean deleteUser(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            userRepository.delete(optionalUser.get());
            return true;
        }
        return false;
    }
    
    public boolean deleteUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            userRepository.delete(optionalUser.get());
            return true;
        }
        return false;
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public org.springframework.data.domain.Page<User> getAllUsers(org.springframework.data.domain.Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    public org.springframework.data.domain.Page<User> searchUsers(String searchTerm, org.springframework.data.domain.Pageable pageable) {
        return userRepository.findByUsernameContainingOrEmailContainingOrMobileContaining(searchTerm, searchTerm, searchTerm, pageable);
    }
}