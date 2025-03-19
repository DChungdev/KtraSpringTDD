package org.example.nguyenducchung;

import org.example.nguyenducchung.controllers.RegistrationController;
import org.example.nguyenducchung.models.entities.Course;
import org.example.nguyenducchung.services.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
class RegistrationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

    @Test
    void testRegisterCourse_Success1() throws Exception {
        Long courseId = 1L;
        String studentEmail = "student@example.com";

        Course course = Course.builder()
                .id(1L)
                .name("Java Basics")
                .startTime(new Date())
                .endTime(new Date())
                .price(1000L)
                .build();

        List<Course> registeredCourses = List.of(course);

        when(registrationService.registerCourse(studentEmail, courseId)).thenReturn(registeredCourses);

        mockMvc.perform(post("/api/registrations/register")
                        .param("studentEmail", studentEmail)
                        .param("courseId", courseId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Java Basics"));
    }

    @Test
    void testRegisterCourse_Fail_StudentNotFound() throws Exception {
        // Given
        Long courseId = 1L;
        String studentEmail = "nonexistent@example.com";

        when(registrationService.registerCourse(studentEmail, courseId))
                .thenThrow(new RuntimeException("Student not found"));

        // When & Then
        mockMvc.perform(post("/api/registrations/register")
                        .param("studentEmail", studentEmail)
                        .param("courseId", courseId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Student not found"));

        verify(registrationService).registerCourse(studentEmail, courseId);
    }

    @Test
    void testRegisterCourse_Fail_CourseNotFound() throws Exception {
        // Given
        Long courseId = 99L;
        String studentEmail = "student@example.com";

        when(registrationService.registerCourse(studentEmail, courseId))
                .thenThrow(new RuntimeException("Course not found"));

        // When & Then
        mockMvc.perform(post("/api/registrations/register")
                        .param("studentEmail", studentEmail)
                        .param("courseId", courseId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Course not found"));

        verify(registrationService).registerCourse(studentEmail, courseId);
    }

    @Test
    void testRegisterCourse_Fail_CourseAlreadyStarted() throws Exception {
        // Given
        Long courseId = 1L;
        String studentEmail = "student@example.com";

        when(registrationService.registerCourse(studentEmail, courseId))
                .thenThrow(new RuntimeException("Cannot register for a course that has already started"));

        // When & Then
        mockMvc.perform(post("/api/registrations/register")
                        .param("studentEmail", studentEmail)
                        .param("courseId", courseId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot register for a course that has already started"));

        verify(registrationService).registerCourse(studentEmail, courseId);
    }

//
//    @Test
//    void testRegisterCourse_Success() throws Exception {
//        // Given
//        String studentEmail = "student@example.com";
//        Long courseId = 3L;
//
//        Course course1 = Course.builder()
//                .id(1L)
//                .name("Java Basics")
//                .startTime(new Date())
//                .endTime(new Date())
//                .price(1000L)
//                .build();
//
//        Course course2 = Course.builder()
//                .id(2L)
//                .name("Spring Boot")
//                .startTime(new Date())
//                .endTime(new Date())
//                .price(2000L)
//                .build();
//
//        Course newCourse = Course.builder()
//                .id(3L)
//                .name("Microservices")
//                .startTime(new Date())
//                .endTime(new Date())
//                .price(3000L)
//                .build();
//
//
//        List<Course> registeredCourses = Arrays.asList(course1, course2, newCourse);
//
//        when(registrationService.registerCourse(studentEmail, courseId)).thenReturn(registeredCourses);
//
//        // When & Then
//        mockMvc.perform(post("/api/registrations/register")
//                        .param("studentEmail", studentEmail)
//                        .param("courseId", String.valueOf(courseId)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(3))
//                .andExpect(jsonPath("$[0].name").value("Java Basics"))
//                .andExpect(jsonPath("$[1].name").value("Spring Boot"))
//                .andExpect(jsonPath("$[2].name").value("Microservices"));
//
//        verify(registrationService).registerCourse(studentEmail, courseId);
//    }

    @Test
    void testUnregisterCourse_Success() throws Exception {
        Long courseId = 1L;
        String studentEmail = "student@example.com";

        doNothing().when(registrationService).unregisterCourse(studentEmail, courseId);

        mockMvc.perform(delete("/api/registrations/unregister/{courseId}/{email}", courseId, studentEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Unregistered successfully"));

        verify(registrationService).unregisterCourse(studentEmail, courseId);
    }


    //Hủy đăng ký khi khóa học đã bắt đầu
    @Test
    void testUnregisterCourse_CourseAlreadyStarted() throws Exception {
        Long courseId = 1L;
        String studentEmail = "student@example.com";
        String errorMessage = "Cannot unregister from a course that has already started";

        doThrow(new RuntimeException(errorMessage))
                .when(registrationService).unregisterCourse(studentEmail, courseId);

        mockMvc.perform(delete("/api/registrations/unregister/{courseId}/{email}", courseId, studentEmail))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    //Hủy đăng ký không tìm thấy sinh viên
    @Test
    void testUnregisterCourse_StudentNotFound() throws Exception {
        Long courseId = 1L;
        String studentEmail = "notfound@example.com";
        String errorMessage = "Student not found";

        doThrow(new RuntimeException(errorMessage))
                .when(registrationService).unregisterCourse(studentEmail, courseId);

        mockMvc.perform(delete("/api/registrations/unregister/{courseId}/{email}", courseId, studentEmail))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    //Hủy đăng ký không tìm thấy khóa học
    @Test
    void testUnregisterCourse_CourseNotFound() throws Exception {
        Long courseId = 999L;
        String studentEmail = "student@example.com";
        String errorMessage = "Course not found";

        doThrow(new RuntimeException(errorMessage))
                .when(registrationService).unregisterCourse(studentEmail, courseId);

        mockMvc.perform(delete("/api/registrations/unregister/{courseId}/{email}", courseId, studentEmail))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

}

