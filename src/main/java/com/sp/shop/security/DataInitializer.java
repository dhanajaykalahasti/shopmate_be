package com.sp.shop.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sp.shop.entity.Role;
import com.sp.shop.entity.User;
import com.sp.shop.repository.UserRepository;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

	 private final UserRepository userRepository;
	    private final PasswordEncoder passwordEncoder;

	    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
	        this.userRepository = userRepository;
	        this.passwordEncoder = passwordEncoder;
	    }

	    @Override
	    public void run(String... args) {
	        if (userRepository.findByUsername("admin").isEmpty()) {
	            User admin = new User();
	            admin.setUsername("admin");
	            admin.setPassword(passwordEncoder.encode("admin123"));
	            admin.setEmail("admin@example.com");
	            admin.setRoles(Set.of(Role.ROLE_ADMIN));
	            admin.setMobile("1234567890");
	            admin.setVerified(true);
	            userRepository.save(admin);
	            System.out.println("Admin user created.");
	        }

	        if (userRepository.findByUsername("user").isEmpty()) {
	            User user = new User();
	            user.setUsername("user");
	            user.setPassword(passwordEncoder.encode("user123"));
	            user.setEmail("user@example.com");
	            user.setRoles(Set.of(Role.ROLE_STAFF));
	            user.setMobile("9876543210");
	            user.setVerified(true);
	            userRepository.save(user);
	            System.out.println("Regular user created.");
	        }
	    }
}
