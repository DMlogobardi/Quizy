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


    public <T> T reattachAndLoad(T entity, Consumer<T> loader) {
        if (entity == null) return null;

        // 1. Riaggancia
        T managedEntity = em.merge(entity);

        loader.accept(managedEntity);

        return managedEntity;
    }
}
