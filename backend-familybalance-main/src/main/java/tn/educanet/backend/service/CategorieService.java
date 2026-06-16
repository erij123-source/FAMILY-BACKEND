package tn.educanet.backend.service;

import java.util.List;
import java.util.Optional;

import tn.educanet.backend.persistence.Categorie;

public interface CategorieService {

    Categorie save(Categorie categorie);
    Optional<Categorie> findById(int id);
    List<Categorie> findAll();
    List<Categorie> findByType(Categorie.Type type);
    void deleteById(int id);

    List<Categorie> findDefaultCategories();
    List<Categorie> findDefaultCategoriesByType(Categorie.Type type);

    Categorie addCustomCategory(Categorie categorie, int userId);
    List<Categorie> findCustomCategoriesByUser(int userId);
    List<Categorie> findAllByType(Categorie.Type type);
}