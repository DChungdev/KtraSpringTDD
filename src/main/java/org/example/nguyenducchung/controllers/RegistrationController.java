package org.example.nguyenducchung.controllers;

import org.example.nguyenducchung.models.entities.Course;
import org.example.nguyenducchung.services.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity registerCourse(
            @RequestParam String studentEmail,
            @RequestParam Long courseId) {
        try{
            List<Course> registeredCourses = registrationService.registerCourse(studentEmail, courseId);
            return ResponseEntity.ok(registeredCourses);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/unregister/{courseId}/{email}")
    public ResponseEntity unregisterCourse(@PathVariable Long courseId, @PathVariable String email) {
        try{
            registrationService.unregisterCourse(email, courseId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Unregistered successfully");
            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }

    }
}
