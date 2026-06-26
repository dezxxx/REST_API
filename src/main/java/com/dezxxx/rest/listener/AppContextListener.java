package com.dezxxx.rest.listener;

import com.dezxxx.rest.repository.impl.EventRepositoryImpl;
import com.dezxxx.rest.repository.impl.FileRepositoryImpl;
import com.dezxxx.rest.repository.impl.UserRepositoryImpl;
import com.dezxxx.rest.service.EventService;
import com.dezxxx.rest.service.FileService;
import com.dezxxx.rest.service.UserService;
import com.dezxxx.rest.util.FlyWayConfig;
import com.dezxxx.rest.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Application starting...");

        FlyWayConfig.runMigrations();

        ServletContext ctx = sce.getServletContext();

        UserRepositoryImpl userRepository = new UserRepositoryImpl();
        FileRepositoryImpl fileRepository = new FileRepositoryImpl();
        EventRepositoryImpl eventRepository = new EventRepositoryImpl();

        ctx.setAttribute("userService", new UserService(userRepository));
        ctx.setAttribute("fileService", new FileService(fileRepository, eventRepository));
        ctx.setAttribute("eventService", new EventService(eventRepository, userRepository, fileRepository));

        log.info("Application started successfully");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        HibernateUtil.shutdown();
        log.info("Application stopped");
    }

}