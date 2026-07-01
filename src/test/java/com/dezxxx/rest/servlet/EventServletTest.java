package com.dezxxx.rest.servlet;

import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.exception.ValidationException;
import com.dezxxx.rest.model.Event;
import com.dezxxx.rest.model.File;
import com.dezxxx.rest.model.User;
import com.dezxxx.rest.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServletTest {

    @Mock private EventService eventService;
    @Mock private HttpServletRequest req;
    @Mock private HttpServletResponse resp;
    @Mock private ServletConfig servletConfig;
    @Mock private ServletContext servletContext;

    private EventServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("eventService")).thenReturn(eventService);
        servlet = new EventServlet();
        servlet.init(servletConfig);
        lenient().when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    // --- GET all ---

    @Test
    void doGet_shouldReturn200_whenNoId() throws Exception {
        when(req.getPathInfo()).thenReturn(null);
        when(eventService.findAll()).thenReturn(List.of());

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
        verify(eventService).findAll();
    }

    // --- GET by id ---

    @Test
    void doGet_shouldReturn200_whenEventFound() throws Exception {
        when(req.getPathInfo()).thenReturn("/1");
        when(eventService.findById(1)).thenReturn(Optional.of(event(1)));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void doGet_shouldReturn404_whenEventNotFound() throws Exception {
        when(req.getPathInfo()).thenReturn("/99");
        when(eventService.findById(99)).thenReturn(Optional.empty());

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    // --- GET by userId ---

    @Test
    void doGet_shouldReturn200_whenGetByUserId() throws Exception {
        when(req.getPathInfo()).thenReturn("/user/1");
        when(eventService.findByUserId(1)).thenReturn(List.of(event(1)));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
        verify(eventService).findByUserId(1);
    }

    // --- POST create ---

    @Test
    void doPost_shouldReturn201_whenCreated() throws Exception {
        Event created = event(1);
        when(req.getReader()).thenReturn(reader("{\"user\":{\"id\":1},\"file\":{\"id\":1}}"));
        when(req.getContextPath()).thenReturn("/REST_API");
        when(eventService.create(any())).thenReturn(created);

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_CREATED);
        verify(resp).setHeader("Location", "/REST_API/api/v1/events/1");
    }

    @Test
    void doPost_shouldReturn400_whenValidationFails() throws Exception {
        when(req.getReader()).thenReturn(reader("{\"user\":null,\"file\":null}"));
        when(eventService.create(any())).thenThrow(new ValidationException("Event must have a valid user id"));

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void doPost_shouldReturn404_whenUserOrFileNotFound() throws Exception {
        when(req.getReader()).thenReturn(reader("{\"user\":{\"id\":99},\"file\":{\"id\":1}}"));
        when(eventService.create(any())).thenThrow(new EntityNotFoundException("User", 99));

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    // --- PUT update ---

    @Test
    void doPut_shouldReturn200_whenUpdated() throws Exception {
        Event updated = event(1);
        when(req.getPathInfo()).thenReturn("/1");
        when(req.getReader()).thenReturn(reader("{\"user\":{\"id\":2},\"file\":{\"id\":2}}"));
        when(eventService.update(any())).thenReturn(updated);

        servlet.doPut(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void doPut_shouldReturn400_whenNoId() throws Exception {
        when(req.getPathInfo()).thenReturn(null);

        servlet.doPut(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(eventService, never()).update(any());
    }

    @Test
    void doPut_shouldReturn404_whenNotFound() throws Exception {
        when(req.getPathInfo()).thenReturn("/99");
        when(req.getReader()).thenReturn(reader("{\"user\":{\"id\":1},\"file\":{\"id\":1}}"));
        when(eventService.update(any())).thenThrow(new EntityNotFoundException("Event", 99));

        servlet.doPut(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    // --- PATCH update ---

    @Test
    void doPatch_shouldReturn200_whenUpdated() throws Exception {
        Event existing = event(1);
        when(req.getPathInfo()).thenReturn("/1");
        when(eventService.findById(1)).thenReturn(Optional.of(existing));
        when(req.getReader()).thenReturn(reader("{}"));
        when(eventService.update(any())).thenReturn(existing);

        servlet.doPatch(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void doPatch_shouldReturn400_whenNoId() throws Exception {
        when(req.getPathInfo()).thenReturn(null);

        servlet.doPatch(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(eventService, never()).update(any());
    }

    @Test
    void doPatch_shouldReturn404_whenEventNotFound() throws Exception {
        when(req.getPathInfo()).thenReturn("/99");
        when(eventService.findById(99)).thenReturn(Optional.empty());

        servlet.doPatch(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    // --- DELETE ---

    @Test
    void doDelete_shouldReturn200_whenDeleted() throws Exception {
        when(req.getPathInfo()).thenReturn("/1");

        servlet.doDelete(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
        verify(eventService).delete(1);
    }

    @Test
    void doDelete_shouldReturn404_whenNotFound() throws Exception {
        when(req.getPathInfo()).thenReturn("/99");
        doThrow(new EntityNotFoundException("Event", 99)).when(eventService).delete(99);

        servlet.doDelete(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void doDelete_shouldReturn400_whenNoId() throws Exception {
        when(req.getPathInfo()).thenReturn(null);

        servlet.doDelete(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(eventService, never()).delete(any());
    }

    private static Event event(int id) {
        User user = new User("Ivan");
        user.setId(1);
        File file = new File("report.pdf", "/storage/report.pdf");
        file.setId(1);
        Event event = new Event(user, file);
        event.setId(id);
        return event;
    }

    private static BufferedReader reader(String json) {
        return new BufferedReader(new StringReader(json));
    }
}
