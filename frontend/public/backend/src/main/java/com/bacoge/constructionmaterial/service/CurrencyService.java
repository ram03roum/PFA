package com.bacoge.constructionmaterial.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class CurrencyService {
    
    private static final String DEFAULT_CURRENCY = "EUR";
    
    public String getDefaultCurrency() {
        return DEFAULT_CURRENCY;
    }
    
    public BigDecimal convertCurrency(BigDecimal amount, String fromCurrency, String toCurrency) {
        // Pour l'instant, retourner le montant tel quel
        // Dans une implémentation complète, on ferait appel à un service de conversion de devises
        return amount;
    }
    
    public BigDecimal getExchangeRate(String from, String to) {
        // Placeholder implementation - return 1.0 for same currency, mock rates for others
        if (from.equals(to)) {
            return BigDecimal.ONE;
        }
        // Mock exchange rates
        return BigDecimal.valueOf(1.2);
    }
    
    public Map<String, String> getSupportedCurrencies() {
        Map<String, String> currencies = new HashMap<>();
        currencies.put("EUR", "Euro");
        currencies.put("USD", "US Dollar");
        currencies.put("GBP", "British Pound");
        currencies.put("CAD", "Canadian Dollar");
        return currencies;
    }
}