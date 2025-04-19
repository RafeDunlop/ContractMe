package nz.ac.canterbury.seng302.homehelper.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthFailHandler implements AuthenticationFailureHandler {

    /**
     * Auth failure handler callback.
     * @param request the http request
     * @param response the http response
     * @param exception the exception
     * @throws IOException for unexpected IO failure
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        if (exception instanceof AccountNotActivatedException) {
            request.getSession().setAttribute("errorMessage", "Please confirm your email address before attempting to login.");
            response.sendRedirect("/confirm-registration");
        } else {
            request.getSession().setAttribute("errorMessage", exception.getMessage());
            response.sendRedirect("/login");
        }
    }
}
