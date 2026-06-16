package tn.educanet.backend.service;

import java.util.List;
import java.util.Optional;

import tn.educanet.backend.persistence.Charge;

public interface ChargeService {

    Optional<Charge> findById(int id);
    List<Charge> findAll();
    List<Charge> findByMembreId(int membreId);
    List<Charge> findByCompteId(int compteId);
    List<Charge> findByType(Charge.Type type);

    Charge add(Charge charge);

    Charge edit(int id, Charge charge);

    void delete(int id);

    boolean checkBudgetLimit(int compteId, double montant);
    
    double getTotalByMembreThisMonth(int membreId);
    double getTotalByMembreThisYear(int membreId);
    double getTotalByCompteThisMonth(int compteId);
}