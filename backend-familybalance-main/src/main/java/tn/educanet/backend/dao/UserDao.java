package tn.educanet.backend.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tn.educanet.backend.persistence.User;

@Repository
public interface UserDao extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByPhone(String phone);  // ← ajout
}