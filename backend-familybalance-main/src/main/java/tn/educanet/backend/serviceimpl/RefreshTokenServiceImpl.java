package tn.educanet.backend.serviceimpl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import tn.educanet.backend.dao.RefreshTokenDao;
import tn.educanet.backend.persistence.RefreshToken;
import tn.educanet.backend.persistence.User;
import tn.educanet.backend.service.RefreshTokenService;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

	@Resource
	private RefreshTokenDao refreshTokenDao;

	@Value("${jwt.refresh.expiration}")
	private Long refreshExpirationMs;

	@Override
	public RefreshToken createRefreshToken(User user) {
		RefreshToken refreshToken = refreshTokenDao.findByUser_Id(user.getId());
		if (refreshToken != null) {
			refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
			refreshToken.setToken(UUID.randomUUID().toString());
			return refreshTokenDao.save(refreshToken);
		} else {
			RefreshToken token = new RefreshToken();
			token.setUser(user);
			token.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
			token.setToken(UUID.randomUUID().toString());
			return refreshTokenDao.save(token);
		}
	}

	@Override
	public RefreshToken verifyExpiration(RefreshToken token) {
		if (token.getExpiryDate().isBefore(Instant.now())) {
			refreshTokenDao.delete(token);
			throw new RuntimeException("Refresh token expired. Please login again.");
		}
		return token;
	}

	@Override
	public Optional<RefreshToken> getByToken(String token) {
		return refreshTokenDao.findByToken(token);
	}

}