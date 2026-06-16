package tn.educanet.backend.service;

import java.util.List;
import java.util.Optional;

import tn.educanet.backend.persistence.Compte;
import tn.educanet.backend.persistence.Famille;
import tn.educanet.backend.persistence.Membre;

public interface FamilleService {

	Famille save(Famille famille);
	Optional<Famille> findById(int id);
	List<Famille> findAll();
	List<Famille> findByCreatorId(int creatorId);
	void deleteById(int id);

	String getNom(int familleId);
	List<Membre> getListeMembres(int familleId);
	Compte getCompte(int familleId);
}