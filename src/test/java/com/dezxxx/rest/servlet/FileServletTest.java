package com.dezxxx.rest.servlet;

import com.dezxxx.rest.exception.DependencyException;
import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.model.Event;
import com.dezxxx.rest.model.File;
import com.dezxxx.rest.model.User;
import com.dezxxx.rest.service.EventService;
import com.dezxxx.rest.service.FileService;
import com.dezxxx.rest.service.UserService;
import com.dezxxx.rest.util.FileStorageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServletTest {

    @Mock private FileService fileService;
    @Mock private UserService userService;
    @Mock private EventService eventService;
    @Mock private HttpServletRequest req;
    @Mock private HttpServletResponse resp;
    @Mock private ServletConfig servletConfig;
    @Mock private ServletContext servletContext;

    private FileServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("fileService")).thenReturn(fileService);
        when(servletContext.getAttribute("userService")).thenReturn(userService);
        when(servletContext.getAttribute("eventService")).thenReturn(eventService);
        servlet = new FileServlet();
        servlet.init(servletConfig);
        lenient().when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    // --- GET all ---

    @Test
    void doGet_shouldReturn200_whenNoId() throws Exception {
        when(req.getPathInfo()).thenReturn(null);
        when(fileService.findAll()).thenReturn(List.of(file(1), file(2)));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
        verify(fileService).findAll();
    }

    // --- GET by id ---

    @Test
    void doGet_shouldReturn200_whenFileFound() throws Exception {
        when(req.getPathInfo()).thenReturn("/1");
        when(fileService.findById(1)).thenReturn(Optional.of(file(1)));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void doGet_shouldReturn404_whenFileNotFound() throws Exception {
        when(req.getPathInfo()).thenReturn("/99");
        when(fileService.findById(99)).thenReturn(Optional.empty());

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    // --- GET download ---

    @Test
    void doGet_shouldStreamFile_whenDownloadPath() throws Exception {
        File f = file(1);
        when(req.getPathInfo()).thenReturn("/1/download");
        when(fileService.findById(1)).thenReturn(Optional.of(f));

        ServletOutputStream out = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(out);

        try (MockedStatic<FileStorageUtil> fsUtil = mockStatic(FileStorageUtil.class)) {
            fsUtil.when(() -> FileStorageUtil.read("/storage/report.pdf"))
                    .thenReturn(new ByteArrayInputStream("content".getBytes()));

            servlet.doGet(req, resp);

            verify(resp).setContentType("application/pdf");
            verify(resp).setHeader("Content-Disposition", "attachment; filename=\"report.pdf\"");
            verify(resp).setContentLengthLong(100L);
        }
    }

    @Test
    void doGet_shouldReturn404_whenDownloadFileNotFound() throws Exception {
        when(req.getPathInfo()).thenReturn("/99/download");
        when(fileService.findById(99)).thenReturn(Optional.empty());

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    // --- POST upload ---

    @Test
    void doPost_shouldReturn201_whenUploaded() throws Exception {
        Part part = uploadPart("report.pdf", "application/pdf", 100L);
        when(req.getPart("file")).thenReturn(part);
        when(req.getHeader("X-User-Id")).thenReturn(null);
        when(req.getContextPath()).thenReturn("/REST_API");

        File created = file(1);
        when(fileService.upload(eq("report.pdf"), eq("application/pdf"), eq(100L), any())).thenReturn(created);

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_CREATED);
        verify(resp).setHeader("Location", "/REST_API/api/v1/files/1");
        verify(eventService, never()).create(any());
    }

    @Test
    void doPost_shouldCreateEvent_whenXUserIdHeaderProvided() throws Exception {
        Part part = uploadPart("report.pdf", "application/pdf", 100L);
        when(req.getPart("file")).thenReturn(part);
        when(req.getHeader("X-User-Id")).thenReturn("1");
        when(req.getContextPath()).thenReturn("/REST_API");

        File created = file(1);
        User uploader = new User("Ivan");
        uploader.setId(1);

        when(fileService.upload(any(), any(), anyLong(), any())).thenReturn(created);
        when(userService.findById(1)).thenReturn(Optional.of(uploader));
        when(eventService.create(any())).thenReturn(new Event(uploader, created));

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_CREATED);
        verify(eventService).create(any());
    }

    @Test
    void doPost_shouldReturn400_whenNoFilePart() throws Exception {
        when(req.getPart("file")).thenReturn(null);

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(fileService, never()).upload(any(), any(), anyLong(), any());
    }

    @Test
    void doPost_shouldReturn400_whenFilePartIsEmpty() throws Exception {
        Part part = mock(Part.class);
        when(part.getSize()).thenReturn(0L);
        when(req.getPart("file")).thenReturn(part);

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(fileService, never()).upload(any(), any(), anyLong(), any());
    }

    // --- PUT update ---

    @Test
    void doPut_shouldReturn200_whenUpdated() throws Exception {
        File updated = file(1);
        when(req.getPathInfo()).thenReturn("/1");
        when(req.getReader()).thenReturn(reader("{\"name\":\"renamed.pdf\"}"));
        when(fileService.update(any())).thenReturn(updated);

        servlet.doPut(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void doPut_shouldReturn400_whenNoId() throws Exception {
        when(req.getPathInfo()).thenReturn(null);

        servlet.doPut(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(fileService, never()).update(any());
    }

    @Test
    void doPut_shouldReturn404_whenNotFound() throws Exception {
        when(req.getPathInfo()).thenReturn("/99");
        when(req.getReader()).thenReturn(reader("{\"name\":\"renamed.pdf\"}"));
        when(fileService.update(any())).thenThrow(new EntityNotFoundException("File", 99));

        servlet.doPut(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    // --- PATCH update ---

    @Test
    void doPatch_shouldReturn200_whenUpdated() throws Exception {
        File existing = file(1);
        when(req.getPathInfo()).thenReturn("/1");
        when(fileService.findById(1)).thenReturn(Optional.of(existing));
        when(req.getReader()).thenReturn(reader("{\"name\":\"renamed.pdf\"}"));
        when(fileService.update(any())).thenReturn(existing);

        servlet.doPatch(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void doPatch_shouldReturn400_whenNoId() throws Exception {
        when(req.getPathInfo()).thenReturn(null);

        servlet.doPatch(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(fileService, never()).update(any());
    }

    @Test
    void doPatch_shouldReturn404_whenFileNotFound() throws Exception {
        when(req.getPathInfo()).thenReturn("/99");
        when(fileService.findById(99)).thenReturn(Optional.empty());

        servlet.doPatch(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    // --- DELETE ---

    @Test
    void doDelete_shouldReturn200_whenDeleted() throws Exception {
        when(req.getPathInfo()).thenReturn("/1");

        servlet.doDelete(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
        verify(fileService).delete(1);
    }

    @Test
    void doDelete_shouldReturn409_whenFileHasEvents() throws Exception {
        when(req.getPathInfo()).thenReturn("/1");
        doThrow(new DependencyException("Cannot delete file 1: it has associated events"))
                .when(fileService).delete(1);

        servlet.doDelete(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_CONFLICT);
    }

    @Test
    void doDelete_shouldReturn404_whenNotFound() throws Exception {
        when(req.getPathInfo()).thenReturn("/99");
        doThrow(new EntityNotFoundException("File", 99)).when(fileService).delete(99);

        servlet.doDelete(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void doDelete_shouldReturn400_whenNoId() throws Exception {
        when(req.getPathInfo()).thenReturn(null);

        servlet.doDelete(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(fileService, never()).delete(any());
    }

    private static File file(int id) {
        File f = new File("report.pdf", "/storage/report.pdf");
        f.setId(id);
        f.setContentType("application/pdf");
        f.setSize(100L);
        return f;
    }

    private static Part uploadPart(String filename, String contentType, long size) throws Exception {
        Part part = mock(Part.class);
        when(part.getSize()).thenReturn(size);
        when(part.getContentType()).thenReturn(contentType);
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));
        when(part.getHeader("Content-Disposition"))
                .thenReturn("form-data; name=\"file\"; filename=\"" + filename + "\"");
        return part;
    }

    private static BufferedReader reader(String json) {
        return new BufferedReader(new StringReader(json));
    }
}
