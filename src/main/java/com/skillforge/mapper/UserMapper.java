package com.skillforge.mapper;

import com.skillforge.dto.InstructorBasicDTO;
import com.skillforge.dto.InstructorInfoDTO;
import com.skillforge.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "totalCourses", ignore = true)
    @Mapping(target = "totalStudents", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    InstructorInfoDTO toInstructorInfoDTO(User user);

    InstructorBasicDTO toInstructorBasicDTO(User user);

    @Named("toInstructorBasicDTO")
    default InstructorBasicDTO toInstructorBasicDTONamed(User user) {
        if (user == null) return null;
        return InstructorBasicDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }
}