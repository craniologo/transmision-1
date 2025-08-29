package pe.gob.onpe.wsonpe.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import pe.gob.onpe.wsonpe.constants.CryptoValues;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

class JWTAuthorizationFilterTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @Mock
  private SecurityContext securityContext;

  @InjectMocks
  private JWTAuthorizationFilter jwtAuthorizationFilter;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void doFilterInternal_WithValidToken_ShouldSetAuthentication() throws Exception {
    // Arrange
    String validToken = "Bearer validToken";
    ArrayList<String> authorities = new ArrayList<>();
    authorities.add("ROLE_USER");

    // Configurar request para que devuelva un token válido
    when(request.getHeader("Authorization")).thenReturn(validToken);
    when(request.getRequestURI()).thenReturn("/api/data");

    // Mockear contexto de seguridad
    try (MockedStatic<SecurityContextHolder> securityContextHolderMock = mockStatic(SecurityContextHolder.class)) {
      securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      // Mockear Jwts y Claims para versiones anteriores a 0.11.0
      try (MockedStatic<Jwts> jwtsMock = mockStatic(Jwts.class)) {
        try (MockedStatic<CryptoValues> cryptoValuesMock = mockStatic(CryptoValues.class)) {
          // Configurar CryptoValues.getJwtKey()
          cryptoValuesMock.when(CryptoValues::getJwtKey).thenReturn("test-secret-key");

          // Configurar Jwts para la API anterior
          io.jsonwebtoken.JwtParser jwtParserMock = mock(io.jsonwebtoken.JwtParser.class);
          io.jsonwebtoken.Jws<Claims> jwsMock = mock(io.jsonwebtoken.Jws.class);
          Claims claimsMock = mock(Claims.class);

          jwtsMock.when(Jwts::parser).thenReturn(jwtParserMock);
          when(jwtParserMock.setSigningKey(any(byte[].class))).thenReturn(jwtParserMock);
          when(jwtParserMock.parseClaimsJws(anyString())).thenReturn(jwsMock);
          when(jwsMock.getBody()).thenReturn(claimsMock);

          // Configurar claims
          when(claimsMock.get("authorities")).thenReturn(authorities);
          when(claimsMock.getSubject()).thenReturn("testUser");

          // Act
          jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

          // Assert
          verify(filterChain).doFilter(request, response);
          verify(securityContext).setAuthentication(any());
        }
      }
    }
  }
}
