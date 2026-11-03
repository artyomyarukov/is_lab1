package com.yarukov.dao;

import com.yarukov.model.CarCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CarCodeDAO {

    @PersistenceContext
    private EntityManager em;

    public CarCode save(CarCode carCode) {
        em.persist(carCode);
        return carCode;
    }

    public Optional<CarCode> findById(Long id) {
        return Optional.ofNullable(em.find(CarCode.class, id));
    }

    public List<CarCode> findAll() {
        return em.createQuery("SELECT c FROM CarCode c ORDER BY c.id ASC", CarCode.class)
                .getResultList();
    }

    public CarCode update(CarCode carCode) {
        return em.merge(carCode);
    }

    public void delete(CarCode carCode) {
        em.remove(em.contains(carCode) ? carCode : em.merge(carCode));
    }

    public boolean isLinkedToAnyCity(Long codeId) {
        Long count = em.createQuery(
                        "SELECT COUNT(c) FROM City c JOIN c.carCode cc WHERE cc.id = :codeId", Long.class)
                .setParameter("codeId", codeId)
                .getSingleResult();
        return count > 0;
    }
}