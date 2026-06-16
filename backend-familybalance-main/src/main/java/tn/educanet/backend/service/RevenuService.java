package tn.educanet.backend.service;

import java.util.List;
import java.util.Optional;

import tn.educanet.backend.persistence.Revenu;

public interface RevenuService {

	Optional<Revenu> findById(int id);
	List<Revenu> findAll();
	List<Revenu> findByMembreId(int membreId);
	List<Revenu> findByCompteId(int compteId);

	Revenu ajouter(Revenu revenu);
	Revenu edit(int id, Revenu revenu);
	void delete(int id);
	double getTotalAmount(int compteId);
	
	double getTotalByMembreThisMonth(int membreId);
	double getTotalByMembreThisYear(int membreId);
	double getTotalByCompteThisMonth(int compteId);

	/** Total des revenus d'un compte pour un mois/année donnés (perceptionDate).
	 *  Sert au calcul du report de revenu non distribué du mois précédent. */
	double getTotalByCompteForMonth(int compteId, int month, int year);
}