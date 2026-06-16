package tn.educanet.backend.config;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	  private final JwtUtil jwtUtil;

	    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
	        this.jwtUtil = jwtUtil;
	    }

	    @Override
	    protected void doFilterInternal(HttpServletRequest request,
	                                    HttpServletResponse response,
	                                    FilterChain filterChain)
	            throws IOException, jakarta.servlet.ServletException {

	        String header = request.getHeader("Authorization");

	        if (header != null && header.startsWith("Bearer ")) {
	            String token = header.substring(7);
	            if (jwtUtil.validateToken(token)) {
	                String username = jwtUtil.extractUsername(token);

	                UsernamePasswordAuthenticationToken auth =
	                        new UsernamePasswordAuthenticationToken(
	                                username, null, Collections.emptyList());

	                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	                SecurityContextHolder.getContext().setAuthentication(auth);
	            }
	        }

	        filterChain.doFilter(request, response);
	    }
}
