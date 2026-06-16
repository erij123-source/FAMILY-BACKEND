package tn.educanet.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tn.educanet.backend.persistence.Famille;

@Repository
public interface FamilleDao extends JpaRepository<Famille, Integer> {
	List<Famille> findByCreatorId(int creatorId);
}