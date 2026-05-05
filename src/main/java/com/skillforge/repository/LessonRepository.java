package com.skillforge.repository;

import com.skillforge.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, String> {

    List<Lesson> findByModuleIdOrderByOrderIndexAsc(String moduleId);

    @Query("SELECT l FROM Lesson l WHERE l.module.course.id = :courseId ORDER BY l.module.orderIndex, l.orderIndex")
    List<Lesson> findAllLessonsByCourse(@Param("courseId") String courseId);

    @Modifying
    @Query("UPDATE Lesson l SET l.orderIndex = l.orderIndex + 1 WHERE l.module.id = :moduleId AND l.orderIndex >= :fromIndex")
    void shiftLessonOrder(@Param("moduleId") String moduleId, @Param("fromIndex") int fromIndex);

    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.module.course.id = :courseId")
    int countLessonsByCourse(@Param("courseId") String courseId);

    @Query("SELECT l FROM Lesson l WHERE l.module.course.id = :courseId AND l.isPreview = true")
    List<Lesson> findPreviewLessonsByCourse(@Param("courseId") String courseId);
}