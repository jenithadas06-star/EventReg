package com.college.events.config;

import com.college.events.model.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Role based access control.
 *
 * <p>Students can never reach {@code /organizer/**} and organizers can never reach
 * {@code /student/**}, even by typing the URL directly - the check happens on the
 * server before the controller (and therefore before any data) is touched.</p>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean studentArea = path.startsWith("/student");
        boolean organizerArea = path.startsWith("/organizer");

        if (!studentArea && !organizerArea) {
            return true;
        }

        HttpSession session = request.getSession(false);
        Long userId = session == null ? null : (Long) session.getAttribute(SessionKeys.USER_ID);
        Role role = session == null ? null : (Role) session.getAttribute(SessionKeys.USER_ROLE);

        if (userId == null || role == null) {
            response.sendRedirect(request.getContextPath() + "/login?required="
                    + (studentArea ? "STUDENT" : "ORGANIZER"));
            return false;
        }

        if (studentArea && role != Role.STUDENT) {
            response.sendRedirect(request.getContextPath() + "/organizer/dashboard?denied=1");
            return false;
        }
        if (organizerArea && role != Role.ORGANIZER) {
            response.sendRedirect(request.getContextPath() + "/student/dashboard?denied=1");
            return false;
        }
        return true;
    }
}
