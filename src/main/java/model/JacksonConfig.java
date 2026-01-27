package model;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper;

    public JacksonConfig() {
        mapper = new ObjectMapper();

        // 1. Modulo Hibernate (già presente)
        Hibernate6Module h6m = new Hibernate6Module();
        h6m.enable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
        mapper.registerModule(h6m);

        // 2. MODULO JSR310 (La soluzione al tuo errore)
        // Questo permette a Jackson di capire LocalDateTime, LocalDate, ecc.
        mapper.registerModule(new JavaTimeModule());

        // 3. OPZIONALE: Formattazione leggibile
        // Senza questo, le date potrebbero apparire come liste di numeri [2026, 1, 27...]
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}