package com.abc.jibpilot.course.repository;

import com.abc.jibpilot.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String code);

    @Query(value = """
        SELECT * FROM courses 
        ORDER BY search_text <@> :query 
        LIMIT :limit
        """, nativeQuery = true)
    List<Course> searchCourses(@Param("query") String query, @Param("limit") int limit);
}
