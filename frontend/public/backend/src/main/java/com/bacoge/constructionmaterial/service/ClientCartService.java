package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.dto.client.AddToCartRequest;
import com.bacoge.constructionmaterial.dto.client.CartDto;
import com.bacoge.constructionmaterial.dto.client.CartItemDto;
import com.bacoge.constructionmaterial.dto.client.ProductDisplayDto;
import com.bacoge.constructionmaterial.dto.client.UpdateCartItemRequest;
import com.bacoge.constructionmaterial.model.Cart;
import com.bacoge.constructionmaterial.model.CartItem;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.repository.CartItemRepository;
import com.bacoge.constructionmaterial.repository.CartRepository;
import com.bacoge.constructionmaterial.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientCartService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public ClientCartService(ProductRepository productRepository,
                             CartRepository cartRepository,
                             CartItemRepository cartItemRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart c = new Cart();
            c.setUserId(userId);
            c.setTotalAmount(BigDecimal.ZERO);
            return cartRepository.save(c);
        });
    }

    private void recomputeCartTotals(Cart cart) {
        BigDecimal total = BigDecimal.ZERO;
        if (cart.getItems() != null) {
            for (CartItem it : cart.getItems()) {
                if (it.getTotalPrice() != null) {
                    total = total.add(it.getTotalPrice());
                } else if (it.getUnitPrice() != null && it.getQuantity() != null) {
                    total = total.add(it.getUnitPrice().multiply(BigDecimal.valueOf(it.getQuantity())));
                }
            }
        }
        cart.setTotalAmount(total);
    }

    public List<Map<String, Object>> getCartItems() { return new ArrayList<>(); }

    public List<Map<String, Object>> getCartItems(Long userId) { return new ArrayList<>(); }

    public BigDecimal getCartTotal() { return BigDecimal.ZERO; }

    public BigDecimal getCartTotal(Long userId) { return BigDecimal.ZERO; }

    public int getCartItemCount() { return 0; }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public int getCartItemCount(Long userId) {
        if (userId == null) return 0;
        return cartRepository.findByUserId(userId)
                .map(c -> {
                    // Éviter LazyInitializationException en chargeant via le repository
                    java.util.List<com.bacoge.constructionmaterial.model.CartItem> items = cartItemRepository.findByCartId(c.getId());
                    return items.stream().mapToInt(ci -> Optional.ofNullable(ci.getQuantity()).orElse(0)).sum();
                })
                .orElse(0);
    }

    public boolean validateCartForOrder(Long userId) { return true; }

    @Transactional
    public void addToCart(Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) throw new RuntimeException("La quantité doit être positive");
        Product productEntity = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        if (productEntity.getStockQuantity() < quantity) throw new RuntimeException("Stock insuffisant. Stock disponible: " + productEntity.getStockQuantity());
    }

    @Transactional
    public CartItemDto addToCart(Long userId, AddToCartRequest request) {
        if (userId == null) throw new IllegalArgumentException("userId null");
        if (request.getQuantity() == null || request.getQuantity() <= 0) throw new RuntimeException("Quantité invalide");

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Stock insuffisant pour le produit: " + product.getName());
        }

        Cart cart = getOrCreateCart(userId);

        // Chercher un item existant pour ce produit
        Optional<CartItem> existingOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());
        CartItem item;
        if (existingOpt.isPresent()) {
            item = existingOpt.get();
            int newQty = Optional.ofNullable(item.getQuantity()).orElse(0) + request.getQuantity();
            if (product.getStockQuantity() < newQty) {
                throw new RuntimeException("Stock insuffisant pour le produit: " + product.getName());
            }
            item.setQuantity(newQty);
        } else {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(request.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setDiscountedPrice(product.getDiscountedPrice());
        }
        // Persister l'item
        item = cartItemRepository.save(item);

        // Rafraîchir la relation items du cart pour recalculer le total
        if (!cart.getItems().contains(item)) {
            cart.getItems().add(item);
        }
        recomputeCartTotals(cart);
        cartRepository.save(cart);

        // Construire le DTO
        CartItemDto dto = new CartItemDto();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        // Recharger le produit avec images pour DTO
        Product fullProduct = productRepository.findByIdWithPromotions(product.getId()).orElse(product);
        ProductDisplayDto productDto = ProductDisplayDto.fromProduct(fullProduct);
        // Assurer une image principale si absente
        if (productDto.getImageUrl() == null && productDto.getImageUrls() != null && !productDto.getImageUrls().isEmpty()) {
            productDto.setImageUrl(productDto.getImageUrls().get(0));
        }
        dto.setProduct(productDto);
        dto.setUnitPrice(item.getUnitPrice());
        dto.setDiscountedPrice(item.getDiscountedPrice());
        dto.setTotalPrice(item.getTotalPrice());
        return dto;
    }

    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        if (userId == null || productId == null) return;
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).ifPresent(ci -> {
                cart.getItems().remove(ci);
                cartItemRepository.delete(ci);
                recomputeCartTotals(cart);
                cartRepository.save(cart);
            });
        });
    }

    @Transactional
    public void updateCartItemQuantity(Long userId, Long productId, Integer newQuantity) {
        if (userId == null || productId == null) return;
        if (newQuantity == null || newQuantity <= 0) throw new RuntimeException("La quantité doit être positive");
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).ifPresent(ci -> {
                Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Produit non trouvé"));
                if (product.getStockQuantity() < newQuantity) throw new RuntimeException("Stock insuffisant");
                ci.setQuantity(newQuantity);
                cartItemRepository.save(ci);
                recomputeCartTotals(cart);
                cartRepository.save(cart);
            });
        });
    }

    @Transactional
    public void clearCart(Long userId) {
        if (userId == null) throw new IllegalArgumentException("User ID cannot be null");
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            for (CartItem it : new ArrayList<>(cart.getItems())) {
                cartItemRepository.delete(it);
            }
            cart.getItems().clear();
            recomputeCartTotals(cart);
            cartRepository.save(cart);
        });
    }

    @Transactional(readOnly = true)
    public CartDto getCartByUserId(Long userId) {
        CartDto dto = new CartDto();
        dto.setUserId(userId);
        dto.setItems(new ArrayList<>());
        dto.setTotalAmount(BigDecimal.ZERO);
        dto.setTotalItems(0);
        if (userId == null) return dto;

        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isEmpty()) return dto;
        Cart cart = cartOpt.get();
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem it : items) {
            CartItemDto itemDto = new CartItemDto();
            itemDto.setId(it.getId());
            itemDto.setQuantity(it.getQuantity());
            itemDto.setUnitPrice(it.getUnitPrice());
            itemDto.setDiscountedPrice(it.getDiscountedPrice());
            itemDto.setTotalPrice(it.getTotalPrice());
            // Charger produit avec images
            Product full = productRepository.findByIdWithPromotions(it.getProduct().getId()).orElse(it.getProduct());
            ProductDisplayDto pDto = ProductDisplayDto.fromProduct(full);
            if (pDto.getImageUrl() == null && pDto.getImageUrls() != null && !pDto.getImageUrls().isEmpty()) {
                pDto.setImageUrl(pDto.getImageUrls().get(0));
            }
            itemDto.setProduct(pDto);
            dto.getItems().add(itemDto);
            if (itemDto.getTotalPrice() != null) total = total.add(itemDto.getTotalPrice());
        }
        dto.setTotalAmount(total);
        dto.setTotalItems(items.stream().mapToInt(ci -> Optional.ofNullable(ci.getQuantity()).orElse(0)).sum());
        return dto;
    }

    @Transactional
    public CartItemDto updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        if (userId == null) throw new IllegalArgumentException("userId null");
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Panier introuvable"));
        CartItem item = cart.getItems().stream().filter(i -> i.getId().equals(cartItemId)).findFirst()
                .orElseThrow(() -> new RuntimeException("Article du panier non trouvé"));
        Product product = productRepository.findById(item.getProduct().getId()).orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        if (product.getStockQuantity() < request.getQuantity()) throw new RuntimeException("Stock insuffisant");
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        recomputeCartTotals(cart);
        cartRepository.save(cart);
        // Build DTO
        CartItemDto dto = new CartItemDto();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        Product full = productRepository.findByIdWithPromotions(product.getId()).orElse(product);
        ProductDisplayDto pDto = ProductDisplayDto.fromProduct(full);
        if (pDto.getImageUrl() == null && pDto.getImageUrls() != null && !pDto.getImageUrls().isEmpty()) {
            pDto.setImageUrl(pDto.getImageUrls().get(0));
        }
        dto.setProduct(pDto);
        dto.setUnitPrice(item.getUnitPrice());
        dto.setDiscountedPrice(item.getDiscountedPrice());
        dto.setTotalPrice(item.getTotalPrice());
        return dto;
    }

    @Transactional
    public void removeCartItem(Long userId, Long cartItemId) {
        if (userId == null) return;
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().stream().filter(i -> i.getId().equals(cartItemId)).findFirst().ifPresent(ci -> {
                cart.getItems().remove(ci);
                cartItemRepository.delete(ci);
                recomputeCartTotals(cart);
                cartRepository.save(cart);
            });
        });
    }

    @Transactional
    public void mergeGuestItems(Long userId, List<GuestCartService.GuestCartItem> guestItems) {
        if (userId == null || guestItems == null || guestItems.isEmpty()) return;
        for (GuestCartService.GuestCartItem gi : guestItems) {
            AddToCartRequest req = new AddToCartRequest();
            req.setProductId(gi.getProductId());
            req.setQuantity(gi.getQuantity());
            try { addToCart(userId, req); } catch (Exception ignore) {}
        }
    }
}