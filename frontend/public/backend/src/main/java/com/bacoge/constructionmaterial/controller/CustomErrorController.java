package com.bacoge.constructionmaterial.controller;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Controller
public class CustomErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/custom-error")
    public String handleError(WebRequest webRequest, Model model) {
        // Get error attributes
        Map<String, Object> errorAttributes = this.errorAttributes.getErrorAttributes(
            webRequest,
            ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE)
        );
        
        // Get error status
        int statusCode = (int) errorAttributes.getOrDefault("status", 500);
        
        // Default error message
        String errorMessage = (String) errorAttributes.getOrDefault("message", "An unexpected error occurred");
        String errorTitle = (String) errorAttributes.getOrDefault("error", "Error");
        
        // Set error message based on status code
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            errorTitle = "Page Not Found";
            errorMessage = "The page you're looking for doesn't exist or has been moved.";
        } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
            errorTitle = "Access Denied";
            errorMessage = "You don't have permission to access this resource.";
        } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            errorTitle = "Server Error";
            errorMessage = "Something went wrong on our end. We're working to fix it.";
        } else if (statusCode == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            errorTitle = "Service Unavailable";
            errorMessage = "We're currently down for maintenance. Please check back soon.";
        }
        
        // Add error attributes to the model
        model.addAttribute("status", statusCode);
        model.addAttribute("errorTitle", errorTitle);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("timestamp", errorAttributes.get("timestamp"));
        model.addAttribute("path", errorAttributes.get("path"));
        
        // Return the appropriate error page
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            return "error/404";
        } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
            return "error/403";
        } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return "error/500";
        } else if (statusCode == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            return "error/maintenance";
        } else {
            return "error/generic";
        }
    }

}
