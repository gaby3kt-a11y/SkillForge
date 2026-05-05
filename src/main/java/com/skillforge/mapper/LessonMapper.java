package com.skillforge.mapper;

import com.skillforge.dto.LessonSummaryDTO;
import com.skillforge.model.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LessonMapper {

    @Mapping(target = "lessonType", expression = "java(lesson.getType() != null ? lesson.getType().name() : \"VIDEO\")")
    @Mapping(target = "isCompleted", ignore = true)
    @Mapping(target = "watchTimeSeconds", ignore = true)
    @Mapping(target = "lastPositionSeconds", ignore = true)
    LessonSummaryDTO toSummaryDTO(Lesson lesson);

    List<LessonSummaryDTO> toSummaryDTOs(List<Lesson> lessons);
}