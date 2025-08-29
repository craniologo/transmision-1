package pe.gob.onpe.wsonpe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class WsOnpeApplicationTests {

  @Test
  void prueba() {
    Assertions.assertNull(null);
  }

}
