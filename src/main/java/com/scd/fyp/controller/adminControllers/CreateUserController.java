package com.scd.fyp.controller.adminControllers;

import com.scd.fyp.model.User;
import com.scd.fyp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.scd.fyp.model.Role;
import com.scd.fyp.model.UserRole;
import com.scd.fyp.repository.RoleRepository;
import com.scd.fyp.repository.UserRoleRepository;



import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class CreateUserController {

    @Autowired
    private UserRepository userRepository;

    // For password hashing
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @PostMapping("/create")
    public String createUser(@RequestBody CreateUserRequest request) {
        // ✅ Duplicate email check
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setSemester(request.getSemester());

        userRepository.save(user);

        if (request.getRoles() != null) {
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByRoleName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

                UserRole userRole = new UserRole();
                userRole.setUser(user);
                userRole.setRole(role);

                userRoleRepository.save(userRole);
            }
        }

        return "User saved with roles: " + request.getRoles();
    }





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

