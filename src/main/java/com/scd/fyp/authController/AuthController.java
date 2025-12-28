// Controller: AuthController
package com.scd.fyp.authController;

import com.scd.fyp.model.User;
import com.scd.fyp.model.UserRole;
import com.scd.fyp.model.Role;
import com.scd.fyp.repository.UserRepository;
import com.scd.fyp.repository.UserRoleRepository;
import com.scd.fyp.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // DTO for login request
    public static class LoginRequest {
        private String email;
        private String password;
        private String role;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();

        // Step 1: Find user by email
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            response.put("success", false);
            response.put("message", "Invalid email or password");
            return response;
        }


        // Step 2: Verify role
        Role role = roleRepository.findByRoleName(request.getRole().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Invalid role"));

        UserRole userRole = userRoleRepository.findByUserIdAndRoleId(user.getUserId(), role.getRoleId());
        if (userRole == null) {
            response.put("success", false);
            response.put("message", "User does not have this role");
            return response;
        }

        // Step 3: Generate JWT token (placeholder here)
        String token = "jwt_token_here"; // integrate with JWT util later

        // Step 4: Build response
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getUserId());
        userMap.put("name", user.getName());
        userMap.put("email", user.getEmail());
        userMap.put("role", request.getRole());

        response.put("success", true);
        response.put("user", userMap);
        response.put("token", token);

        return response;
    }
}
