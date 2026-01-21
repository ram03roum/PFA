package com.bacoge.constructionmaterial.controller.admin;

import com.bacoge.constructionmaterial.dto.admin.StockMovementDto;
import com.bacoge.constructionmaterial.model.StockMovement;
import com.bacoge.constructionmaterial.service.admin.AdminStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stock")
public class AdminStockController {

    private final AdminStockService adminStockService;

    @Autowired
    public AdminStockController(AdminStockService adminStockService) {
        this.adminStockService = adminStockService;
    }

    /**
     * Adjust stock for a product
     */
    @PostMapping("/adjust")
    public ResponseEntity<StockMovementDto> adjustStock(@RequestBody Map<String, Object> request) {
        Long productId = Long.valueOf(request.get("productId").toString());
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        String type = request.get("type").toString();
        String reason = request.get("reason").toString();
        String notes = request.get("notes") != null ? request.get("notes").toString() : null;
        
        // Get current user ID from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            // In a real application, you would get the user ID from the authentication principal
            // This is a simplified example
            userId = 1L; // Default admin user ID
        }
        
        StockMovement.MovementType movementType;
        switch (type) {
            case "entry":
                movementType = StockMovement.MovementType.IN;
                break;
            case "exit":
                movementType = StockMovement.MovementType.OUT;
                break;
            case "adjustment":
                movementType = StockMovement.MovementType.ADJUSTMENT;
                break;
            default:
                return ResponseEntity.badRequest().build();
        }
        
        StockMovementDto result = adminStockService.recordStockMovement(
                productId, quantity, movementType, reason, userId, notes);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Get recent stock movements
     */
    @GetMapping("/movements/recent")
    public ResponseEntity<List<StockMovementDto>> getRecentMovements(
            @RequestParam(defaultValue = "10") int limit) {
        List<StockMovementDto> movements = adminStockService.getRecentStockMovements(limit);
        return ResponseEntity.ok(movements);
    }

    /**
     * Get stock movements for a specific product
     */
    @GetMapping("/movements/product/{productId}")
    public ResponseEntity<List<StockMovementDto>> getProductMovements(
            @PathVariable Long productId) {
        List<StockMovementDto> movements = adminStockService.getProductStockMovements(productId);
        return ResponseEntity.ok(movements);
    }
}
