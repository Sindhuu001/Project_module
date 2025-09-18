// package com.example.projectmanagement.service;

// import com.example.projectmanagement.dto.UserDto;
// import com.example.projectmanagement.entity.User;
// import com.example.projectmanagement.repository.UserRepository;
// import org.modelmapper.ModelMapper;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.List;
// import java.util.stream.Collectors;

// @Service
// @Transactional
// public class UserService {

//     @Autowired
//     private UserRepository userRepository;

//     @Autowired
//     private ModelMapper modelMapper;

//     public UserDto createUser(UserDto userDto) {
//         if (userRepository.existsByEmail(userDto.getEmail())) {
//             throw new RuntimeException("User with email " + userDto.getEmail() + " already exists");
//         }

//         User user = modelMapper.map(userDto, User.class);
//         User savedUser = userRepository.save(user);
//         return modelMapper.map(savedUser, UserDto.class);
//     }

//     @Transactional(readOnly = true)
//     public UserDto getUserById(Long id) {
//         User user = userRepository.findById(id)
//                 .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
//         return modelMapper.map(user, UserDto.class);
//     }

//     @Transactional(readOnly = true)
//     public UserDto getUserByEmail(String email) {
//         User user = userRepository.findByEmail(email)
//                 .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
//         return modelMapper.map(user, UserDto.class);
//     }

//     @Transactional(readOnly = true)
//     public User findByEmail(String email) {
//         return userRepository.findByEmail(email)
//                 .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
//     }

//     @Transactional(readOnly = true)
//     public List<UserDto> getAllUsers() {
//         return userRepository.findAll().stream()
//                 .map(user -> modelMapper.map(user, UserDto.class))
//                 .collect(Collectors.toList());
//     }

//     @Transactional(readOnly = true)
//     public Page<UserDto> getAllUsers(Pageable pageable) {
//         return userRepository.findAll(pageable)
//                 .map(user -> modelMapper.map(user, UserDto.class));
//     }

//     @Transactional(readOnly = true)
//     public List<UserDto> getUsersByRole(User.UserRole role) {
//         return userRepository.findByRole(role).stream()
//                 .map(user -> modelMapper.map(user, UserDto.class))
//                 .collect(Collectors.toList());
//     }

//     // @Transactional(readOnly = true)
//     // public List<UserDto> getUsersByProject(Long projectId) {
//     //     return userRepository.findByProjectId(projectId).stream()
//     //             .map(user -> modelMapper.map(user, UserDto.class))
//     //             .collect(Collectors.toList());
//     // }

//     public UserDto updateUser(Long id, UserDto userDto) {
//         User user = userRepository.findById(id)
//                 .orElseThrow(() -> new RuntimeException("User not found"));

//         user.setName(userDto.getName());
//         user.setEmail(userDto.getEmail());
//         // user.setRole(userDto.getRole()); // ✅ FIXED: use setRole instead of setRoles

//         User updatedUser = userRepository.save(user);
//         return modelMapper.map(updatedUser, UserDto.class);
//     }

//     public void deleteUser(Long id) {
//         if (!userRepository.existsById(id)) {
//             throw new RuntimeException("User not found with id: " + id);
//         }
//         userRepository.deleteById(id);
//     }

//     @Transactional(readOnly = true)
//     public Page<UserDto> searchUsers(String name, User.UserRole role, Pageable pageable) {
//         if (name != null && role != null) {
//             return userRepository.findByNameContaining(name, pageable)
//                     .map(user -> modelMapper.map(user, UserDto.class));
//         } else if (name != null) {
//             return userRepository.findByNameContaining(name, pageable)
//                     .map(user -> modelMapper.map(user, UserDto.class));
//         } else if (role != null) {
//             return userRepository.findByRole(role, pageable)
//                     .map(user -> modelMapper.map(user, UserDto.class));
//         } else {
//             return userRepository.findAll(pageable)
//                     .map(user -> modelMapper.map(user, UserDto.class));
//         }
//     }
// }
