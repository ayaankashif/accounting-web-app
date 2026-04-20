package com.finance.accounting.controller;

import com.finance.accounting.models.UserLocation;
import com.finance.accounting.service.LocationService;
import com.finance.accounting.service.UserLocationService;
import com.finance.accounting.service.UserService;
import com.finance.accounting.web.TenantSession;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/setup/user-locations")
public class UserLocationController {

    private final UserService userService;
    private final LocationService locationService;
    private final UserLocationService userLocationService;

    public UserLocationController(
            UserService userService,
            LocationService locationService,
            UserLocationService userLocationService) {
        this.userService = userService;
        this.locationService = locationService;
        this.userLocationService = userLocationService;
    }

    @GetMapping
    public String page(
            Model model,
            HttpSession session,
            @RequestParam(value = "userId", required = false) Long userId) {
        Long tenantId = (Long) session.getAttribute(TenantSession.TENANT_ID);
        model.addAttribute("pageTitle", "User Location");
        model.addAttribute("activeNav", "user-locations");
        model.addAttribute("openSetup", true);
        model.addAttribute("openSecurity", false);
        model.addAttribute("selectedUserId", userId);

        if (tenantId == null) {
            model.addAttribute("users", Collections.emptyList());
            model.addAttribute("locations", Collections.emptyList());
            model.addAttribute("assignments", Collections.emptyList());
            model.addAttribute("selectedLocationIds", Collections.emptySet());
            model.addAttribute("userLocationByLocation", Collections.emptyMap());
            return "admin/user-locations";
        }

        model.addAttribute("users", userService.findAll(tenantId));
        model.addAttribute("locations", locationService.findAll(tenantId));
        model.addAttribute("assignments", userLocationService.findAllForTenant(tenantId));

        Map<Long, UserLocation> userLocationByLocation;
        if (userId == null) {
            userLocationByLocation = Collections.emptyMap();
        } else {
            var uOpt = userService.findById(tenantId, userId);
            if (uOpt.isEmpty()) {
                model.addAttribute("errorMessage", "User not found.");
                userLocationByLocation = Collections.emptyMap();
            } else {
                model.addAttribute("selectedUser", uOpt.get());
                userLocationByLocation = userLocationService.findAssignmentsByLocationId(tenantId, userId);
            }
        }
        model.addAttribute("userLocationByLocation", userLocationByLocation);
        model.addAttribute("selectedLocationIds", userLocationByLocation.keySet());
        return "admin/user-locations";
    }

    @PostMapping("/assign")
    public String assign(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "locationIds", required = false) List<Long> locationIds,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long tenantId = (Long) session.getAttribute(TenantSession.TENANT_ID);
        try {
            userLocationService.replaceLocationsForUser(
                    tenantId, userId, locationIds != null ? locationIds : List.of());
            redirectAttributes.addFlashAttribute("successMessage", "User locations saved.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", friendlyPersistenceMessage(ex));
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", friendlyPersistenceMessage(ex));
        }
        return "redirect:/admin/setup/user-locations?userId=" + userId;
    }

    @PostMapping("/delete")
    public String delete(
            @RequestParam("id") Long id,
            @RequestParam(value = "userId", required = false) Long userId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long tenantId = (Long) session.getAttribute(TenantSession.TENANT_ID);
        try {
            userLocationService.deleteById(tenantId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Location assignment removed.");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Assignment not found.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", friendlyPersistenceMessage(ex));
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", friendlyPersistenceMessage(ex));
        }
        if (userId == null) {
            return "redirect:/admin/setup/user-locations";
        }
        return "redirect:/admin/setup/user-locations?userId=" + userId;
    }

    private static String friendlyPersistenceMessage(Throwable ex) {
        String combined = collectMessages(ex);
        if (combined == null) {
            return "Could not complete the request. Please try again.";
        }
        String lower = combined.toLowerCase();
        if (lower.contains("foreign key") || lower.contains("cannot delete")) {
            return "This assignment is still referenced elsewhere.";
        }
        return "Could not update assignments. Please try again.";
    }

    private static String collectMessages(Throwable ex) {
        if (ex == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t.getMessage() != null) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(t.getMessage());
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
}
