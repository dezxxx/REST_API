package com.dezxxx.rest.servlet;

import com.dezxxx.rest.model.File;
import com.dezxxx.rest.service.FileService;
import com.dezxxx.rest.util.JsonUtil;
import com.dezxxx.rest.validation.EntityValidator;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/files/*")
public class FileServlet extends BaseServlet {

    private FileService fileService;

    @Override
    public void init() {
        fileService = (FileService) getServletContext().getAttribute("fileService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = extractId(req);
            if (id == null) {
                JsonUtil.writeResponse(resp, HttpServletResponse.SC_OK, fileService.findAll());
            } else {
                fileService.findById(id)
                        .ifPresentOrElse(
                                file -> {
                                    try {
                                        JsonUtil.writeResponse(resp, HttpServletResponse.SC_OK, file);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                },
                                () -> {
                                    try {
                                        JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "File not found: " + id);
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
            File file = JsonUtil.readBody(req, File.class);
            EntityValidator.validate(file);
            File created = fileService.create(file);
            JsonUtil.writeResponse(resp, HttpServletResponse.SC_CREATED, created);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = extractId(req);
            File file = JsonUtil.readBody(req, File.class);
            EntityValidator.validate(file);
            file.setId(id);
            File updated = fileService.update(file);
            JsonUtil.writeResponse(resp, HttpServletResponse.SC_OK, updated);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = extractId(req);
            fileService.delete(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
