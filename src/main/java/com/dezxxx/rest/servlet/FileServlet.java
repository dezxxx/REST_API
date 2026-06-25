package com.dezxxx.rest.servlet;

import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.model.File;
import com.dezxxx.rest.service.FileService;
import com.dezxxx.rest.util.JsonUtil;

import java.util.Map;
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
                writeFoundOrNotFound(resp, fileService.findById(id), "File not found: " + id);
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            File file = JsonUtil.readBody(req, File.class);
            File created = fileService.create(file);
            resp.setHeader("Location", req.getContextPath() + "/files/" + created.getId());
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
            File file = JsonUtil.readBody(req, File.class);
            file.setId(id);
            File updated = fileService.update(file);
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
            File existing = fileService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("File", id));
            File patched = JsonUtil.mergeBody(req, existing);
            File updated = fileService.update(patched);
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
            fileService.delete(id);
            JsonUtil.writeResponse(resp, HttpServletResponse.SC_OK, Map.of("message", "File deleted successfully"));
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}