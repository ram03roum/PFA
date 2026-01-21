package com.bacoge.constructionmaterial.service.admin;

import com.bacoge.constructionmaterial.dto.admin.StockMovementDto;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.model.StockMovement;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.repository.ProductRepository;
import com.bacoge.constructionmaterial.repository.StockMovementRepository;
import com.bacoge.constructionmaterial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminStockService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public AdminStockService(StockMovementRepository stockMovementRepository, 
                             ProductRepository productRepository,
                             UserRepository userRepository) {
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Record a stock movement and update product stock
     */
    @Transactional
    public StockMovementDto recordStockMovement(Long productId, Integer quantity, 
                                              StockMovement.MovementType type, 
                                              String reason, Long userId, String notes) {
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
        
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        }
        
        // Calculate new stock level
        Integer currentStock = product.getStockQuantity();
        Integer newStock;
        
        switch (type) {
            case IN:
                newStock = currentStock + quantity;
                break;
            case OUT:
                newStock = currentStock - quantity;
                if (newStock < 0) {
                    throw new IllegalArgumentException("Cannot reduce stock below zero");
                }
                break;
            case ADJUSTMENT:
                newStock = quantity; // For adjustments, quantity is the new stock level
                break;
            case RETURN:
                newStock = currentStock + quantity;
                break;
            default:
                throw new IllegalArgumentException("Invalid movement type");
        }
        
        // Update product stock
        product.setStockQuantity(newStock);
        productRepository.save(product);
        
        // Create stock movement record
        StockMovement movement = new StockMovement();
        movement.setProductId(product.getId());
        movement.setProductName(product.getName());
        movement.setQuantity(quantity);
        movement.setMovementType(type);
        movement.setReason(reason);
        movement.setCreatedAt(LocalDateTime.now());
        
        StockMovement savedMovement = stockMovementRepository.save(movement);
        
        return StockMovementDto.fromStockMovement(savedMovement);
    }

    /**
     * Get recent stock movements
     */
    public List<StockMovementDto> getRecentStockMovements(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return stockMovementRepository.findAll(pageable).stream()
                .map(StockMovementDto::fromStockMovement)
                .collect(Collectors.toList());
    }

    /**
     * Get stock movements for a specific product
     */
    public List<StockMovementDto> getProductStockMovements(Long productId) {
        return stockMovementRepository.findAll().stream()
                .filter(movement -> movement.getProductId().equals(productId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(StockMovementDto::fromStockMovement)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated stock movements for a specific product
     */
    public Page<StockMovementDto> getProductStockMovements(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<StockMovement> allMovements = stockMovementRepository.findAll().stream()
                .filter(movement -> movement.getProductId().equals(productId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allMovements.size());
        List<StockMovement> pageContent = allMovements.subList(start, end);
        
        return new PageImpl<>(
            pageContent.stream().map(StockMovementDto::fromStockMovement).collect(Collectors.toList()),
            pageable,
            allMovements.size()
        );
    }

    /**
     * Get stock movements between two dates
     */
    public List<StockMovementDto> getStockMovementsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return stockMovementRepository.findAll().stream()
                .filter(movement -> movement.getCreatedAt().isAfter(startDate) && movement.getCreatedAt().isBefore(endDate))
                .map(StockMovementDto::fromStockMovement)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated stock movements between two dates
     */
    public Page<StockMovementDto> getStockMovementsBetweenDates(LocalDateTime startDate, LocalDateTime endDate, 
                                                              int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<StockMovement> allMovements = stockMovementRepository.findAll().stream()
                .filter(movement -> movement.getCreatedAt().isAfter(startDate) && movement.getCreatedAt().isBefore(endDate))
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allMovements.size());
        List<StockMovement> pageContent = allMovements.subList(start, end);
        
        return new PageImpl<>(
            pageContent.stream().map(StockMovementDto::fromStockMovement).collect(Collectors.toList()),
            pageable,
            allMovements.size()
        );
    }

    /**
     * Get movements by type
     */
    public List<StockMovementDto> getMovementsByType(StockMovement.MovementType type, int limit) {
        return stockMovementRepository.findAll().stream()
                .filter(movement -> movement.getMovementType() == type)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(limit)
                .map(StockMovementDto::fromStockMovement)
                .collect(Collectors.toList());
    }

    /**
     * Count movements by type
     */
    public long countMovementsByType(StockMovement.MovementType type) {
        return stockMovementRepository.findAll().stream()
                .filter(movement -> movement.getMovementType() == type)
                .count();
    }
}