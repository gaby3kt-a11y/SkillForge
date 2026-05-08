package com.skillforge.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDTO<T> {

  private List<T> content;
  private int currentPage;
  private int totalPages;
  private long totalElements;
  private int pageSize;
  private boolean first;
  private boolean last;
  private boolean hasNext;
  private boolean hasPrevious;

  public static <T> PageResponseDTO<T> fromPage(Page<T> page) {
    return PageResponseDTO.<T>builder()
        .content(page.getContent())
        .currentPage(page.getNumber())
        .totalPages(page.getTotalPages())
        .totalElements(page.getTotalElements())
        .pageSize(page.getSize())
        .first(page.isFirst())
        .last(page.isLast())
        .hasNext(page.hasNext())
        .hasPrevious(page.hasPrevious())
        .build();
  }
}
