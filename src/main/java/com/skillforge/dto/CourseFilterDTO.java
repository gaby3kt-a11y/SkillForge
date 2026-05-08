package com.skillforge.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseFilterDTO {

  private String searchKeyword;
  private List<String> categories;
  private List<String> difficultyLevels;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private Double minRating;
  private Integer page;
  private Integer size;
  private String sortBy; // "rating", "price", "enrollments", "newest"
  private String sortDirection; // "asc", "desc"
  private Boolean onlyFree;
  private Boolean onlyPublished;
}
