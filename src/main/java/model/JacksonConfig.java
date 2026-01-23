package model;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper;

    public JacksonConfig() {
        mapper = new ObjectMapper();
        Hibernate6Module h6m = new Hibernate6Module();

        // FORZA Jackson a caricare le liste Lazy anche se la sessione Hibernate è chiusa.
        // Questo trasforma l'errore 500 in una lista JSON piena di dati.
        h6m.enable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);

        mapper.registerModule(h6m);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}