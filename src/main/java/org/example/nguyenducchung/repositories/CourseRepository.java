package org.example.nguyenducchung.repositories;

import org.example.nguyenducchung.models.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
