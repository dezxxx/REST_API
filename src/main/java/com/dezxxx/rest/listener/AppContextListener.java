package com.dezxxx.rest.listener;

import com.dezxxx.rest.repository.impl.EventRepositoryImpl;
import com.dezxxx.rest.repository.impl.FileRepositoryImpl;
import com.dezxxx.rest.repository.impl.UserRepositoryImpl;
import com.dezxxx.rest.service.impl.EventServiceImpl;
import com.dezxxx.rest.service.impl.FileServiceImpl;
import com.dezxxx.rest.service.impl.UserServiceImpl;
import com.dezxxx.rest.util.HibernateUtil;
import org.flywaydb.core.Flyway;
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

        runFlyway();

        ServletContext ctx = sce.getServletContext();

        UserRepositoryImpl userRepository = new UserRepositoryImpl();
        FileRepositoryImpl fileRepository = new FileRepositoryImpl();
        EventRepositoryImpl eventRepository = new EventRepositoryImpl();

        ctx.setAttribute("userService", new UserServiceImpl(userRepository));
        ctx.setAttribute("fileService", new FileServiceImpl(fileRepository));
        ctx.setAttribute("eventService", new EventServiceImpl(eventRepository, userRepository, fileRepository));

        log.info("Application started successfully");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        HibernateUtil.shutdown();
        log.info("Application stopped");
    }

    private void runFlyway() {
        Flyway flyway = Flyway.configure()
                .dataSource(
                        "jdbc:mysql://localhost:3306/file_manager_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                        "root",
                        "dezxxx"
                )
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        log.info("Flyway migration completed");
    }
}
