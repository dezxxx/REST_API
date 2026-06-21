package com.dezxxx.rest.servlet;

import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.exception.ValidationException;
import com.dezxxx.rest.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(BaseServlet.class);

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
        if (e instanceof EntityNotFoundException) {
            log.warn(e.getMessage());
            JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
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
        } else {
            log.error("Unexpected error", e);
            JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
}
