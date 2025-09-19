package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.dto.UserDto;
// import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.service.TaskService;
import com.example.projectmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173") 
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TaskService taskService;
    
    // @PostMapping
    // public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
    //     UserDto createdUser = userService.createUser(userDto);
    //     return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    // }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String role) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserDto> users = userService.searchUsers(name, role, pageable);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable String role) {
        List<UserDto> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }
    
    // @GetMapping("/project/{projectId}")
    // public ResponseEntity<List<UserDto>> getUsersByProject(@PathVariable Long projectId) {
    //     List<UserDto> users = userService.getUsersByProject(projectId);
    //     return ResponseEntity.ok(users);
    // }
    
    @GetMapping("/{userId}/tasks")
    public ResponseEntity<List<TaskDto>> getUserTasks(@PathVariable Long userId) {
        List<TaskDto> tasks = taskService.getTasksByAssignee(userId);
        return ResponseEntity.ok(tasks);
    }
    
//     @PutMapping("/{id}")
// public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
//     UserDto updated = userService.updateUser(id, userDto);
//     return ResponseEntity.ok(updated);
// }

    
    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    //     userService.deleteUser(id);
    //     return ResponseEntity.noContent().build();
    // }

    
}