package view.api;

import controller.utility.JWT_Provider;
import controller.utility.SessionLog;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import controller.menager.AutanticateMenager;
import controller.utility.PassCrypt;
@ApplicationPath("/api")
public class MyApplicationConfig extends ResourceConfig {
    public MyApplicationConfig() {
        packages("src.main.java");
    }
}
