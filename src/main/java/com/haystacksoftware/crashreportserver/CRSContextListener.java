package com.haystacksoftware.crashreportserver;

import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class CRSContextListener implements ServletContextListener {
    /*
     * init parameters:
     * mail.recipient
     * mail.smtp.host
     * crash.reports.dir
     */
    public void contextInitialized(ServletContextEvent arg0) {
        URL log4jProperties = getClass().getClassLoader().getResource("/log4j/log4j.properties");
        PropertyConfigurator.configure(log4jProperties);
        ServletContext sc = arg0.getServletContext();
        validateInitParameter(sc, "mail.recipient");
        validateInitParameter(sc, "mail.smtp.host");
        validateInitParameter(sc, "crash.reports.dir");
        Logger.getLogger(CRSContextListener.class).info("context initialized");
    }

    public void contextDestroyed(ServletContextEvent arg0) {
        Logger.getLogger(CRSContextListener.class).info("context destroyed");
    }
    private void validateInitParameter(ServletContext sc, String paramName) {
        if (sc.getInitParameter(paramName) == null) {
            throw new RuntimeException("missing init-param " + paramName);
        }
    }
}
