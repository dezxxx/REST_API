package com.dezxxx.rest.servlet;

import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.model.Event;
import com.dezxxx.rest.model.File;
import com.dezxxx.rest.model.User;
import com.dezxxx.rest.service.EventService;
import com.dezxxx.rest.service.FileService;
import com.dezxxx.rest.service.UserService;
import com.dezxxx.rest.util.FileStorageUtil;
import com.dezxxx.rest.util.JsonUtil;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@WebServlet("/api/v1/files/*")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 11 * 1024 * 1024)
public class FileServlet extends BaseServlet {

    private FileService fileService;
    private UserService userService;
    private EventService eventService;

    @Override
    public void init() {
        fileService = (FileService) getServletContext().getAttribute("fileService");
        userService = (UserService) getServletContext().getAttribute("userService");
        eventService = (EventService) getServletContext().getAttribute("eventService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo != null && pathInfo.matches("/\\d+/download")) {
                Integer id = extractId(req);
                streamFile(id, resp);
                return;
            }
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
            Part filePart = req.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Multipart part 'file' is required");
                return;
            }

            String originalName = extractFileName(filePart);
            String contentType = filePart.getContentType();
            long size = filePart.getSize();

            File created = fileService.upload(originalName, contentType, size, filePart.getInputStream());

            String userIdHeader = req.getHeader("X-User-Id");
            if (userIdHeader != null && !userIdHeader.isBlank()) {
                User uploader = userService.findById(Integer.parseInt(userIdHeader)).orElse(null);
                if (uploader != null) {
                    eventService.create(new Event(uploader, created));
                }
            }

            resp.setHeader("Location", req.getContextPath() + "/api/v1/files/" + created.getId());
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
            File patch = JsonUtil.readBody(req, File.class);
            patch.setId(id);
            File updated = fileService.update(patch);
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

    private void streamFile(Integer id, HttpServletResponse resp) throws IOException {
        File file = fileService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("File", id));
        resp.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        if (file.getSize() != null) {
            resp.setContentLengthLong(file.getSize());
        }
        try (InputStream in = FileStorageUtil.read(file.getFilePath())) {
            in.transferTo(resp.getOutputStream());
        }
    }

    private String extractFileName(Part part) {
        String disposition = part.getHeader("Content-Disposition");
        if (disposition != null) {
            for (String token : disposition.split(";")) {
                token = token.trim();
                if (token.startsWith("filename")) {
                    return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                }
            }
        }
        return "unnamed";
    }
}
