package com.dezxxx.rest.servlet;

import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.exception.ValidationException;
import com.dezxxx.rest.model.User;
import com.dezxxx.rest.service.UserService;
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
class UserServletTest {

    @Mock private UserService userService;
    @Mock private HttpServletRequest req;
    @Mock private HttpServletResponse resp;
    @Mock private ServletConfig servletConfig;
    @Mock private ServletContext servletContext;

    private UserServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("userService")).thenReturn(userService);
        servlet = new UserServlet();
        servlet.init(servletConfig);
        lenient().when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    // --- GET all ---

    @Test
    void doGet_shouldReturn200_whenNoId() throws Exception {
        when(req.getPathInfo()).thenReturn(null);
        when(userService.findAll()).thenReturn(List.of(new User("Ivan"), new User("Maria")));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
        verify(userService).findAll();
    }

    // --- GET by id ---

    @Test
    void doGet_shouldReturn200_whenUserFound() throws Exception {
        User user = new User("Ivan");
        user.setId(1);
        when(req.getPathInfo()).thenReturn("/1");
        when(userService.findById(1)).thenReturn(Optional.of(user));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void doGet_shouldReturn404_whenUserNotFound() throws Exception {
        when(req.getPathInfo()).thenReturn("/99");
        when(userService.findById(99)).thenReturn(Optional.empty());

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void doGet_shouldReturn400_whenIdIsInvalid() throws Exception {
        when(req.getPathInfo()).thenReturn("/abc");

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    // --- POST create ---

    @Test
    void doPost_shouldReturn201_whenCreated() throws Exception {
        User created = new User("Ivan");
        created.setId(1);
        when(req.getReader()).thenReturn(reader("{\"name\":\"Ivan\"}"));
        when(req.getContextPath()).thenReturn("/REST_API");
        when(userService.create(any())).thenReturn(created);

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_CREATED);
        verify(resp).setHeader("Location", "/REST_API/api/v1/users/1");
    }

    @Test
    void doPost_shouldReturn400_whenValidationFails() throws Exception {
        when(req.getReader()).thenReturn(reader("{\"name\":\"  \"}"));
        when(userService.create(any())).thenThrow(new ValidationException("User name must not be blank"));

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    // --- PUT update ---

    @Test
    void doPut_shouldReturn200_whenUpdated() throws Exception {
        User updated = new User("Sergey");
        updated.setId(1);
        when(req.getPathInfo()).thenReturn("/1");
        when(req.getReader()).thenReturn(reader("{\"name\":\"Sergey\"}"));
        when(userService.update(any())).thenReturn(updated);

        servlet.doPut(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void doPut_shouldReturn400_whenNoId() throws Exception {
        when(req.getPathInfo()).thenReturn(null);

        servlet.doPut(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(userService, never()).update(any());
    }

    @Test
    void doPut_shouldReturn404_whenNotFound() throws Exception {
        when(req.getPathInfo()).thenReturn("/99");
        when(req.getReader()).thenReturn(reader("{\"name\":\"Sergey\"}"));
        when(userService.update(any())).thenThrow(new EntityNotFoundException("User", 99));

        servlet.doPut(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    // --- PATCH update ---

    @Test
    void doPatch_shouldReturn200_whenUpdated() throws Exception {
        User existing = new User("Ivan");
        existing.setId(1);
        when(req.getPathInfo()).thenReturn("/1");
        when(userService.findById(1)).thenReturn(Optional.of(existing));
        when(req.getReader()).thenReturn(reader("{\"name\":\"Sergey\"}"));
        when(userService.update(any())).thenReturn(existing);

        servlet.doPatch(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void doPatch_shouldReturn400_whenNoId() throws Exception {
        when(req.getPathInfo()).thenReturn(null);

        servlet.doPatch(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(userService, never()).update(any());
    }

    @Test
    void doPatch_shouldReturn404_whenUserNotFound() throws Exception {
        when(req.getPathInfo()).thenReturn("/99");
        when(userService.findById(99)).thenReturn(Optional.empty());

        servlet.doPatch(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    // --- DELETE ---

    @Test
    void doDelete_shouldReturn200_whenDeleted() throws Exception {
        when(req.getPathInfo()).thenReturn("/1");

        servlet.doDelete(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
        verify(userService).delete(1);
    }

    @Test
    void doDelete_shouldReturn404_whenNotFound() throws Exception {
        when(req.getPathInfo()).thenReturn("/99");
        doThrow(new EntityNotFoundException("User", 99)).when(userService).delete(99);

        servlet.doDelete(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void doDelete_shouldReturn400_whenNoId() throws Exception {
        when(req.getPathInfo()).thenReturn(null);

        servlet.doDelete(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(userService, never()).delete(any());
    }

    // --- BaseServlet: PATCH routing ---

    @Test
    void service_shouldRoutePatch_toDoPatch() throws Exception {
        UserServlet spy = spy(servlet);
        when(req.getMethod()).thenReturn("PATCH");
        when(req.getPathInfo()).thenReturn(null);

        spy.service(req, resp);

        verify(spy).doPatch(req, resp);
    }

    private static BufferedReader reader(String json) {
        return new BufferedReader(new StringReader(json));
    }
}
