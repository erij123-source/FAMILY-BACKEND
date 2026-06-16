package tn.educanet.backend.service;

import java.util.Optional;

import tn.educanet.backend.persistence.RefreshToken;
import tn.educanet.backend.persistence.User;

public interface RefreshTokenService {
	RefreshToken createRefreshToken(User user);
	RefreshToken verifyExpiration(RefreshToken token);
	Optional<RefreshToken> getByToken(String token);
}