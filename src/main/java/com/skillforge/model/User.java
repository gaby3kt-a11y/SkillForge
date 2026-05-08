// User.java
package com.skillforge.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = "email"),
      @UniqueConstraint(columnNames = "username")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false)
  private String fullName;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(name = "profile_picture_url")
  private String profilePictureUrl;

  @Column(name = "bio", length = 500)
  private String bio;

  @Column(name = "is_active")
  @Builder.Default
  private boolean isActive = true;

  @Column(name = "is_email_verified")
  @Builder.Default
  private boolean isEmailVerified = false;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  @Builder.Default
  @ToString.Exclude  // Prevent toString recursion
  @EqualsAndHashCode.Exclude  // Exclude from equals/hashCode
  private Set<Role> roles = new HashSet<>();

  @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL)
  @Builder.Default
  @ToString.Exclude  // Prevent toString recursion
  @EqualsAndHashCode.Exclude  // Exclude from equals/hashCode
  private Set<Course> taughtCourses = new HashSet<>();

  @ManyToMany(mappedBy = "students")
  @Builder.Default
  @ToString.Exclude  // Prevent toString recursion
  @EqualsAndHashCode.Exclude  // Exclude from equals/hashCode
  private Set<Course> enrolledCourses = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  @Builder.Default
  @ToString.Exclude  // Prevent toString recursion
  @EqualsAndHashCode.Exclude  // Exclude from equals/hashCode
  private Set<Enrollment> enrollments = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  @Builder.Default
  @ToString.Exclude  // Prevent toString recursion
  @EqualsAndHashCode.Exclude  // Exclude from equals/hashCode
  private Set<Review> reviews = new HashSet<>();

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;


  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roles.stream()
        .map(role -> new SimpleGrantedAuthority(role.getName()))
        .collect(Collectors.toSet());
  }

    @Override
  public boolean isEnabled() {
    return isActive;
  }
}
