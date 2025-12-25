package com.scd.fyp.controller.adminControllers.createStudent;

import com.scd.fyp.model.Student;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/students")
public class CreateStudentController {
    @PostMapping("/create")
    public String createStudent(@RequestBody Student student) {
        return "Student created: " + student.getName();
    }
}
