package com.finance.accounting.controller;

import com.finance.accounting.repository.TenantRepository;
import com.finance.accounting.web.TenantSession;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class RootController {

    private final TenantRepository tenantRepository;

    @GetMapping("/")
    public String root(HttpSession session) {
        if (session.getAttribute(TenantSession.TENANT_ID) != null) {
            return "redirect:/admin/home";
        }
        if (tenantRepository.count() == 0) {
            return "redirect:/setup/organization";
        }
        return "redirect:/setup/select-organization";
    }
}
