package com.dezxxx.rest.servlet;

import com.dezxxx.rest.exception.DependencyException;
import com.dezxxx.rest.exception.DuplicateEntityException;
import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.exception.ValidationException;
import com.dezxxx.rest.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(BaseServlet.class);

    // HttpServlet не знает о PATCH — перехватываем его здесь и роутим в doPatch()
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonUtil.writeError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method not allowed");
    }

    protected Integer extractId(HttpServletRequest req) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            return null;
        }
        try {
            return Integer.parseInt(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid id: " + pathInfo.substring(1));
        }
    }

    protected void handleException(HttpServletResponse resp, Exception e) throws IOException {
        if (e instanceof DependencyException) {
            log.warn(e.getMessage());
            JsonUtil.writeError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } else if (e instanceof EntityNotFoundException) {
            log.warn(e.getMessage());
            JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } else if (e instanceof DuplicateEntityException) {
            log.warn(e.getMessage());
            JsonUtil.writeError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } else if (e instanceof ValidationException) {
            log.warn(e.getMessage());
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } else if (e instanceof IllegalArgumentException) {
            log.warn(e.getMessage());
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } else if (e instanceof JsonProcessingException
                || (e.getCause() instanceof JsonProcessingException)) {
            log.warn("Invalid JSON: {}", e.getMessage());
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing JSON body");
        } else if (hasConstraintViolation(e)) {
            log.warn("Constraint violation: {}", e.getMessage());
            JsonUtil.writeError(resp, HttpServletResponse.SC_CONFLICT, "Duplicate entry — this combination already exists");
        } else {
            log.error("Unexpected error", e);
            JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private boolean hasConstraintViolation(Throwable e) {
        while (e != null) {
            if (e instanceof ConstraintViolationException) return true;
            e = e.getCause();
        }
        return false;
    }
}
