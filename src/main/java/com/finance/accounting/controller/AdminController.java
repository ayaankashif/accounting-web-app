package com.finance.accounting.controller;

import com.finance.accounting.models.AppRole;
import com.finance.accounting.models.Location;
import com.finance.accounting.models.User;
import com.finance.accounting.service.AppRoleService;
import com.finance.accounting.service.LocationService;
import com.finance.accounting.service.UserService;
import com.finance.accounting.web.TenantSession;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final AppRoleService appRoleService;
    private final LocationService locationService;

    private static final DateTimeFormatter HEADER_DATE =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    public AdminController(
            UserService userService, AppRoleService appRoleService, LocationService locationService) {
        this.userService = userService;
        this.appRoleService = appRoleService;
        this.locationService = locationService;
    }

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
    public String users(Model model, HttpSession session) {
        addShell(model, "Users", "users", true, false);
        Long tenantId = (Long) session.getAttribute(TenantSession.TENANT_ID);
        List<User> users =
                tenantId != null
                        ? userService.findAll(tenantId)
                        : Collections.emptyList();
        List<AppRole> roles =
                tenantId != null
                        ? appRoleService.findAll(tenantId)
                        : Collections.emptyList();
        List<Location> locations =
                tenantId != null
                        ? locationService.findAll(tenantId)
                        : Collections.emptyList();
        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        model.addAttribute("locations", locations);
        return "admin/users";
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
