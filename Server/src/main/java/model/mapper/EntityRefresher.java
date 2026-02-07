package model.mapper;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.function.Consumer;

@ApplicationScoped
public class EntityRefresher {

    @Inject
    private EntityManager em;

    public <T> T reattach(T entity) {
        if (entity == null) return null;

        return em.merge(entity);
    }
}
