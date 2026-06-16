package tn.educanet.backend.serviceimpl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import tn.educanet.backend.dao.CategorieDao;
import tn.educanet.backend.dao.UserDao;
import tn.educanet.backend.persistence.Categorie;
import tn.educanet.backend.persistence.User;
import tn.educanet.backend.service.CategorieService;

@Service
public class CategorieServiceImpl implements CategorieService {

    @Resource
    private CategorieDao categorieDao;

    @Resource
    private UserDao userDao;

    @Override
    public Categorie save(Categorie categorie) {
        return categorieDao.save(categorie);
    }

    @Override
    public Optional<Categorie> findById(int id) {
        return categorieDao.findById(id);
    }

    @Override
    public List<Categorie> findAll() {
        return categorieDao.findAll();
    }

    @Override
    public List<Categorie> findByType(Categorie.Type type) {
        return categorieDao.findByType(type);
    }

    @Override
    public void deleteById(int id) {
        categorieDao.deleteById(id);
    }

    @Override
    public List<Categorie> findDefaultCategories() {
        return categorieDao.findByIsDefaultTrue();
    }

    @Override
    public List<Categorie> findDefaultCategoriesByType(Categorie.Type type) {
        return categorieDao.findByTypeAndIsDefaultTrue(type);
    }

    @Override
    public Categorie addCustomCategory(Categorie categorie, int userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        categorie.setCreatedBy(user);
        categorie.setDefault(false);
        return categorieDao.save(categorie);
    }

    @Override
    public List<Categorie> findCustomCategoriesByUser(int userId) {
        return categorieDao.findByCreatedById(userId);
    }

    @Override
    public List<Categorie> findAllByType(Categorie.Type type) {
        return categorieDao.findByType(type);
    }

}