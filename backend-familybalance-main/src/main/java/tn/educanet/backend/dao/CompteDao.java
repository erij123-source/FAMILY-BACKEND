package tn.educanet.backend.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tn.educanet.backend.persistence.Compte;

@Repository
public interface CompteDao extends JpaRepository<Compte, Integer> {
	Optional<Compte> findByFamilleId(int familleId);
}