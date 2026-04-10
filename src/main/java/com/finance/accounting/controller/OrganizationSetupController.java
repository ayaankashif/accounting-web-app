package com.finance.accounting.controller;

import com.finance.accounting.models.Tenant;
import com.finance.accounting.repository.TenantRepository;
import com.finance.accounting.service.TenantService;
import com.finance.accounting.web.TenantSession;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/setup")
@RequiredArgsConstructor
public class OrganizationSetupController {

    private final TenantService tenantService;
    private final TenantRepository tenantRepository;

    @GetMapping("/organization")
    public String organizationForm(Model model) {
        model.addAttribute("pageTitle", "Register organization");
        model.addAttribute("tenantsExist", tenantRepository.count() > 0);
        return "setup/organization";
    }

    @PostMapping("/organization")
    public String registerOrganization(
            @RequestParam("code") String code,
            @RequestParam("name") String name,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            Tenant tenant = tenantService.registerOrganization(code, name);
            session.setAttribute(TenantSession.TENANT_ID, tenant.getId());
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Organization registered. You can continue to administration.");
            return "redirect:/admin/home";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("prefillCode", code);
            redirectAttributes.addFlashAttribute("prefillName", name);
            return "redirect:/setup/organization";
        }
    }

    @GetMapping("/select-organization")
    public String selectOrganization(Model model, HttpSession session) {
        if (tenantRepository.count() == 0) {
            return "redirect:/setup/organization";
        }
        model.addAttribute("pageTitle", "Select organization");
        model.addAttribute("tenants", tenantService.findAllActive());
        model.addAttribute("currentTenantId", session.getAttribute(TenantSession.TENANT_ID));
        return "setup/select-organization";
    }

    @PostMapping("/select-organization")
    public String selectOrganizationPost(
            @RequestParam("tenantId") Long tenantId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Tenant tenant =
                tenantRepository
                        .findById(tenantId)
                        .filter(Tenant::isActive)
                        .orElse(null);
        if (tenant == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Organization not found or inactive.");
            return "redirect:/setup/select-organization";
        }
        session.setAttribute(TenantSession.TENANT_ID, tenant.getId());
        return "redirect:/admin/home";
    }
}
