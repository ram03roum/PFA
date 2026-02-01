

package com.bacoge.constructionmaterial.config;

import com.bacoge.constructionmaterial.model.Category;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.repository.CategoryRepository;
import com.bacoge.constructionmaterial.repository.ProductRepository;
import com.bacoge.constructionmaterial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
//@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        try {
            System.out.println("Initializing application data...");
            
            if (userRepository.count() == 0) {
                initializeUsers();
            } else {
                System.out.println("Users already initialized.");
            }
            
            if (categoryRepository.count() == 0) {
                initializeCategories();
            } else {
                System.out.println("Categories already initialized.");
            }
            
            if (productRepository.count() == 0) {
                initializeProducts();
            } else {
                System.out.println("Products already initialized.");
            }
            
            System.out.println("Data initialization completed successfully!");
        } catch (Exception e) {
            System.err.println("Error during data initialization: " + e.getMessage());
            e.printStackTrace();
            // Don't throw the exception to prevent application startup failure
        }
    }
    
    private void initializeUsers() {
        // Créer un utilisateur admin
        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("Bacoge");
        admin.setEmail("admin@bacoge.fr");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(User.UserRole.ADMIN);
        admin.setStatus(User.UserStatus.ACTIVE);
        admin.setPhoneNumber("+33 1 23 45 67 89");
        admin.setAddress("123 Rue de la Construction, 75001 Paris");
        userRepository.save(admin);
        
        // Créer un utilisateur client
        User client = new User();
        client.setFirstName("Jean");
        client.setLastName("Dupont");
        client.setEmail("client@example.com");
        client.setPassword(passwordEncoder.encode("client123"));
        client.setRole(User.UserRole.CLIENT);
        client.setStatus(User.UserStatus.ACTIVE);
        client.setPhoneNumber("+33 6 12 34 56 78");
        client.setAddress("456 Avenue des Matériaux, 75002 Paris");
        userRepository.save(client);
        
        System.out.println("Utilisateurs initialisés avec succès");
    }
    
    private void initializeCategories() {
        List<Category> categories = Arrays.asList(
            createCategory("Ciment et Mortier", "Matériaux de base pour la construction", "/images/categories/ciment.jpg"),
            createCategory("Briques et Blocs", "Éléments de maçonnerie", "/images/categories/briques.jpg"),
            createCategory("Acier et Ferraillage", "Armatures et structures métalliques", "/images/categories/acier.jpg"),
            createCategory("Isolation", "Matériaux d'isolation thermique et phonique", "/images/categories/isolation.jpg"),
            createCategory("Plomberie", "Tuyaux, raccords et accessoires", "/images/categories/plomberie.jpg"),
            createCategory("Électricité", "Câbles, interrupteurs et accessoires", "/images/categories/electricite.jpg"),
            createCategory("Outillage", "Outils manuels et électriques", "/images/categories/outillage.jpg"),
            createCategory("Peinture et Finition", "Peintures, enduits et revêtements", "/images/categories/peinture.jpg")
        );
        
        categoryRepository.saveAll(categories);
        System.out.println("Catégories initialisées avec succès");
    }
    
    private Category createCategory(String name, String description, String imageUrl) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setImageUrl(imageUrl);
        category.setStatus(Category.CategoryStatus.ACTIVE);
        return category;
    }
    
    private void initializeProducts() {
        List<Category> categories = categoryRepository.findAll();
        
        // Ciment et Mortier
        Category cimentCategory = categories.stream()
                .filter(c -> c.getName().equals("Ciment et Mortier"))
                .findFirst().orElse(categories.get(0));
        
        createProduct("Ciment Portland 32.5R", "Ciment gris pour tous travaux de maçonnerie", 
                     new BigDecimal("8.50"), 100, cimentCategory, "/images/products/ciment-portland.jpg");
        createProduct("Mortier de Jointoiement", "Mortier prêt à l'emploi pour joints", 
                     new BigDecimal("12.00"), 50, cimentCategory, "/images/products/mortier-joint.jpg");
        
        // Briques et Blocs
        Category briquesCategory = categories.stream()
                .filter(c -> c.getName().equals("Briques et Blocs"))
                .findFirst().orElse(categories.get(0));
        
        createProduct("Briques Creuses 20x20x50", "Briques creuses pour murs porteurs", 
                     new BigDecimal("0.85"), 2000, briquesCategory, "/images/products/briques-creuses.jpg");
        createProduct("Blocs Béton 20x20x50", "Blocs de béton pour construction", 
                     new BigDecimal("1.20"), 1500, briquesCategory, "/images/products/blocs-beton.jpg");
        
        // Acier et Ferraillage
        Category acierCategory = categories.stream()
                .filter(c -> c.getName().equals("Acier et Ferraillage"))
                .findFirst().orElse(categories.get(0));
        
        createProduct("Tiges Acier HA6", "Tiges d'acier haute adhérence 6mm", 
                     new BigDecimal("2.50"), 500, acierCategory, "/images/products/tiges-acier-6.jpg");
        createProduct("Treillis Soudé 150x150", "Treillis soudé pour dalles", 
                     new BigDecimal("15.00"), 100, acierCategory, "/images/products/treillis-soude.jpg");
        
        // Isolation
        Category isolationCategory = categories.stream()
                .filter(c -> c.getName().equals("Isolation"))
                .findFirst().orElse(categories.get(0));
        
        createProduct("Laine de Verre 100mm", "Rouleau isolation thermique", 
                     new BigDecimal("25.00"), 80, isolationCategory, "/images/products/laine-verre.jpg");
        createProduct("Plaque Polystyrène 60mm", "Plaque isolation extérieure", 
                     new BigDecimal("18.50"), 120, isolationCategory, "/images/products/polystyrene.jpg");
        
        // Plomberie
        Category plomberieCategory = categories.stream()
                .filter(c -> c.getName().equals("Plomberie"))
                .findFirst().orElse(categories.get(0));
        
        createProduct("Tuyau PVC 100mm", "Tuyau PVC évacuation 3m", 
                     new BigDecimal("12.50"), 200, plomberieCategory, "/images/products/tuyau-pvc.jpg");
        createProduct("Raccords Cuivre 15mm", "Lot raccords cuivre 10 pièces", 
                     new BigDecimal("8.75"), 150, plomberieCategory, "/images/products/raccords-cuivre.jpg");
        
        // Électricité
        Category electriciteCategory = categories.stream()
                .filter(c -> c.getName().equals("Électricité"))
                .findFirst().orElse(categories.get(0));
        
        createProduct("Câble Électrique 2.5mm²", "Rouleau câble 100m", 
                     new BigDecimal("45.00"), 50, electriciteCategory, "/images/products/cable-electrique.jpg");
        createProduct("Interrupteur Simple", "Interrupteur simple allumage", 
                     new BigDecimal("3.50"), 300, electriciteCategory, "/images/products/interrupteur.jpg");
        
        // Outillage
        Category outillageCategory = categories.stream()
                .filter(c -> c.getName().equals("Outillage"))
                .findFirst().orElse(categories.get(0));
        
        createProduct("Marteau 1kg", "Marteau de maçon 1kg", 
                     new BigDecimal("15.00"), 100, outillageCategory, "/images/products/marteau.jpg");
        createProduct("Truelle 14cm", "Truelle de maçon en acier", 
                     new BigDecimal("8.50"), 150, outillageCategory, "/images/products/truelle.jpg");
        
        // Peinture et Finition
        Category peintureCategory = categories.stream()
                .filter(c -> c.getName().equals("Peinture et Finition"))
                .findFirst().orElse(categories.get(0));
        
        createProduct("Peinture Mur 5L", "Peinture acrylique blanche 5L", 
                     new BigDecimal("28.00"), 80, peintureCategory, "/images/products/peinture-mur.jpg");
        createProduct("Enduit de Lissage", "Enduit de finition 25kg", 
                     new BigDecimal("12.50"), 120, peintureCategory, "/images/products/enduit-lissage.jpg");
        
        System.out.println("Produits initialisés avec succès");
    }
    
    private void createProduct(String name, String description, BigDecimal price, 
                             Integer stockQuantity, Category category, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        product.setSku(generateSku(name));
        product.setWeightKg(new BigDecimal("0.5"));
        product.setDimensions("20x20x50 cm");
        product.setBrand("Bacoge");
        product.setStatus(Product.ProductStatus.ACTIVE);
        productRepository.save(product);
    }
    
    private String generateSku(String name) {
        return "BG-" + name.substring(0, Math.min(3, name.length())).toUpperCase() + 
               "-" + System.currentTimeMillis() % 10000;
    }
}