package pe.gob.onpe.wsonpe.utils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

class ElectionInformationTest {

  @Test
  void testAllArgsConstructorAndGetters() {
    ElectionInformation info = new ElectionInformation("Presidential", "2025-10-01");

    assertEquals("Presidential", info.getProcessName());
    assertEquals("2025-10-01", info.getProcessDate());
  }

  @Test
  void testNoArgsConstructorAndSetters() {
    ElectionInformation info = new ElectionInformation();

    info.setProcessName("Regional");
    info.setProcessDate("2026-03-01");

    assertEquals("Regional", info.getProcessName());
    assertEquals("2026-03-01", info.getProcessDate());
  }

}
