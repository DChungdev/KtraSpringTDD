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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    void testRegisterCourse_Success() throws Exception {
        // Given
        String studentEmail = "student@example.com";
        Long courseId = 3L;

        Course course1 = Course.builder()
                .id(1L)
                .name("Java Basics")
                .startTime(new Date())
                .endTime(new Date())
                .price(1000L)
                .build();

        Course course2 = Course.builder()
                .id(2L)
                .name("Spring Boot")
                .startTime(new Date())
                .endTime(new Date())
                .price(2000L)
                .build();

        Course newCourse = Course.builder()
                .id(3L)
                .name("Microservices")
                .startTime(new Date())
                .endTime(new Date())
                .price(3000L)
                .build();


        List<Course> registeredCourses = Arrays.asList(course1, course2, newCourse);

        when(registrationService.registerCourse(studentEmail, courseId)).thenReturn(registeredCourses);

        // When & Then
        mockMvc.perform(post("/api/registrations/register")
                        .param("studentEmail", studentEmail)
                        .param("courseId", String.valueOf(courseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Java Basics"))
                .andExpect(jsonPath("$[1].name").value("Spring Boot"))
                .andExpect(jsonPath("$[2].name").value("Microservices"));

        verify(registrationService).registerCourse(studentEmail, courseId);
    }
}

