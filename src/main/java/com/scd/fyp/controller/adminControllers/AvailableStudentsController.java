package com.scd.fyp.controller.adminControllers;

import com.scd.fyp.model.User;
import com.scd.fyp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AvailableStudentsController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/available_students_for_grouping")
    public List<User> getAvailableStudentsForGrouping() {
        return userRepository.findAvailableStudentsForGrouping();
    }
}
