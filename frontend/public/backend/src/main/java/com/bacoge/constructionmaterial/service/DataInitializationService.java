package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Créer l'utilisateur admin par défaut s'il n'existe pas déjà
        if (!userRepository.existsByEmail("Admin@root.com")) {
            User adminUser = new User();
            adminUser.setFirstName("Admin");
            adminUser.setLastName("System");
            adminUser.setEmail("Admin@root.com");
            adminUser.setPassword(passwordEncoder.encode("123456789"));
            adminUser.setRole(User.UserRole.ADMIN);
            adminUser.setStatus(User.UserStatus.ACTIVE);
            adminUser.setPhoneNumber("+33 1 23 45 67 89");
            adminUser.setAddress("123 Rue de l'Administration, 75000 Paris");
            
            userRepository.save(adminUser);
            
            System.out.println("Utilisateur admin créé avec succès!");
        }
    }
}