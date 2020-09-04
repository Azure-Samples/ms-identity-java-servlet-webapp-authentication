package com.microsoft.azuresamples.webapp;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.util.Properties;

@WebListener
public class Config implements ServletContextListener {
    private static Properties props;

    public void contextInitialized(ServletContextEvent event) {
        instantiateProperties();
    }
    public void contextDestroyed(ServletContextEvent event) {
    }

    private void instantiateProperties() {
        try {
            props = new Properties();
            props.load(getClass().getResourceAsStream("application.properties"));
            System.out.println(props.getProperty("aad.clientId"));

        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("couldn't load properties file");
        }
    }

    public static String getProperty(String key) {
        if (props != null)
            return props.getProperty(key);

        System.out.println("couldn't load properties file");
        return null;
    }
}



