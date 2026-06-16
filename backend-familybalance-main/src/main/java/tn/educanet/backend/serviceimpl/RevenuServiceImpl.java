package tn.educanet.backend.serviceimpl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import tn.educanet.backend.dao.CategorieDao;
import tn.educanet.backend.dao.RevenuDao;
import tn.educanet.backend.persistence.Categorie;
import tn.educanet.backend.persistence.Revenu;
import tn.educanet.backend.service.RevenuService;

@Service
public class RevenuServiceImpl implements RevenuService {

	@Resource
	private RevenuDao revenuDao;

	@Resource
	private CategorieDao categorieDao;

	@Override
	public Optional<Revenu> findById(int id) {
		return revenuDao.findById(id);
	}

	@Override
	public List<Revenu> findAll() {
		return revenuDao.findAll();
	}

	@Override
	public List<Revenu> findByMembreId(int membreId) {
		return revenuDao.findByMembreId(membreId);
	}

	@Override
	public List<Revenu> findByCompteId(int compteId) {
		return revenuDao.findByCompteId(compteId);
	}

	@Override
	public Revenu ajouter(Revenu revenu) {
		// Pas de contrôle ensureCanSpend ici : un revenu ALIMENTE le compte,
		// il ne consomme pas le budget. Un membre dont le budget est épuisé
		// doit toujours pouvoir enregistrer une rentrée d'argent (le blocage
		// ne s'applique qu'aux charges et factures — opérations sortantes).
		attachCategorie(revenu);
		return revenuDao.save(revenu);
	}

	@Override
	public Revenu edit(int id, Revenu revenu) {
		revenu.setId(id);
		attachCategorie(revenu);
		return revenuDao.save(revenu);
	}

	/**
	 * Reload the managed {@link Categorie} when the client sent a stub
	 * containing only its id (mobile clients tend to do this).
	 */
	private void attachCategorie(Revenu revenu) {
		Categorie stub = revenu.getCategorie();
		if (stub != null && stub.getId() > 0) {
			Categorie managed = categorieDao.findById(stub.getId()).orElse(null);
			if (managed != null) revenu.setCategorie(managed);
		}
	}

	@Override
	public void delete(int id) {
		revenuDao.deleteById(id);
	}

	@Override
	public double getTotalAmount(int compteId) {
		return revenuDao.findByCompteId(compteId)
				.stream()
				.mapToDouble(Revenu::getAmount)
				.sum();
	}

	@Override
	public double getTotalByMembreThisMonth(int membreId) {
	    int currentMonth = java.time.LocalDate.now().getMonthValue();
	    int currentYear = java.time.LocalDate.now().getYear();
	    return revenuDao.findByMembreId(membreId)
	            .stream()
	            .filter(r -> r.getPerceptionDate() != null
	                    && r.getPerceptionDate().getMonthValue() == currentMonth
	                    && r.getPerceptionDate().getYear() == currentYear)
	            .mapToDouble(Revenu::getAmount)
	            .sum();
	}

	@Override
	public double getTotalByMembreThisYear(int membreId) {
	    int currentYear = java.time.LocalDate.now().getYear();
	    return revenuDao.findByMembreId(membreId)
	            .stream()
	            .filter(r -> r.getPerceptionDate() != null
	                    && r.getPerceptionDate().getYear() == currentYear)
	            .mapToDouble(Revenu::getAmount)
	            .sum();
	}
	
	@Override
	public double getTotalByCompteThisMonth(int compteId) {
	    int currentMonth = java.time.LocalDate.now().getMonthValue();
	    int currentYear  = java.time.LocalDate.now().getYear();
	    return getTotalByCompteForMonth(compteId, currentMonth, currentYear);
	}

	@Override
	public double getTotalByCompteForMonth(int compteId, int month, int year) {
	    return revenuDao.findByCompteId(compteId)
	            .stream()
	            .filter(r -> r.getPerceptionDate() != null
	                    && r.getPerceptionDate().getMonthValue() == month
	                    && r.getPerceptionDate().getYear() == year)
	            .mapToDouble(Revenu::getAmount)
	            .sum();
	}

}