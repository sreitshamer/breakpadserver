package com.haystacksoftware.crashreportserver;

import java.net.URL;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class CRSContextListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent arg0) {
        URL log4jProperties = getClass().getClassLoader().getResource("/log4j/log4j.properties");
        PropertyConfigurator.configure(log4jProperties);
        Logger.getLogger(CRSContextListener.class).info("context initialized");
    }

    public void contextDestroyed(ServletContextEvent arg0) {
        Logger.getLogger(CRSContextListener.class).info("context destroyed");
    }

}
