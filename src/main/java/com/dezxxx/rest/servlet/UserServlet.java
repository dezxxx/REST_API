package com.dezxxx.rest.servlet;

import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.model.User;
import com.dezxxx.rest.service.UserService;
import com.dezxxx.rest.util.JsonUtil;

import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/users/*")
public class UserServlet extends BaseServlet {

    private UserService userService;

    @Override
    public void init() {
        userService = (UserService) getServletContext().getAttribute("userService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = extractId(req);
            if (id == null) {
                JsonUtil.writeResponse(resp, HttpServletResponse.SC_OK, userService.findAll());
            } else {
                userService.findById(id)
                        .ifPresentOrElse(
                                user -> {
                                    try {
                                        JsonUtil.writeResponse(resp, HttpServletResponse.SC_OK, user);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                },
                                () -> {
                                    try {
                                        JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "User not found: " + id);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        );
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User user = JsonUtil.readBody(req, User.class);
            User created = userService.create(user);
            JsonUtil.writeResponse(resp, HttpServletResponse.SC_CREATED, created);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = extractId(req);
            if (id == null) {
                JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "ID is required");
                return;
            }
            User user = JsonUtil.readBody(req, User.class);
            user.setId(id);
            User updated = userService.update(user);
            JsonUtil.writeResponse(resp, HttpServletResponse.SC_OK, updated);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = extractId(req);
            if (id == null) {
                JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "ID is required");
                return;
            }
            User existing = userService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("User", id));
            User patched = JsonUtil.mergeBody(req, existing);
            User updated = userService.update(patched);
            JsonUtil.writeResponse(resp, HttpServletResponse.SC_OK, updated);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = extractId(req);
            if (id == null) {
                JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "ID is required");
                return;
            }
            userService.delete(id);
            JsonUtil.writeResponse(resp, HttpServletResponse.SC_OK, Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}