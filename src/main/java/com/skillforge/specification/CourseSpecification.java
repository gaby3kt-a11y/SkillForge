package com.skillforge.specification;

import com.skillforge.dto.CourseFilterDTO;
import com.skillforge.model.Course;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CourseSpecification {

    public static Specification<Course> withFilters(CourseFilterDTO filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only published courses for public view
            predicates.add(criteriaBuilder.isTrue(root.get("isPublished")));
            predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));

            if (filters == null) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

            // Search by keyword (title or description)
            if (StringUtils.hasText(filters.getSearchKeyword())) {
                String keyword = "%" + filters.getSearchKeyword().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), keyword);
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), keyword);
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
            }

            // Filter by categories
            if (filters.getCategories() != null && !filters.getCategories().isEmpty()) {
                predicates.add(root.get("category").in(filters.getCategories()));
            }

            // Filter by difficulty levels
            if (filters.getDifficultyLevels() != null && !filters.getDifficultyLevels().isEmpty()) {
                predicates.add(root.get("difficultyLevel").in(filters.getDifficultyLevels()));
            }

            // Filter by price range
            if (filters.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"), filters.getMinPrice()));
            }
            if (filters.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"), filters.getMaxPrice()));
            }
            if (Boolean.TRUE.equals(filters.getOnlyFree())) {
                predicates.add(criteriaBuilder.equal(root.get("price"), BigDecimal.ZERO));
            }

            // Filter by minimum rating
            if (filters.getMinRating() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("averageRating"), filters.getMinRating()));
            }

            // Apply sorting
            if (StringUtils.hasText(filters.getSortBy())) {
                switch (filters.getSortBy()) {
                    case "rating":
                        query.orderBy(criteriaBuilder.desc(root.get("averageRating")));
                        break;
                    case "price":
                        if ("asc".equalsIgnoreCase(filters.getSortDirection())) {
                            query.orderBy(criteriaBuilder.asc(root.get("price")));
                        } else {
                            query.orderBy(criteriaBuilder.desc(root.get("price")));
                        }
                        break;
                    case "enrollments":
                        query.orderBy(criteriaBuilder.desc(root.get("totalEnrollments")));
                        break;
                    case "newest":
                        query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
                        break;
                    default:
                        query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
                }
            } else {
                query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}