package com.college.events.controller;

import com.college.events.service.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Turns business rule violations into a friendly flash message on the page the
 * user came from, instead of a stack trace.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException exception,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        return "redirect:" + safeReturnUrl(request);
    }

    private String safeReturnUrl(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return "/";
        }
        // Only bounce back inside this application.
        String contextPath = request.getContextPath();
        int index = referer.indexOf(contextPath + "/");
        if (index < 0) {
            return "/";
        }
        String path = referer.substring(index + contextPath.length());
        return path.startsWith("/student") || path.startsWith("/organizer") || path.startsWith("/explore")
                ? path : "/";
    }
}
