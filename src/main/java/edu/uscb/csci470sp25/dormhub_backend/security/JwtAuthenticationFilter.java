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
import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
   private final JwtUtil jwtUtil;
   public JwtAuthenticationFilter(JwtUtil jwtUtil) {
       this.jwtUtil = jwtUtil;
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
               // ✅ Set authentication in security context
               UsernamePasswordAuthenticationToken authentication =
                       new UsernamePasswordAuthenticationToken(email, null, Collections.singletonList(new SimpleGrantedAuthority(role)));
               SecurityContextHolder.getContext().setAuthentication(authentication);
           } catch (ExpiredJwtException e) {
               System.out.println("❌ JWT Token Expired: " + e.getMessage());
               SecurityContextHolder.clearContext(); // ✅ Clear security context
               response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
               return;
           } catch (JwtException e) {
               System.out.println("❌ Invalid JWT Token: " + e.getMessage());
               SecurityContextHolder.clearContext(); // ✅ Clear security context
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