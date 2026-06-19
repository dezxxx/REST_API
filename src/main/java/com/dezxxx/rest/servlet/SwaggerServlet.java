package com.dezxxx.rest.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@WebServlet("/api-docs")
public class SwaggerServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(SwaggerServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("openapi.yaml")) {
            if (is == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                log.error("openapi.yaml not found on classpath");
                return;
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/yaml");
            resp.setCharacterEncoding("UTF-8");
            resp.getOutputStream().write(is.readAllBytes());
        }
    }
}
