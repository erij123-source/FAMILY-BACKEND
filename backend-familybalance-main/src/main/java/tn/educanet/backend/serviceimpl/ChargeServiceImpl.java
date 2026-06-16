package tn.educanet.backend.serviceimpl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import tn.educanet.backend.dao.ChargeDao;
import tn.educanet.backend.dao.CompteDao;
import tn.educanet.backend.persistence.Charge;
import tn.educanet.backend.persistence.Compte;
import tn.educanet.backend.service.ChargeService;
import tn.educanet.backend.service.MembreService;

@Service
public class ChargeServiceImpl implements ChargeService {

    @Resource
    private ChargeDao chargeDao;

    @Resource
    private CompteDao compteDao;

    @Resource
    private MembreService membreService;

    @Override
    public Optional<Charge> findById(int id) {
        return chargeDao.findById(id);
    }

    @Override
    public List<Charge> findAll() {
        return chargeDao.findAll();
    }

    @Override
    public List<Charge> findByMembreId(int membreId) {
        return chargeDao.findByMembreId(membreId);
    }

    @Override
    public List<Charge> findByCompteId(int compteId) {
        return chargeDao.findByCompteId(compteId);
    }

    @Override
    public List<Charge> findByType(Charge.Type type) {
        return chargeDao.findByType(type);
    }

    @Override
    public Charge add(Charge charge) {
        if (charge.getMembre() != null && charge.getMembre().getId() > 0) {
            membreService.ensureCanSpend(charge.getMembre().getId());
        }
        if (charge.getPaymentDate() == null) {
            charge.setPaymentDate(java.time.LocalDate.now());
        }
        return chargeDao.save(charge);
    }

    @Override
    public Charge edit(int id, Charge charge) {
        charge.setId(id);
        if (charge.getPaymentDate() == null) {
            charge.setPaymentDate(java.time.LocalDate.now());
        }
        return chargeDao.save(charge);
    }

    @Override
    public void delete(int id) {
        chargeDao.deleteById(id);
    }

    @Override
    public boolean checkBudgetLimit(int compteId, double montant) {
        Optional<Compte> compte = compteDao.findById(compteId);
        if (compte.isPresent()) {
        	double budgetRestant = compte.get().getRemainingBalance();


            return montant <= budgetRestant;
        }
        return false;
    }
    
    // Les totaux mensuels/annuels sont basés sur paymentDate (cash effectif),
    // pas chargeDate (mois auquel la charge est rattachée pour l'affichage).
    // Fallback sur chargeDate pour les lignes anciennes sans paymentDate.
    private static java.time.LocalDate effectivePaymentDate(Charge c) {
        return c.getPaymentDate() != null ? c.getPaymentDate() : c.getChargeDate();
    }

    @Override
    public double getTotalByMembreThisMonth(int membreId) {
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        int currentYear = java.time.LocalDate.now().getYear();
        return chargeDao.findByMembreId(membreId)
                .stream()
                .filter(c -> {
                    java.time.LocalDate d = effectivePaymentDate(c);
                    return d != null
                            && d.getMonthValue() == currentMonth
                            && d.getYear() == currentYear;
                })
                .mapToDouble(Charge::getAmount)
                .sum();
    }

    @Override
    public double getTotalByMembreThisYear(int membreId) {
        int currentYear = java.time.LocalDate.now().getYear();
        return chargeDao.findByMembreId(membreId)
                .stream()
                .filter(c -> {
                    java.time.LocalDate d = effectivePaymentDate(c);
                    return d != null && d.getYear() == currentYear;
                })
                .mapToDouble(Charge::getAmount)
                .sum();
    }

    @Override
    public double getTotalByCompteThisMonth(int compteId) {
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        int currentYear  = java.time.LocalDate.now().getYear();
        return chargeDao.findByCompteId(compteId)
                .stream()
                .filter(c -> {
                    java.time.LocalDate d = effectivePaymentDate(c);
                    return d != null
                            && d.getMonthValue() == currentMonth
                            && d.getYear() == currentYear;
                })
                .mapToDouble(Charge::getAmount)
                .sum();
    }
    
    

}
