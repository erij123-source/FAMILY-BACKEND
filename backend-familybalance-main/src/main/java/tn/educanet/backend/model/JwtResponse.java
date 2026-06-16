package tn.educanet.backend.model;

public class JwtResponse {

	private String token;
	private String refreshToken;

	public JwtResponse(String token, String refreshToken) {
		super();
		this.token = token;
		this.refreshToken = refreshToken;
	}

	public JwtResponse(String token) {
		super();
		this.token = token;
	}

	public JwtResponse() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
