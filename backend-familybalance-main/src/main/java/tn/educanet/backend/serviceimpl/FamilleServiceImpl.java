package tn.educanet.backend.serviceimpl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import tn.educanet.backend.dao.AdminDao;
import tn.educanet.backend.dao.CompteDao;
import tn.educanet.backend.dao.FamilleDao;
import tn.educanet.backend.dao.MembreDao;
import tn.educanet.backend.dao.UserDao;
import tn.educanet.backend.persistence.Admin;
import tn.educanet.backend.persistence.Compte;
import tn.educanet.backend.persistence.Famille;
import tn.educanet.backend.persistence.Membre;
import tn.educanet.backend.persistence.User;
import tn.educanet.backend.service.FamilleService;

@Service
public class FamilleServiceImpl implements FamilleService {

    @Resource
    private FamilleDao familleDao;

    @Resource
    private MembreDao membreDao;

    @Resource
    private CompteDao compteDao;

    @Resource
    private AdminDao adminDao;

    @Resource
    private UserDao userDao;

    @Override
    @Transactional
    public Famille save(Famille famille) {
        // Re-attach the creator User from the DB so Hibernate doesn't choke
        // on a detached entity when the client posts {"creator":{"id":N}}.
        if (famille.getCreator() != null && famille.getCreator().getId() > 0) {
            User managed = userDao.findById(famille.getCreator().getId()).orElse(null);
            if (managed != null) famille.setCreator(managed);
        }
        Famille savedFamille = familleDao.save(famille);

        boolean membreDejaExiste = membreDao
            .findByUserIdAndFamilleId(savedFamille.getCreator().getId(), savedFamille.getId())
            .isPresent();

        if (!membreDejaExiste) {
            Membre membre = new Membre();
            membre.setUser(savedFamille.getCreator());
            membre.setFamille(savedFamille);
            membre.setRole(Membre.Role.ADMINISTRATEUR);
            membreDao.save(membre);
        }

        boolean adminDejaExiste = adminDao
            .findByUser_IdAndFamille_Id(savedFamille.getCreator().getId(), savedFamille.getId())
            .isPresent();

        if (!adminDejaExiste) {
            Admin admin = new Admin();
            admin.setUser(savedFamille.getCreator());
            admin.setFamille(savedFamille);
            adminDao.save(admin);
        }

        return savedFamille;
    }

    @Override
    public Optional<Famille> findById(int id) {
        return familleDao.findById(id);
    }

    @Override
    public List<Famille> findAll() {
        return familleDao.findAll();
    }

    @Override
    public List<Famille> findByCreatorId(int creatorId) {
        return familleDao.findByCreatorId(creatorId);
    }

    @Override
    public void deleteById(int id) {
        familleDao.deleteById(id);
    }

    @Override
    public String getNom(int familleId) {
        return familleDao.findById(familleId)
                .map(Famille::getName)
                .orElseThrow(() -> new RuntimeException("Famille not found"));
    }

    @Override
    public List<Membre> getListeMembres(int familleId) {
        return membreDao.findByFamilleId(familleId);
    }

    @Override
    public Compte getCompte(int familleId) {
        return compteDao.findByFamilleId(familleId)
                .orElseThrow(() -> new RuntimeException("Compte not found"));
    }
}