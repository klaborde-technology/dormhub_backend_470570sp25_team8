package edu.uscb.csci470sp25.dormhub_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import edu.uscb.csci470sp25.dormhub_backend.model.User;
import edu.uscb.csci470sp25.dormhub_backend.repository.UserRepository;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
   private final JwtUtil jwtUtil;
   private final UserRepository userRepository;
   public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
       this.jwtUtil = jwtUtil;
       this.userRepository = userRepository;
   }
   
   @Override
   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
           throws ServletException, IOException {

       String token = extractToken(request);
       if (token != null) {
           try {
               Claims claims = jwtUtil.validateToken(token);
               String email = claims.getSubject();
               String role = claims.get("role", String.class);

               // ✅ Get the actual User from DB
               User user = userRepository.findByEmail(email)
                       .orElseThrow(() -> new RuntimeException("User not found"));

               UsernamePasswordAuthenticationToken authentication =
                       new UsernamePasswordAuthenticationToken(user, null,
                               Collections.singletonList(new SimpleGrantedAuthority(role)));

               SecurityContextHolder.getContext().setAuthentication(authentication);

           } catch (ExpiredJwtException e) {
               response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
               return;
           } catch (JwtException e) {
               response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
               return;
           }
       }

       filterChain.doFilter(request, response);
   }

   private String extractToken(HttpServletRequest request) {
       String authHeader = request.getHeader("Authorization");
       if (authHeader != null && authHeader.startsWith("Bearer ")) {
           return authHeader.substring(7);
       }
       return null;
   }
}