package com.skillforge.mapper;

import com.skillforge.dto.ModuleSummaryDTO;
import com.skillforge.model.Module;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    uses = {LessonMapper.class})
public interface ModuleMapper {

  @Mapping(
      target = "totalLessons",
      expression = "java(module.getLessons() != null ? module.getLessons().size() : 0)")
  @Mapping(target = "totalDurationMinutes", expression = "java(calculateTotalDuration(module))")
  @Mapping(target = "lessons", source = "lessons")
  ModuleSummaryDTO toSummaryDTO(Module module);

  List<ModuleSummaryDTO> toSummaryDTOs(List<Module> modules);

  default int calculateTotalDuration(Module module) {
    if (module.getLessons() == null) {
      return 0;
    }
    return module.getLessons().stream()
        .mapToInt(lesson -> lesson.getDurationMinutes() != null ? lesson.getDurationMinutes() : 0)
        .sum();
  }
}
