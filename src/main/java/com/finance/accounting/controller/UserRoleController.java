package com.finance.accounting.controller;

import com.finance.accounting.models.AppRole;
import com.finance.accounting.service.AppRoleService;
import com.finance.accounting.web.TenantSession;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/setup/user-roles")
public class UserRoleController {

    private final AppRoleService appRoleService;

    public UserRoleController(AppRoleService appRoleService) {
        this.appRoleService = appRoleService;
    }

    @GetMapping
    public String page(Model model, HttpSession session) {
        Long tenantId = (Long) session.getAttribute(TenantSession.TENANT_ID);
        model.addAttribute("pageTitle", "User Roles");
        model.addAttribute("activeNav", "user-roles");
        model.addAttribute("openSetup", true);
        model.addAttribute("openSecurity", false);
        List<AppRole> roles =
                tenantId != null
                        ? appRoleService.findAll(tenantId)
                        : Collections.emptyList();
        model.addAttribute("roles", roles);
        return "admin/user-roles";
    }

    @PostMapping
    public String create(
            @ModelAttribute("role") AppRole role,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long tenantId = (Long) session.getAttribute(TenantSession.TENANT_ID);
        // Always create a new row from this action (hidden id may be stale from Edit).
        role.setId(null);
        try {
            appRoleService.save(tenantId, role);
            redirectAttributes.addFlashAttribute("successMessage", "Role saved.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", friendlyPersistenceMessage(ex));
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", friendlyPersistenceMessage(ex));
        }
        return "redirect:/admin/setup/user-roles";
    }

    @PostMapping("/update")
    public String update(
            @RequestParam("id") Long id,
            @ModelAttribute("role") AppRole role,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long tenantId = (Long) session.getAttribute(TenantSession.TENANT_ID);
        try {
            appRoleService.update(tenantId, id, role);
            redirectAttributes.addFlashAttribute("successMessage", "Role updated.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage() != null ? ex.getMessage() : "Role not found.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", friendlyPersistenceMessage(ex));
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", friendlyPersistenceMessage(ex));
        }
        return "redirect:/admin/setup/user-roles";
    }

    @PostMapping("/delete")
    public String delete(
            @RequestParam("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long tenantId = (Long) session.getAttribute(TenantSession.TENANT_ID);
        try {
            appRoleService.deleteById(tenantId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Role deleted.");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Role not found.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", friendlyPersistenceMessage(ex));
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", friendlyPersistenceMessage(ex));
        }
        return "redirect:/admin/setup/user-roles";
    }

    private static String friendlyPersistenceMessage(Throwable ex) {
        String combined = collectMessages(ex);
        if (combined == null) {
            return "Could not complete the request. Please try again.";
        }
        String lower = combined.toLowerCase();
        if (lower.contains("duplicate") || lower.contains("unique")) {
            return "This value conflicts with another record. If the problem continues, contact support.";
        }
        if (lower.contains("foreign key") || lower.contains("cannot delete")) {
            return "This role is still assigned to users or linked data and cannot be removed.";
        }
        return "Could not save changes. Please verify input and try again.";
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
