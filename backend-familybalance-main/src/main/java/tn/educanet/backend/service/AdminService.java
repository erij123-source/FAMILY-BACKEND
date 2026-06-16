package tn.educanet.backend.service;

import java.util.List;
import java.util.Optional;

import tn.educanet.backend.persistence.Admin;
import tn.educanet.backend.persistence.Famille;
import tn.educanet.backend.persistence.Membre;

public interface AdminService {

    Admin save(Admin admin);
    Optional<Admin> findById(int id);
    Optional<Admin> findFirstByUser_Id(int userId);
    Optional<Admin> findFirstByFamille_Id(int familleId);

    List<Admin> findAll();
    void deleteById(int id);

    Famille createFamily(String name, int creatorId);
    void deleteFamily(int familleId);
    void inviteMember(int userId, int familleId);
    void removeMember(int membreId);
    void changeMemberRole(int membreId, Membre.Role role);
    void setBudget(int membreId, double budget);

    // ── FIX #5 : Reset budgets au début d'un nouveau mois ──────────────
    void resetBudgetsForNewMonth();
}