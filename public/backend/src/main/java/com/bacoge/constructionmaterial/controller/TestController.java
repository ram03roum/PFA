package com.bacoge.constructionmaterial.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    
    @GetMapping("/test")
    public String test(Model model) {
        model.addAttribute("message", "Test page works!");
        return "client/test";
    }
    
    @GetMapping("/simple")
    public String simple() {
        return "client/test";
    }
}
