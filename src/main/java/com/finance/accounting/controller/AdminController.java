package com.finance.accounting.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final DateTimeFormatter HEADER_DATE =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    @GetMapping({"", "/"})
    public String adminRoot() {
        return "redirect:/admin/home";
    }

    @GetMapping("/home")
    public String home(Model model) {
        addShell(model, "Home", "home", false, false);
        LocalDate today = LocalDate.now();
        model.addAttribute("headerDate", today.format(HEADER_DATE));
        model.addAttribute("financialYearLabel", "2015–2016");
        model.addAttribute("workingDate", today);
        return "admin/home";
    }

    @GetMapping("/setup/users")
    public String users(Model model) {
        addShell(model, "Users", "users", true, false);
        return "admin/users";
    }

    @GetMapping("/setup/user-locations")
    public String userLocations(Model model) {
        addShell(model, "User Locations", "user-locations", true, false);
        return "admin/user-locations";
    }

    @GetMapping("/setup/user-roles")
    public String userRoles(Model model) {
        addShell(model, "User Roles", "user-roles", true, false);
        return "admin/user-roles";
    }

    @GetMapping("/security/user-privileges")
    public String userPrivileges(Model model) {
        addShell(model, "User Privileges", "user-privileges", false, true);
        return "admin/user-privileges";
    }

    private static void addShell(
            Model model,
            String pageTitle,
            String activeNav,
            boolean openSetup,
            boolean openSecurity) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("activeNav", activeNav);
        model.addAttribute("openSetup", openSetup);
        model.addAttribute("openSecurity", openSecurity);
    }
}
