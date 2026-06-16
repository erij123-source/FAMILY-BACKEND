package tn.educanet.backend.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tn.educanet.backend.persistence.Admin;

@Repository
public interface AdminDao extends JpaRepository<Admin, Integer> {
    Optional<Admin> findFirstByUser_Id(int userId); 
    Optional<Admin> findFirstByFamille_Id(int familleId);
    
    Optional<Admin> findByUser_IdAndFamille_Id(int userId, int familleId);

    
}