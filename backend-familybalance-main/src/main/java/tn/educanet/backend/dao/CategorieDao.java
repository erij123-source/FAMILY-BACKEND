package tn.educanet.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tn.educanet.backend.persistence.Categorie;

@Repository
public interface CategorieDao extends JpaRepository<Categorie, Integer> {
    List<Categorie> findByType(Categorie.Type type);
    List<Categorie> findByIsDefaultTrue();
    List<Categorie> findByTypeAndIsDefaultTrue(Categorie.Type type);
    List<Categorie> findByCreatedById(int userId);
}