package tn.educanet.backend.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tn.educanet.backend.persistence.RefreshToken;

@Repository
public interface RefreshTokenDao extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByToken(String token);

	void deleteByUserId(Long userId);

	RefreshToken findByUser_Id(long idUser);
}
