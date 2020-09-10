package com.microsoft.azuresamples.webapp;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

@WebListener
public class Config implements ServletContextListener {
    private static final Properties props = Config.instantiateProperties();

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        // Fill this in if need be
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        // Fill this in if need be.
    }

    private static Properties instantiateProperties() {
        Properties props = new Properties();
        try {
            props.load(Config.class.getClassLoader().getResourceAsStream("authentication.properties"));
            System.out.println(props.getProperty("aad.clientId"));
            return props;

        } catch (final IOException ex) {
            ex.printStackTrace();
            System.out.println("couldn't load properties file");
        }
        return props;
    }

    public static String getProperty(final String key) {
        if (props != null)
            return props.getProperty(key);

        System.out.println("couldn't load properties file");
        return null;
    }
}



