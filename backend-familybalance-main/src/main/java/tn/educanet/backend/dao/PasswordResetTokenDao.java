package tn.educanet.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.educanet.backend.persistence.PasswordResetToken;
import java.util.Optional;

public interface PasswordResetTokenDao extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByCodeAndUsedFalse(String code);
    void deleteByUser_Id(int userId);
}