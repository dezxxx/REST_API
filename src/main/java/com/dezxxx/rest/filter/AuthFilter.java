package com.dezxxx.rest.filter;

import com.dezxxx.rest.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

// @WebFilter регистрирует фильтр на Tomcat без web.xml.
// Указываем пути — фильтр сработает ТОЛЬКО для этих URL.
// /swagger-ui.html и /api-docs не указаны → Swagger открывается без авторизации.
@WebFilter({"/users/*", "/files/*", "/events/*"})
public class AuthFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD = "admin";

    // init() и destroy() не нужны — оставляем дефолтные пустые реализации из интерфейса.
    // doFilter() — сердце фильтра, вызывается на каждый входящий запрос.
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Filter работает с базовыми ServletRequest/ServletResponse,
        // но нам нужен HTTP — кастим, чтобы получить доступ к заголовкам.
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Шаг 1: читаем заголовок Authorization.
        // Если его нет вообще — сразу 401.
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            sendUnauthorized(resp);
            return; // важно: return останавливает цепочку, сервлет не вызывается
        }

        // Шаг 2: декодируем Base64.
        // "Basic YWRtaW46YWRtaW4=" → убираем "Basic " → декодируем → "admin:admin"
        String decoded;
        try {
            decoded = new String(
                    Base64.getDecoder().decode(authHeader.substring(6)),
                    StandardCharsets.UTF_8
            );
        } catch (IllegalArgumentException e) {
            // Строка не является валидным Base64
            sendUnauthorized(resp);
            return;
        }

        // Шаг 3: разбиваем "admin:admin" на логин и пароль.
        // limit=2 чтобы пароль сам мог содержать двоеточие: "user:pa:ss" → ["user", "pa:ss"]
        String[] parts = decoded.split(":", 2);
        if (parts.length != 2) {
            sendUnauthorized(resp);
            return;
        }

        String username = parts[0];
        String password = parts[1];

        // Шаг 4: проверяем учётные данные.
        if (!VALID_USERNAME.equals(username) || !VALID_PASSWORD.equals(password)) {
            log.warn("Failed auth attempt for username: {}", username);
            sendUnauthorized(resp);
            return;
        }

        log.debug("Authenticated: {}", username);

        // Шаг 5: chain.doFilter() — передаём запрос дальше по цепочке.
        // Это ключевой вызов: без него запрос никогда не дойдёт до сервлета.
        chain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse resp) throws IOException {
        // WWW-Authenticate — стандартный HTTP заголовок, который говорит клиенту:
        // "нужна Basic Auth, имя области — REST API".
        // Браузер увидит этот заголовок и покажет диалог логин/пароль.
        resp.setHeader("WWW-Authenticate", "Basic realm=\"REST API\"");
        JsonUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
    }
}
