package org.example.nguyenducchung.repositories;

import org.example.nguyenducchung.models.entities.Registration;
import org.example.nguyenducchung.models.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByStudentAndCourseStartTimeAfter(Student student, Date date);
    void deleteByStudentEmailAndCourseId(String email, Long courseId);
    List<Registration> findByStudentId(Long studentId);

}
