package com.dezxxx.rest.servlet;

import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.model.Event;
import com.dezxxx.rest.repository.EventRepository;
import com.dezxxx.rest.util.JsonUtil;
import com.dezxxx.rest.validation.EntityValidator;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/events/*")
public class EventServlet extends BaseServlet {

    private EventRepository eventService;

    @Override
    public void init() {
        eventService = (EventRepository) getServletContext().getAttribute("eventService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo != null && pathInfo.startsWith("/user/")) {
                Integer userId = Integer.parseInt(pathInfo.substring("/user/".length()));
                JsonUtil.writeResponse(resp, HttpServletResponse.SC_OK, eventService.findByUserId(userId));
                return;
            }

            Integer id = extractId(req);
            if (id == null) {
                JsonUtil.writeResponse(resp, HttpServletResponse.SC_OK, eventService.findAll());
            } else {
                eventService.findById(id)
                        .ifPresentOrElse(
                                event -> {
                                    try {
                                        JsonUtil.writeResponse(resp, HttpServletResponse.SC_OK, event);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                },
                                () -> {
                                    try {
                                        JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Event not found: " + id);
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
            Event event = JsonUtil.readBody(req, Event.class);
            EntityValidator.validate(event);
            Event created = eventService.create(event);
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
            Event event = JsonUtil.readBody(req, Event.class);
            EntityValidator.validate(event);
            event.setId(id);
            Event updated = eventService.update(event);
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
            Event existing = eventService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Event", id));
            Event patched = JsonUtil.mergeBody(req, existing);
            EntityValidator.validate(patched);
            Event updated = eventService.update(patched);
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
            eventService.delete(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
