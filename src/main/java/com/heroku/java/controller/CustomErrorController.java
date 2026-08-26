package com.heroku.java.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(jakarta.servlet.RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            // 404 (unknown page) and 403 (bad CSRF/expired session action)
            // both get the friendly page instead of the Whitelabel wall.
            if (statusCode == HttpStatus.NOT_FOUND.value()
                    || statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error";
            }
        }
        // Anything else (500 etc.) also gets the themed page, with generic copy.
        return "error";
    }
}
