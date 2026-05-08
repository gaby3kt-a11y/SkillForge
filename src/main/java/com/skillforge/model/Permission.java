package com.skillforge.model;

import jakarta.persistence.*;
import java.util.Set;

import lombok.*;

@Entity
@Table(name = "permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false, unique = true)
  private String name; // COURSE_CREATE, COURSE_EDIT, USER_VIEW, etc.

  private String description;

  @ToString.Exclude  // Prevent toString recursion
  @EqualsAndHashCode.Exclude  // Exclude from equals/hashCode
  @ManyToMany(mappedBy = "permissions")
  private Set<Role> roles;
}
