package de.doubleslash.keeptime.REST_API.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Properties;

@Configuration
public class apiPort {
   private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

   public static final String filePath = "application.properties";

   @Value("${server.port:8085}")
   private int port;

   public void loadSettings() {
      try (final FileReader fr = new FileReader(filePath)) {
         final Properties properties = new Properties();
         properties.load(fr);
         setPort(Integer.parseInt(properties.getProperty("server.port")));
      } catch (IOException e) {
         LOG.error("Properties could not be loaded from file '{}'", filePath, e);
      }
   }

   @Autowired
   private ConfigurableServletWebServerFactory serverFactory;

   public void customize() {
      if (serverFactory instanceof TomcatServletWebServerFactory) {
         ((TomcatServletWebServerFactory) serverFactory).setPort(port);
         loadSettings();
      }
   }

   public void setPort(int newPort) {
      this.port = newPort;
      customize();
   }

   public int getPort() {
      return port;
   }
}
