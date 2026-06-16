package tn.educanet.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tn.educanet.backend.persistence.Revenu;

@Repository
public interface RevenuDao extends JpaRepository<Revenu, Integer> {
	List<Revenu> findByMembreId(int membreId);
	List<Revenu> findByCompteId(int compteId);
	
	void deleteByMembre_Id(int membreId);


}