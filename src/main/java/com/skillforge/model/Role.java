package com.skillforge.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false, unique = true)
  private String name; // ROLE_STUDENT, ROLE_INSTRUCTOR, ROLE_ADMIN

  private String description;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "role_permissions",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  @Builder.Default
  @ToString.Exclude  // Prevent toString recursion
  @EqualsAndHashCode.Exclude  // Exclude from equals/hashCode
  private Set<Permission> permissions = new HashSet<>();

  @ManyToMany(mappedBy = "roles")
  @Builder.Default
  @ToString.Exclude  // Prevent toString recursion
  @EqualsAndHashCode.Exclude  // Exclude from equals/hashCode
  private Set<User> users = new HashSet<>();

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}
