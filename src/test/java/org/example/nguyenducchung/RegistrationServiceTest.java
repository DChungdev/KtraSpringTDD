package org.example.nguyenducchung;

import org.example.nguyenducchung.models.entities.Course;
import org.example.nguyenducchung.models.entities.Registration;
import org.example.nguyenducchung.models.entities.Student;
import org.example.nguyenducchung.repositories.CourseRepository;
import org.example.nguyenducchung.repositories.RegistrationRepository;
import org.example.nguyenducchung.repositories.StudentRepository;
import org.example.nguyenducchung.services.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {
    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void testRegisterCourse_Success_WithDiscount() {
        Student student = Student.builder()
                .id(1L)
                .email("student@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        Course course1 = Course.builder()
                .id(1L)
                .name("Java Basics")
                .startTime(new Date(System.currentTimeMillis() + 86400000)) // Ngày mai
                .endTime(new Date(System.currentTimeMillis() + 86400000 * 10))
                .price(1000L)
                .build();

        Course course2 = Course.builder()
                .id(2L)
                .name("Spring Boot")
                .startTime(new Date(System.currentTimeMillis() + 86400000 * 2))
                .endTime(new Date(System.currentTimeMillis() + 86400000 * 12))
                .price(2000L)
                .build();

        Registration existingRegistration1 = Registration.builder()
                .id(1L)
                .student(student)
                .course(course1)
                .price(course1.getPrice())
                .registeredDate(new Date())
                .build();

        Registration existingRegistration2 = Registration.builder()
                .id(2L)
                .student(student)
                .course(course2)
                .price(course2.getPrice())
                .registeredDate(new Date())
                .build();

        Course newCourse = Course.builder()
                .id(3L)
                .name("Microservices")
                .startTime(new Date(System.currentTimeMillis() + 86400000 * 5))
                .endTime(new Date(System.currentTimeMillis() + 86400000 * 15))
                .price(3000L)
                .build();

        when(studentRepository.findByEmail(student.getEmail())).thenReturn(student);
        when(courseRepository.findById(newCourse.getId())).thenReturn(Optional.of(newCourse));

        List<Registration> existingRegistrations = new ArrayList<>(Arrays.asList(existingRegistration1, existingRegistration2));

        when(registrationRepository.findByStudentId(student.getId()))
                .thenAnswer(invocation -> new ArrayList<>(existingRegistrations)); // Luôn trả về danh sách hiện tại

        Long expectedPrice = newCourse.getPrice() * 75 / 100;

        when(registrationRepository.save(any(Registration.class)))
                .thenAnswer(invocation -> {
                    Registration newReg = invocation.getArgument(0);
                    existingRegistrations.add(newReg); // Cập nhật danh sách sau khi đăng ký
                    return newReg;
                });

        // When
        List<Course> registeredCourses = registrationService.registerCourse(student.getEmail(), newCourse.getId());

        // Then
        assertNotNull(registeredCourses);
        assertEquals(3, registeredCourses.size());
        verify(registrationRepository).save(argThat(reg -> reg.getPrice().equals(expectedPrice)));
    }

    @Test
    void shouldThrowExceptionWhenRegisteringForStartedCourse() {
        String studentEmail = "student@example.com";
        Student student = Student.builder().id(1L).email(studentEmail).build();

        Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000); // Hôm qua
        Date endDate = new Date(System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000); // 10 ngày sau

        Course startedCourse = Course.builder()
                .id(2L)
                .name("Spring Boot")
                .startTime(startDate)
                .endTime(endDate)
                .price(2000L)
                .build();

        when(studentRepository.findByEmail(studentEmail)).thenReturn(student);
        when(courseRepository.findById(2L)).thenReturn(Optional.of(startedCourse));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            registrationService.registerCourse(studentEmail, 2L);
        });

        assertEquals("Cannot register for a course that has already started", thrown.getMessage());

        verify(registrationRepository, never()).save(any(Registration.class));
    }


    @Test
    void testRegisterCourse_Fail_AlreadyRegistered() {
        // Given
        Student student = Student.builder()
                .id(1L)
                .email("student@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        Course course = Course.builder()
                .id(1L)
                .name("Java Basics")
                .startTime(new Date(System.currentTimeMillis() + 86400000))
                .endTime(new Date(System.currentTimeMillis() + 86400000 * 10))
                .price(1000L)
                .build();

        Registration existingRegistration = Registration.builder()
                .id(1L)
                .student(student)
                .course(course)
                .price(course.getPrice())
                .registeredDate(new Date())
                .build();

        when(studentRepository.findByEmail(student.getEmail())).thenReturn(student);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(registrationRepository.findByStudentId(student.getId()))
                .thenReturn(List.of(existingRegistration));

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () ->
                registrationService.registerCourse(student.getEmail(), course.getId())
        );

        assertEquals("Student has already registered for this course", exception.getMessage());
        verify(registrationRepository, never()).save(any(Registration.class));
    }
    @Test
    void testRegisterCourse_StudentNotFound() {
        // Given
        String studentEmail = "nonexistent@example.com";
        Long courseId = 1L;

        when(studentRepository.findByEmail(studentEmail)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                registrationService.registerCourse(studentEmail, courseId));

        assertEquals("Student not found", exception.getMessage());
    }
    @Test
    void testRegisterCourse_CourseNotFound() {
        // Given
        Student student = Student.builder()
                .id(1L)
                .email("student@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        Long courseId = 999L; // ID không tồn tại

        when(studentRepository.findByEmail(student.getEmail())).thenReturn(student);
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                registrationService.registerCourse(student.getEmail(), courseId));

        assertEquals("Course not found", exception.getMessage());
    }


    //unregister
    @Test
    void testUnregisterCourse_Success() {
        // Given
        Student student = Student.builder()
                .id(1L)
                .email("student@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        Course course = Course.builder()
                .id(1L)
                .name("Java Basics")
                .startTime(new Date(System.currentTimeMillis() + 86400000)) // Ngày mai
                .endTime(new Date(System.currentTimeMillis() + 86400000 * 10))
                .price(1000L)
                .build();

        Registration registration = Registration.builder()
                .id(1L)
                .student(student)
                .course(course)
                .price(course.getPrice())
                .registeredDate(new Date())
                .build();

        when(studentRepository.findByEmail(student.getEmail())).thenReturn(student);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(registrationRepository.findByStudentId(student.getId()))
                .thenReturn(Collections.singletonList(registration));

        // When
        registrationService.unregisterCourse(student.getEmail(), course.getId());

        // Then
        verify(registrationRepository).delete(registration);
    }

    @Test
    void testUnregisterCourse_AlreadyStarted() {
        // Given
        Student student = Student.builder()
                .id(1L)
                .email("student@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        Course course = Course.builder()
                .id(1L)
                .name("Java Basics")
                .startTime(new Date(System.currentTimeMillis() - 86400000)) // Đã bắt đầu
                .endTime(new Date(System.currentTimeMillis() + 86400000 * 9))
                .price(1000L)
                .build();

        Registration registration = Registration.builder()
                .id(1L)
                .student(student)
                .course(course)
                .price(course.getPrice())
                .registeredDate(new Date())
                .build();

        when(studentRepository.findByEmail(student.getEmail())).thenReturn(student);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(registrationRepository.findByStudentId(student.getId()))
                .thenReturn(Collections.singletonList(registration));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                registrationService.unregisterCourse(student.getEmail(), course.getId()));

        assertEquals("Cannot unregister from a course that has already started", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUnregisteringWithNonExistentStudent() {
        String studentEmail = "nonexistent@example.com";
        Long courseId = 1L;

        when(studentRepository.findByEmail(studentEmail)).thenReturn(null);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            registrationService.unregisterCourse(studentEmail, courseId);
        });

        assertEquals("Student not found", thrown.getMessage());

        verify(registrationRepository, never()).delete(any(Registration.class));
    }

    @Test
    void testUnregisterCourse_StudentNotRegistered() {
        Student student = Student.builder()
                .id(1L)
                .email("student@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        Course course = Course.builder()
                .id(1L)
                .name("Java Basics")
                .startTime(new Date(System.currentTimeMillis() + 86400000)) // Khóa học chưa bắt đầu
                .build();

        when(studentRepository.findByEmail(student.getEmail())).thenReturn(student);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(registrationRepository.findByStudentId(student.getId())).thenReturn(Collections.emptyList()); // Không có đăng ký nào

        Exception exception = assertThrows(RuntimeException.class, () -> {
            registrationService.unregisterCourse(student.getEmail(), course.getId());
        });

        assertEquals("Registration not found", exception.getMessage());
        verify(registrationRepository, never()).delete(any(Registration.class));
    }

    @Test
    void testUnregisterCourse_CourseAlreadyStarted() {
        Student student = Student.builder()
                .id(1L)
                .email("student@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        Course course = Course.builder()
                .id(1L)
                .name("Java Basics")
                .startTime(new Date(System.currentTimeMillis() - 86400000))
                .build();

        Registration registration = Registration.builder()
                .id(1L)
                .student(student)
                .course(course)
                .registeredDate(new Date())
                .build();

        when(studentRepository.findByEmail(student.getEmail())).thenReturn(student);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(registrationRepository.findByStudentId(student.getId())).thenReturn(List.of(registration));

        // Gọi service và kiểm tra exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            registrationService.unregisterCourse(student.getEmail(), course.getId());
        });

        assertEquals("Cannot unregister from a course that has already started", exception.getMessage());
        verify(registrationRepository, never()).delete(any(Registration.class));
    }

}
