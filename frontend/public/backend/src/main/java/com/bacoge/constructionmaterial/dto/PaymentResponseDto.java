package com.bacoge.constructionmaterial.dto;

import java.math.BigDecimal;
import java.util.Map;

public class PaymentResponseDto {
    private boolean success;
    private String message;
    private String transactionId;
    private Long orderId;
    private String paymentMethod;
    private BigDecimal amount;
    private String redirectUrl;
    private Map<String, String> bankDetails;
    
    // Constructeurs
    public PaymentResponseDto() {}
    
    public PaymentResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    // Méthodes statiques pour créer des réponses
    public static PaymentResponseDto success(String transactionId, Long orderId, String paymentMethod, BigDecimal amount) {
        PaymentResponseDto response = new PaymentResponseDto(true, "Paiement traité avec succès");
        response.setTransactionId(transactionId);
        response.setOrderId(orderId);
        response.setPaymentMethod(paymentMethod);
        response.setAmount(amount);
        return response;
    }
    
    public static PaymentResponseDto failure(String message) {
        return new PaymentResponseDto(false, message);
    }
    
    public static PaymentResponseDto paypalRedirect(String redirectUrl, String paymentId, Long orderId, BigDecimal amount) {
        PaymentResponseDto response = new PaymentResponseDto(true, "Redirection vers PayPal");
        response.setRedirectUrl(redirectUrl);
        response.setTransactionId(paymentId);
        response.setOrderId(orderId);
        response.setPaymentMethod("paypal");
        response.setAmount(amount);
        return response;
    }
    
    public static PaymentResponseDto bankTransfer(String transactionId, Long orderId, BigDecimal amount, Map<String, String> bankDetails) {
        PaymentResponseDto response = new PaymentResponseDto(true, "Détails de virement bancaire générés");
        response.setTransactionId(transactionId);
        response.setOrderId(orderId);
        response.setPaymentMethod("bank_transfer");
        response.setAmount(amount);
        response.setBankDetails(bankDetails);
        return response;
    }
    
    // Getters et Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getRedirectUrl() { return redirectUrl; }
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }
    
    public String getPaypalRedirectUrl() { return redirectUrl; }
    
    public String getErrorMessage() { return success ? null : message; }
    
    public Map<String, String> getBankDetails() { return bankDetails; }
    public void setBankDetails(Map<String, String> bankDetails) { this.bankDetails = bankDetails; }
}
