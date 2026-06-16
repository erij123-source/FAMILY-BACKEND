package tn.educanet.backend.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tn.educanet.backend.persistence.Membre;

@Repository
public interface MembreDao extends JpaRepository<Membre, Integer> {
	List<Membre> findByFamilleId(int familleId);
	Optional<Membre> findByUserIdAndFamilleId(int userId, int familleId);
	Optional<Membre> findFirstByUser_Id(int userId);
}