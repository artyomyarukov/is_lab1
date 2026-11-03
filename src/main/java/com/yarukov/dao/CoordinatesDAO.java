package com.yarukov.dao;

import com.yarukov.model.Coordinates;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class CoordinatesDAO {

    @PersistenceContext
    private EntityManager em;

    public Coordinates save(Coordinates coordinates) {
        em.persist(coordinates);
        return coordinates;
    }

    public Optional<Coordinates> findById(Long id) {
        return Optional.ofNullable(em.find(Coordinates.class, id));
    }

    public List<Coordinates> findAll() {
        return em.createQuery("SELECT c FROM Coordinates c", Coordinates.class)
                .getResultList();
    }

    public Coordinates update(Coordinates coordinates) {
        return em.merge(coordinates);
    }

    public void delete(Coordinates coordinates) {
        em.remove(em.contains(coordinates) ? coordinates : em.merge(coordinates));
    }


    public boolean isLinkedToAnyCity(Long coordinatesId) {
        Long count = em.createQuery(
                        "SELECT COUNT(c) FROM City c WHERE c.coordinates.id = :coordId", Long.class)
                .setParameter("coordId", coordinatesId)
                .getSingleResult();
        return count > 0;
    }

    public boolean existsByXAndY(Float x, Integer y) {
        Long count = em.createQuery(
                        "SELECT COUNT(c) FROM Coordinates c WHERE c.x = :x AND c.y = :y", Long.class)
                .setParameter("x", x)
                .setParameter("y", y)
                .getSingleResult();
        return count > 0;
    }
}