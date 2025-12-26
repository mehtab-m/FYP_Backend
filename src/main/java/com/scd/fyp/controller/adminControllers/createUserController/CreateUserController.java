package com.scd.fyp.controller.adminControllers.createUserController;

import com.scd.fyp.model.User;
import com.scd.fyp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class CreateUserController {

    @Autowired
    private UserRepository userRepository;

    // For password hashing
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/create")
    public String createUser(@RequestBody CreateUserRequest request) {

        // 1️⃣ Create User entity
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setSemester(request.getSemester()); // nullable if not student

        // 2️⃣ Save user
        userRepository.save(user);

        // 3️⃣ Here, you can handle roles mapping separately (user_roles table)
        // Example: store role strings in DB or handle later in service layer

        return "User saved to DB: " + user.getName();
    }

    // DTO
    public static class CreateUserRequest {
        private String name;
        private String email;
        private String password;
        private Integer semester;  // nullable for non-students
        private List<String> roles; // e.g., ["STUDENT"]

        // getters & setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public Integer getSemester() { return semester; }
        public void setSemester(Integer semester) { this.semester = semester; }
        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }
    }
}

