package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.admin.CreateUserRequest;
import com.bacoge.constructionmaterial.dto.admin.UserDto;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.service.admin.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")

@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    
    private final AdminUserService adminUserService;
    
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        
        Pageable pageable = PageRequest.of(page, size);
        User.UserRole userRole = role != null ? User.UserRole.valueOf(role.toUpperCase()) : null;
        User.UserStatus userStatus = status != null ? User.UserStatus.valueOf(status.toUpperCase()) : null;
        
        Page<UserDto> users = adminUserService.getAllUsers(firstName, lastName, email, userRole, userStatus, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", users.getContent());
        response.put("currentPage", users.getNumber());
        response.put("totalItems", users.getTotalElements());
        response.put("totalPages", users.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = adminUserService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody CreateUserRequest request) {
        try {
            UserDto user = adminUserService.createUser(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("message", "Utilisateur créé avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody CreateUserRequest request) {
        try {
            UserDto user = adminUserService.updateUser(id, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("message", "Utilisateur mis à jour avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        try {
            adminUserService.deleteUser(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Utilisateur supprimé avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        try {
            User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
            UserDto user = adminUserService.updateUserStatus(id, userStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("message", "Statut de l'utilisateur mis à jour avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/by-role/{role}")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable String role) {
        User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
        List<UserDto> users = adminUserService.getUsersByRole(userRole);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<UserDto>> getUsersByStatus(@PathVariable String status) {
        User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
        List<UserDto> users = adminUserService.getUsersByStatus(userStatus);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", adminUserService.getTotalUsers());
        stats.put("adminUsers", adminUserService.getUsersCountByRole(User.UserRole.ADMIN));
        stats.put("clientUsers", adminUserService.getUsersCountByRole(User.UserRole.CLIENT));
        
        return ResponseEntity.ok(stats);
    }
}
