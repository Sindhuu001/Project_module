 package com.example.projectmanagement.entity;

// import jakarta.persistence.*;
// import jakarta.validation.constraints.Email;
// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.Size;
// import org.hibernate.annotations.CreationTimestamp;
// import org.hibernate.annotations.UpdateTimestamp;

// import java.time.LocalDateTime;
// import java.util.List;

// @Entity
// @Table(name = "users")
// public class User {
    
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @NotBlank(message = "Name is required")
//     @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
//     @Column(nullable = false)
//     private String name;

//     @Email(message = "Email should be valid")
//     @NotBlank(message = "Email is required")
//     @Column(unique = true, nullable = false)
//     private String email;

//     @Enumerated(EnumType.STRING)
//     @Column(nullable = false)
//     private UserRole role;

//     @OneToMany(mappedBy = "assignee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//     private List<Task> assignedTasks;

//     @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//     private List<Task> reportedTasks;

//     // private List<Project> projects;

//     @CreationTimestamp
//     @Column(name = "created_at", nullable = false, updatable = false)
//     private LocalDateTime createdAt;

//     @UpdateTimestamp
//     @Column(name = "updated_at")
//     private LocalDateTime updatedAt;

    // Enum inside the class
    public enum UserRole {
        ADMIN, DEVELOPER, TESTER, PRODUCT_OWNER, SCRUM_MASTER
    }

//     // Constructors
//     public User() {}

//     public User(String name, String email, UserRole role) {
//         this.name = name;
//         this.email = email;
//         this.role = role;
//     }

//     // Getters and Setters
//     public Long getId() { return id; }
//     public void setId(Long id) { this.id = id; }

//     public String getName() { return name; }
//     public void setName(String name) { this.name = name; }

//     public String getEmail() { return email; }
//     public void setEmail(String email) { this.email = email; }

//     public UserRole getRole() { return role; }
//     public void setRole(UserRole role) { this.role = role; }

//     public List<Task> getAssignedTasks() { return assignedTasks; }
//     public void setAssignedTasks(List<Task> assignedTasks) { this.assignedTasks = assignedTasks; }

//     public List<Task> getReportedTasks() { return reportedTasks; }
//     public void setReportedTasks(List<Task> reportedTasks) { this.reportedTasks = reportedTasks; }

//     // public List<Project> getProjects() { return projects; }
//     // public void setProjects(List<Project> projects) { this.projects = projects; }

//     public LocalDateTime getCreatedAt() { return createdAt; }
//     public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

//     public LocalDateTime getUpdatedAt() { return updatedAt; }
//     public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
// }
