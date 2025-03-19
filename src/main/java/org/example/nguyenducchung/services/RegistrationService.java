package org.example.nguyenducchung.services;

import org.example.nguyenducchung.models.entities.Course;
import org.example.nguyenducchung.models.entities.Registration;
import org.example.nguyenducchung.models.entities.Student;
import org.example.nguyenducchung.repositories.CourseRepository;
import org.example.nguyenducchung.repositories.RegistrationRepository;
import org.example.nguyenducchung.repositories.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public RegistrationService(RegistrationRepository registrationRepository, StudentRepository studentRepository, CourseRepository courseRepository) {
        this.registrationRepository = registrationRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }
    public List<Course> registerCourse(String studentEmail, Long courseId) {
        Student student = studentRepository.findByEmail(studentEmail);
        if (student == null) {
            throw new RuntimeException("Student not found");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<Registration> registrations = registrationRepository.findByStudentId(student.getId());

        boolean alreadyRegistered = registrations.stream()
                .anyMatch(reg -> reg.getCourse().getId().equals(courseId));

        // Kiểm tra nếu khóa học đã bắt đầu
        if (course.getStartTime().before(new Date())) {
            throw new RuntimeException("Cannot register for a course that has already started");
        }

        if (alreadyRegistered) {
            throw new RuntimeException("Student has already registered for this course");
        }

        Long discountedPrice = course.getPrice();
        if (registrations.size() >= 2) {
            discountedPrice = course.getPrice() * 75 / 100;
        }

        Registration newRegistration = Registration.builder()
                .student(student)
                .course(course)
                .price(discountedPrice)
                .registeredDate(new Date())
                .build();

        registrationRepository.save(newRegistration);

        List<Course> registeredCourses = registrations.stream()
                .map(Registration::getCourse)
                .collect(Collectors.toList());

        registeredCourses.add(course);

        return registeredCourses;
    }

    public void unregisterCourse(String studentEmail, Long courseId) {
        Student student = studentRepository.findByEmail(studentEmail);
        if (student == null) {
            throw new RuntimeException("Student not found");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Registration registration = registrationRepository.findByStudentId(student.getId())
                .stream()
                .filter(reg -> reg.getCourse().getId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        // Kiểm tra xem khóa học đã bắt đầu chưa
        if (course.getStartTime().before(new Date())) {
            throw new RuntimeException("Cannot unregister from a course that has already started");
        }

        registrationRepository.delete(registration);
    }


}
