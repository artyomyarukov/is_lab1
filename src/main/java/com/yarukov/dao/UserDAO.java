package com.yarukov.dao;

import com.yarukov.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDAO {

    @PersistenceContext
    private EntityManager em;

    public User save(User user) {
        em.persist(user);
        return user;
    }

    public Optional<User> findByUsername(String username) {
        List<User> users = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultList();
        return users.stream().findFirst();
    }
}