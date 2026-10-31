package com.college.events.controller;

import com.college.events.service.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Turns business rule violations and upload errors into friendly flash messages on the page
 * the user came from, instead of crashing to a generic error page.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException exception,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        return "redirect:" + safeReturnUrl(request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeException(MaxUploadSizeExceededException exception,
                                               HttpServletRequest request,
                                               RedirectAttributes redirectAttributes) {
        log.warn("Upload size exceeded: {}", exception.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage",
                "The uploaded image is too large. Please choose an image smaller than 20MB.");
        return "redirect:" + safeReturnUrl(request);
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception exception,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {
        log.error("Unhandled exception processing request: {}", exception.getMessage(), exception);
        String msg = exception.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = "An unexpected error occurred. Please try again.";
        }
        redirectAttributes.addFlashAttribute("errorMessage", msg);
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
