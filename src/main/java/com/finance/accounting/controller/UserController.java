package com.finance.accounting.controller;

import com.finance.accounting.models.AppRole;
import com.finance.accounting.models.Location;
import com.finance.accounting.models.User;
import com.finance.accounting.service.SignatureStorageService;
import com.finance.accounting.service.UserService;
import com.finance.accounting.web.TenantSession;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final UserService userService;
    private final SignatureStorageService signatureStorageService;

    public UserController(UserService userService, SignatureStorageService signatureStorageService) {
        this.userService = userService;
        this.signatureStorageService = signatureStorageService;
    }

    /** Legacy URL; use {@code /admin/setup/users}. */
    @Deprecated
    @GetMapping("/users")
    public String legacyUsers() {
        return "redirect:/admin/setup/users";
    }

    @PostMapping("/admin/setup/users")
    public String registerUser(
            @ModelAttribute("user") User user,
            @RequestParam(value = "roleId", required = false) Long roleId,
            @RequestParam(value = "locationId", required = false) Long locationId,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "signature", required = false) MultipartFile signature,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long tenantId = (Long) session.getAttribute(TenantSession.TENANT_ID);
        applyRoleId(user, roleId);
        if (StringUtils.hasText(password)) {
            user.setPasswordHash(password);
        }
        applyLocationId(user, locationId);
        user.setId(null);
        try {
            signatureStorageService
                    .storeSignature(tenantId, signature)
                    .ifPresent(user::setSignatureFilePath);
            userService.save(tenantId, user);
            redirectAttributes.addFlashAttribute("successMessage", "User saved.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Could not store signature file. Please try again.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", friendlyPersistenceMessage(ex));
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", friendlyPersistenceMessage(ex));
        }
        return "redirect:/admin/setup/users";
    }

    @PostMapping("/admin/setup/users/update")
    public String updateUser(
            @RequestParam("id") Long id,
            @ModelAttribute("user") User user,
            @RequestParam(value = "roleId", required = false) Long roleId,
            @RequestParam(value = "locationId", required = false) Long locationId,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "signature", required = false) MultipartFile signature,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long tenantId = (Long) session.getAttribute(TenantSession.TENANT_ID);
        applyRoleId(user, roleId);
        if (StringUtils.hasText(password)) {
            user.setPasswordHash(password);
        }
        applyLocationId(user, locationId);
        try {
            signatureStorageService
                    .storeSignature(tenantId, signature)
                    .ifPresent(user::setSignatureFilePath);
            userService.update(tenantId, id, user);
            redirectAttributes.addFlashAttribute("successMessage", "User updated.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage() != null ? ex.getMessage() : "Record not found.");
        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Could not store signature file. Please try again.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", friendlyPersistenceMessage(ex));
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", friendlyPersistenceMessage(ex));
        }
        return "redirect:/admin/setup/users";
    }

    @PostMapping("/admin/setup/users/delete")
    public String deleteUser(
            @RequestParam("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long tenantId = (Long) session.getAttribute(TenantSession.TENANT_ID);
        try {
            userService.deleteById(tenantId, id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted.");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", friendlyPersistenceMessage(ex));
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", friendlyPersistenceMessage(ex));
        }
        return "redirect:/admin/setup/users";
    }

    private static void applyRoleId(User user, Long roleId) {
        if (roleId == null) {
            return;
        }
        AppRole role = new AppRole();
        role.setId(roleId);
        user.setRole(role);
    }

    private static void applyLocationId(User user, Long locationId) {
        if (locationId == null) {
            user.setLocation(null);
            return;
        }
        Location loc = new Location();
        loc.setId(locationId);
        user.setLocation(loc);
    }

    /**
     * Maps JDBC / JPA constraint errors to a short message shown on the Users page after redirect.
     */
    private static String friendlyPersistenceMessage(Throwable ex) {
        String combined = collectMessages(ex);
        if (combined == null) {
            return "Could not complete the request. Please check your input and try again.";
        }
        String lower = combined.toLowerCase();
        if (lower.contains("password_hash") || lower.contains("password hash")) {
            return "Password is required for new users.";
        }
        if (lower.contains("duplicate") || lower.contains("unique")) {
            return "A user with this login name or user code already exists for this organization.";
        }
        if (lower.contains("foreign key") || lower.contains("cannot delete")) {
            return "This record is still in use and cannot be removed or updated that way.";
        }
        return "Could not save changes. Please verify all required fields and try again.";
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
