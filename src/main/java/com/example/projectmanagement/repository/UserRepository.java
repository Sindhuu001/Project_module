// package com.example.projectmanagement.repository;

// import com.example.projectmanagement.entity.User;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// import java.util.List;
// import java.util.Optional;

// @Repository
// public interface UserRepository extends JpaRepository<User, Long> {
    
//     Optional<User> findByEmail(String email);
    
//     List<User> findByRole(User.UserRole role);
    
//     @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
//     Page<User> findByNameContaining(@Param("name") String name, Pageable pageable);
    
//     @Query("SELECT u FROM User u WHERE u.role = :role")
//     Page<User> findByRole(@Param("role") User.UserRole role, Pageable pageable);
    
//     // @Query("SELECT u FROM User u JOIN u.projects p WHERE p.id = :projectId")
//     // List<User> findByProjectId(@Param("projectId") Long projectId);
    
//     boolean existsByEmail(String email);
// }