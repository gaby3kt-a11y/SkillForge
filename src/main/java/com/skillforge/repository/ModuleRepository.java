package com.skillforge.repository;

import com.skillforge.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, String> {

    List<Module> findByCourseIdOrderByOrderIndexAsc(String courseId);

    @Query("SELECT m FROM Module m WHERE m.course.id = :courseId AND m.course.isDeleted = false")
    List<Module> findActiveByCourseId(@Param("courseId") String courseId);

    @Modifying
    @Query("UPDATE Module m SET m.orderIndex = m.orderIndex + 1 WHERE m.course.id = :courseId AND m.orderIndex >= :fromIndex")
    void shiftModuleOrder(@Param("courseId") String courseId, @Param("fromIndex") int fromIndex);

    @Query("SELECT COUNT(m) FROM Module m WHERE m.course.id = :courseId")
    int countModulesByCourse(@Param("courseId") String courseId);

    @Query("SELECT COALESCE(SUM(l.durationMinutes), 0) FROM Module m JOIN m.lessons l WHERE m.course.id = :courseId")
    Integer getTotalCourseDuration(@Param("courseId") String courseId);
}