package controller.api;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api")
public class MyApplicationConfig extends ResourceConfig {
    public MyApplicationConfig() {
        packages("src.main.java");
    }
}
