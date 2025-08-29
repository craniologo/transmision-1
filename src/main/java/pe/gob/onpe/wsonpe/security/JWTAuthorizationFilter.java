package pe.gob.onpe.wsonpe.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.gob.onpe.wsonpe.constants.CryptoValues;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTAuthorizationFilter extends OncePerRequestFilter {

  private static final String HEADER = "Authorization";
  private static final String PREFIX = "Bearer ";

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain chain)
                                  throws ServletException, IOException {
    try {

      if (!request.getRequestURI().contains("_next/webpack-hmr"))
        log.info("Endpoint called: {}", request.getRequestURI());

      boolean tokenExists = jwtTokenExists(request);
      if (tokenExists) {
        Claims claims = validateToken(request);
        if (claims.get("authorities") != null) {
          setUpSpringAuthentication(claims);
        } else {
          throw new UnsupportedJwtException("");
        }
      } else {
        if (request.getRequestURI().startsWith("/login") || request.getMethod().equals("GET")) {
          SecurityContextHolder.clearContext();
        } else {
          throw new MalformedJwtException("El token no existe");
        }
      }
      chain.doFilter(request, response);
    } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException |
             SignatureException e) {
      log.info("Error en el token: {}", e.getMessage());
      MensajeWsResponse wsResponse = new MensajeWsResponse(443, false, "El token no es válido");
      response.setContentType(APPLICATION_JSON_VALUE);
      new ObjectMapper().writeValue(response.getOutputStream(), wsResponse);
    } catch (Exception e) {
      log.error("Error interno del sistema: {}", e.getMessage());
      MensajeWsResponse wsResponse = new MensajeWsResponse(500, false, "Error interno del sistema");
      response.setContentType(APPLICATION_JSON_VALUE);
      new ObjectMapper().writeValue(response.getOutputStream(), wsResponse);
    }
  }

  private Claims validateToken(HttpServletRequest request) {
    String jwtToken = request.getHeader(HEADER).replace(PREFIX, "");

    return Jwts.parser().setSigningKey(CryptoValues.getJwtKey().getBytes()).parseClaimsJws(jwtToken).getBody();
  }

  private void setUpSpringAuthentication(Claims claims) {
    @SuppressWarnings("unchecked")
    List<String> authorities = (List) claims.get("authorities");

    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
      authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private boolean jwtTokenExists(HttpServletRequest request) {
    String authenticationHeader = request.getHeader(HEADER);
    return authenticationHeader != null && authenticationHeader.startsWith(PREFIX);
  }
}
