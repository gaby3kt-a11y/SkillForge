package com.skillforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorInfoDTO {

    private String id;
    private String fullName;
    private String username;
    private String profilePictureUrl;
    private String bio;
    private Integer totalCourses;
    private Integer totalStudents;
    private Double averageRating;
}