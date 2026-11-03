package com.yarukov.dao;

import com.yarukov.model.City;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CityDAO {

    @PersistenceContext
    private EntityManager em;

    public City save(City city) {
        em.persist(city);
        return city;
    }

    public Optional<City> findById(Long id) {
        return Optional.ofNullable(em.find(City.class, id));
    }

    public List<City> findAll() {
        return em.createQuery("SELECT c FROM City c ORDER BY c.id ASC", City.class)
                .getResultList();
    }

    public City update(City city) {
        return em.merge(city);
    }

    public void delete(City city) {
        em.remove(em.contains(city) ? city : em.merge(city));
    }

    public Optional<City> findCapital() {
        List<City> capitals = em.createQuery(
                        "SELECT c FROM City c WHERE c.capital = true", City.class)
                .getResultList();
        return capitals.stream().findFirst();
    }


    public List<City> findCitiesWithSmallestPopulation(Long excludeCityId, int limit) {
        return em.createQuery(
                        "SELECT c FROM City c WHERE c.id <> :excludeId ORDER BY c.population ASC", City.class)
                .setParameter("excludeId", excludeCityId)
                .setMaxResults(limit)
                .getResultList();
    }
}