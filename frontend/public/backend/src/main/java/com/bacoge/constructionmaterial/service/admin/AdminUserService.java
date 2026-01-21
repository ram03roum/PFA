package com.bacoge.constructionmaterial.service.admin;

import com.bacoge.constructionmaterial.dto.admin.CreateUserRequest;
import com.bacoge.constructionmaterial.dto.admin.UserDto;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserService {
    
    private final UserRepository userRepository;
    
    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public Page<UserDto> getAllUsers(String firstName, String lastName, String email, 
                                    User.UserRole role, User.UserStatus status, Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToDto);
    }
    
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToDto(user);
    }
    
    public UserDto createUser(CreateUserRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // Should be encoded in real implementation
        user.setPhoneNumber(request.getPhoneNumber());
        user.setCountry(request.getCountry());
        user.setCity(request.getCity());
        user.setAddress(request.getAddress());
        user.setBirthDate(request.getBirthDate());
        if (request.getGender() != null && !request.getGender().isEmpty()) {
            user.setGender(User.Gender.valueOf(request.getGender().toUpperCase()));
        }
        user.setRole(User.UserRole.valueOf(request.getRole()));
        user.setStatus(User.UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
    
    public UserDto updateUser(Long id, CreateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(request.getPassword()); // Should be encoded in real implementation
        }
        user.setPhoneNumber(request.getPhoneNumber());
        user.setCountry(request.getCountry());
        user.setCity(request.getCity());
        user.setAddress(request.getAddress());
        user.setBirthDate(request.getBirthDate());
        if (request.getGender() != null && !request.getGender().isEmpty()) {
            user.setGender(User.Gender.valueOf(request.getGender().toUpperCase()));
        }
        user.setRole(User.UserRole.valueOf(request.getRole()));
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
    
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
    
    public UserDto updateUserStatus(Long id, User.UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
    
    public List<UserDto> getUsersByRole(User.UserRole role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == role)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<UserDto> getUsersByStatus(User.UserStatus status) {
        return userRepository.findAll().stream()
                .filter(user -> user.getStatus() == status)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public long getTotalUsers() {
        return userRepository.count();
    }
    
    public long getUsersCountByRole(User.UserRole role) {
        return userRepository.countByRole().stream()
                .filter(row -> row[0] == role)
                .map(row -> (Long) row[1])
                .findFirst()
                .orElse(0L);
    }
    
    public long getUsersCountByStatus(User.UserStatus status) {
        return userRepository.countByStatus().stream()
                .filter(row -> row[0] == status)
                .map(row -> (Long) row[1])
                .findFirst()
                .orElse(0L);
    }
    
    public List<UserDto> searchUsers(String firstName, String lastName, String email, 
                                    User.UserRole role, User.UserStatus status) {
        return userRepository.findAll().stream()
                .filter(user -> firstName == null || user.getFirstName().toLowerCase().contains(firstName.toLowerCase()))
                .filter(user -> lastName == null || user.getLastName().toLowerCase().contains(lastName.toLowerCase()))
                .filter(user -> email == null || user.getEmail().toLowerCase().contains(email.toLowerCase()))
                .filter(user -> role == null || user.getRole() == role)
                .filter(user -> status == null || user.getStatus() == status)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setCountry(user.getCountry());
        dto.setCity(user.getCity());
        dto.setAddress(user.getAddress());
        dto.setBirthDate(user.getBirthDate());
        dto.setGender(user.getGender() != null ? user.getGender().name() : null);
        dto.setRole(user.getRole().name());
        dto.setStatus(user.getStatus().name());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
    
    public Long getUsersCountBetweenDates(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return userRepository.countByCreatedAtBetween(startDate, endDate);
    }

    public java.util.Map<String, Long> getUsersByRole() {
        return userRepository.countByRole().stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> ((User.UserRole) row[0]).name(),
                        row -> (Long) row[1]
                ));
    }

    public java.util.Map<String, Long> getUsersByCity() {
        return userRepository.countByCity().stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    public java.util.List<com.bacoge.constructionmaterial.dto.UserResponse> getTopActiveUsersBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, int limit) {
        return userRepository.findRecentUsersBetween(startDate, endDate).stream()
                .limit(limit)
                .map(com.bacoge.constructionmaterial.dto.UserResponse::new)
                .collect(java.util.stream.Collectors.toList());
    }
    
    public static class UserStatsDto {
        private Long totalUsers;
        private Long activeUsers;
        private Long inactiveUsers;
        private Long newUsersThisMonth;
        
        public UserStatsDto() {}
        
        public UserStatsDto(Long totalUsers, Long activeUsers, Long inactiveUsers, Long newUsersThisMonth) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.inactiveUsers = inactiveUsers;
            this.newUsersThisMonth = newUsersThisMonth;
        }
        
        // Getters and setters
        public Long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
        
        public Long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(Long activeUsers) { this.activeUsers = activeUsers; }
        
        public Long getInactiveUsers() { return inactiveUsers; }
        public void setInactiveUsers(Long inactiveUsers) { this.inactiveUsers = inactiveUsers; }
        
        public Long getNewUsersThisMonth() { return newUsersThisMonth; }
        public void setNewUsersThisMonth(Long newUsersThisMonth) { this.newUsersThisMonth = newUsersThisMonth; }
    }
}
