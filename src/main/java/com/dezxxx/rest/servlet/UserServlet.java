package com.dezxxx.rest.servlet;

import com.dezxxx.rest.model.User;
import com.dezxxx.rest.repository.Repository;
import com.dezxxx.rest.util.JsonUtil;
import com.dezxxx.rest.validation.EntityValidator;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/users/*")
public class UserServlet extends BaseServlet {

    private Repository<User> userService;

    @Override
    public void init() {
        userService = (Repository<User>) getServletContext().getAttribute("userService");
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
            EntityValidator.validate(user);
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
            EntityValidator.validate(user);
            user.setId(id);
            User updated = userService.update(user);
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
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
