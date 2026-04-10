package com.finance.accounting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import com.finance.accounting.models.User;
import com.finance.accounting.service.UserService;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/admin/setup/users")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        userService.save(user);
        model.addAttribute("success", true);
        return "redirect:/admin/setup/users";
    }

    @GetMapping("/admin/setup/users/{id}")
    public String users(@RequestParam("id") Long id, Model model) {
        model.addAttribute("users", userService.findAll(id));
        return "admin/users";
    }

    /** Legacy URL; use {@code /admin/setup/users}. */
    @Deprecated
    @GetMapping("/users")
    public String legacyUsers() {
        return "redirect:/admin/setup/users";
    }
}
