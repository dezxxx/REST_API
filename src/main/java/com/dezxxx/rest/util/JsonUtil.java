package com.dezxxx.rest.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtil() {
    }

    public static <T> T readBody(HttpServletRequest req, Class<T> clazz) throws IOException {
        return MAPPER.readValue(req.getReader(), clazz);
    }

    // Накладывает только пришедшие поля поверх существующего объекта — для PATCH
    public static <T> T mergeBody(HttpServletRequest req, T existing) throws IOException {
        return MAPPER.readerForUpdating(existing).readValue(req.getReader());
    }

    public static void writeResponse(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        MAPPER.writeValue(resp.getWriter(), body);
    }

    public static void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        writeResponse(resp, status, Map.of("error", message));
    }
}
