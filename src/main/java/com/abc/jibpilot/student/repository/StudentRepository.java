package com.abc.jibpilot.student.repository;

import com.abc.jibpilot.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);

    @Query(value = """
        SELECT * FROM students 
        ORDER BY search_text <@> :query 
        LIMIT :limit
        """, nativeQuery = true)
    List<Student> searchStudents(@Param("query") String query, @Param("limit") int limit);
}
