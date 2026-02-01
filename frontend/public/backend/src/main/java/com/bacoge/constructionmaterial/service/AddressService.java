package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.dto.AddressDto;
import com.bacoge.constructionmaterial.entity.Address;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.repository.AddressRepository;
import com.bacoge.constructionmaterial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AddressService {
    
    private static final Logger logger = LoggerFactory.getLogger(AddressService.class);
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Récupère toutes les adresses d'un utilisateur
     */
    public List<AddressDto> getUserAddresses(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        List<Address> addresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
        
        // S'il n'y a pas d'adresse par défaut, définir la première comme telle
        if (!addresses.isEmpty() && addresses.stream().noneMatch(Address::isDefault)) {
            Address firstAddress = addresses.get(0);
            firstAddress.setDefault(true);
            addressRepository.save(firstAddress);
        }
        
        return addresses.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Crée une nouvelle adresse pour un utilisateur
     */
    public AddressDto createAddress(String email, AddressDto addressDto) {
        logger.info("=== DEBUG SERVICE ADRESSE ===");
        logger.info("Création d'adresse pour l'email: {}", email);
        logger.info("AddressDto reçu dans le service: name='{}', street='{}', city='{}', postalCode='{}', country='{}', type='{}', isDefault={}", 
            addressDto.getName(), addressDto.getStreet(), addressDto.getCity(), 
            addressDto.getPostalCode(), addressDto.getCountry(), addressDto.getType(), addressDto.isDefault());
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        logger.info("Utilisateur trouvé: ID={}, Email={}", user.getId(), user.getEmail());
        
        Address address = new Address();
        address.setName(addressDto.getName());
        address.setStreet(addressDto.getStreet());
        address.setCity(addressDto.getCity());
        address.setPostalCode(addressDto.getPostalCode());
        address.setCountry(addressDto.getCountry());
        address.setType(Address.AddressType.valueOf(addressDto.getType()));
        address.setUser(user);
        
        logger.info("Objet Address créé: name='{}', street='{}', city='{}', postalCode='{}', country='{}', type='{}'", 
            address.getName(), address.getStreet(), address.getCity(), 
            address.getPostalCode(), address.getCountry(), address.getType());
        
        // Si c'est la première adresse ou si elle est marquée comme par défaut
        long addressCount = addressRepository.countByUser(user);
        logger.info("Nombre d'adresses existantes pour l'utilisateur: {}", addressCount);
        // Limiter à 3 adresses maximum
        if (addressCount >= 3) {
            logger.warn("Tentative d'ajout d'une 4ème adresse pour l'utilisateur {} - refusée", user.getEmail());
            throw new IllegalStateException("Nombre maximum d'adresses atteint (3)");
        }
        
        if (addressCount == 0 || addressDto.isDefault()) {
            // Supprimer le statut par défaut des autres adresses
            addressRepository.clearDefaultAddresses(user);
            address.setDefault(true);
            logger.info("Adresse définie comme adresse par défaut");
        } else {
            address.setDefault(false);
            logger.info("Adresse définie comme adresse non par défaut");
        }
        
        logger.info("Tentative de sauvegarde de l'adresse en base de données...");
        Address savedAddress = addressRepository.save(address);
        logger.info("Adresse sauvegardée avec succès: ID={}", savedAddress.getId());
        
        return convertToDto(savedAddress);
    }
    
    /**
     * Met à jour une adresse existante
     */
    public AddressDto updateAddress(String email, Long addressId, AddressDto addressDto) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Address address = addressRepository.findByIdAndUser(addressId, user)
            .orElseThrow(() -> new RuntimeException("Adresse non trouvée"));
        
        address.setName(addressDto.getName());
        address.setStreet(addressDto.getStreet());
        address.setCity(addressDto.getCity());
        address.setPostalCode(addressDto.getPostalCode());
        address.setCountry(addressDto.getCountry());
        address.setType(Address.AddressType.valueOf(addressDto.getType()));
        
        // Gérer le statut par défaut
        if (addressDto.isDefault() && !address.isDefault()) {
            addressRepository.clearDefaultAddresses(user);
            address.setDefault(true);
        }
        
        Address savedAddress = addressRepository.save(address);
        return convertToDto(savedAddress);
    }
    
    /**
     * Supprime une adresse
     */
    public void deleteAddress(String email, Long addressId) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Address address = addressRepository.findByIdAndUser(addressId, user)
            .orElseThrow(() -> new RuntimeException("Adresse non trouvée"));
        
        boolean wasDefault = address.isDefault();
        addressRepository.delete(address);
        
        // Si l'adresse supprimée était par défaut, définir une autre comme par défaut
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setDefault(true);
                addressRepository.save(newDefault);
            }
        }
    }
    
    /**
     * Définit une adresse comme adresse par défaut
     */
    public AddressDto setDefaultAddress(String email, Long addressId) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Address address = addressRepository.findByIdAndUser(addressId, user)
            .orElseThrow(() -> new RuntimeException("Adresse non trouvée"));
        
        // Supprimer le statut par défaut des autres adresses
        addressRepository.clearDefaultAddresses(user);
        
        // Définir cette adresse comme par défaut
        address.setDefault(true);
        Address savedAddress = addressRepository.save(address);
        
        return convertToDto(savedAddress);
    }
    
    /**
     * Convertit une entité Address en DTO
     */
    private AddressDto convertToDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setName(address.getName());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        dto.setType(address.getType().name());
        dto.setDefault(address.isDefault());
        return dto;
    }
}