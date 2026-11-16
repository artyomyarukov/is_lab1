package com.yarukov.dao;

import com.yarukov.model.Human;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class HumanDAO {


    @PersistenceContext
    private EntityManager em;

    public Human save(Human human) {

        em.persist(human);
        return human;
    }

    public Optional<Human> findById(Long id) {
        Human human = em.find(Human.class, id);
        return Optional.ofNullable(human);
    }

    public List<Human> findAll() {
        return em.createQuery("SELECT h FROM Human h", Human.class)
                .getResultList();
    }

    public Human update(Human human) {
        return em.merge(human);
    }

    public void delete(Human human) {
        em.remove(em.contains(human) ? human : em.merge(human));
    }

    public boolean isLinkedToAnyCity(Long humanId) {
        Long count = em.createQuery(
                        "SELECT COUNT(c) FROM City c WHERE c.governor.id = :humanId", Long.class)
                .setParameter("humanId", humanId)
                .getSingleResult();
        return count > 0;
    }
}