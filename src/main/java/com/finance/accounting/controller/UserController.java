package com.finance.accounting.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;


@Controller
public class UserController {
    
    @GetMapping("/users")
    public String page() {
        return "index";
    }
}