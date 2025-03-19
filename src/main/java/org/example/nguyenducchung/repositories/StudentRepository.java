package org.example.nguyenducchung.repositories;

import org.example.nguyenducchung.models.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Student findByEmail(String email);
}
