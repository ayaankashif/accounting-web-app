package com.finance.accounting.web;

import com.finance.accounting.models.Tenant;
import com.finance.accounting.repository.TenantRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@RequiredArgsConstructor
public class TenantRequiredInterceptor implements HandlerInterceptor {

    private final TenantRepository tenantRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession();
        Long tenantId = (Long) session.getAttribute(TenantSession.TENANT_ID);

        if (tenantId != null && !tenantRepository.existsById(tenantId)) {
            session.removeAttribute(TenantSession.TENANT_ID);
            tenantId = null;
        }

        if (tenantId != null) {
            return true;
        }

        String ctx = request.getContextPath();
        if (tenantRepository.count() == 0) {
            response.sendRedirect(ctx + "/setup/organization");
        } else {
            response.sendRedirect(ctx + "/setup/select-organization");
        }
        return false;
    }

    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView) {
        if (modelAndView == null) {
            return;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Long tenantId = (Long) session.getAttribute(TenantSession.TENANT_ID);
        if (tenantId == null) {
            return;
        }
        Optional<Tenant> tenant = tenantRepository.findById(tenantId);
        modelAndView.addObject("currentTenantId", tenantId);
        modelAndView.addObject("currentTenantName", tenant.map(Tenant::getName).orElse(""));
        modelAndView.addObject("currentTenantCode", tenant.map(Tenant::getCode).orElse(""));
    }
}
