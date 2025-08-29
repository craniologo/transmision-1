package pe.gob.onpe.wsonpe.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import pe.gob.onpe.wsonpe.constants.WebService;
import pe.gob.onpe.wsonpe.dto.FileRequest;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.repository.TabMesaTransmitidaRepository;
import pe.gob.onpe.wsonpe.utils.WsOnpeUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

  @Mock
  private TabMesaTransmitidaRepository mesaTransmitidaRepository;

  @Mock
  private ITransmisionService transmisionService;

  @InjectMocks
  private FileService fileService;

  private FileRequest fileRequest;
  private MockMultipartFile mockFile;
  private final String tempDir = System.getProperty("java.io.tmpdir");

  @BeforeEach
  void setUp() {
    // Configurar el directorio de almacenamiento para las pruebas
    ReflectionTestUtils.setField(fileService, "storageFolder", tempDir);

    // Crear un archivo de prueba
    mockFile = new MockMultipartFile(
      "test.txt",
      "test.txt",
      "text/plain",
      "Test content".getBytes()
    );

    // Configurar el objeto request
    fileRequest = new FileRequest();
    fileRequest.setMesa("123");
    fileRequest.setUsuario("testUser");
    fileRequest.setTipoSolucion(1);
    fileRequest.setTipoModulo(2);
    fileRequest.setTipoTrama(3);
    fileRequest.setNpaginaNro(1);
    fileRequest.setFile(mockFile);
  }

  @Test
  void storeFile_whenValidationAlreadyTransmitted_returnsCodeTwo() {
    // Arrange
    Map<String, Object> validationResponse = new HashMap<>();
    validationResponse.put("PO_RESULTADO", 2);
    validationResponse.put("PO_MENSAJE", "Ya se transmitió");

    when(mesaTransmitidaRepository.spValidaTransmisionArchivo(
      anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString()
    )).thenReturn(validationResponse);

    // Act
    MensajeWsResponse result = fileService.storeFile(fileRequest);

    // Assert
    assertEquals(2, result.getCodigo());
    assertEquals("Ya se transmitió", result.getMessage());
    verify(transmisionService).setCertificatesFolders();
    verify(mesaTransmitidaRepository).spValidaTransmisionArchivo("123", 1, 2, 3, 1, "testUser");
    verifyNoMoreInteractions(mesaTransmitidaRepository);
    verifyNoMoreInteractions(transmisionService);
  }

  @Test
  void storeFile_whenValidationFails_returnsError() {
    // Arrange
    Map<String, Object> validationResponse = new HashMap<>();
    validationResponse.put("PO_RESULTADO", 0);
    validationResponse.put("PO_MENSAJE", "Error de validación");

    when(mesaTransmitidaRepository.spValidaTransmisionArchivo(
      anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString()
    )).thenReturn(validationResponse);

    // Act
    MensajeWsResponse result = fileService.storeFile(fileRequest);

    // Assert
    assertEquals(0, result.getCodigo());
    assertEquals("Error de validación", result.getMessage());
    verify(transmisionService).setCertificatesFolders();
    verify(mesaTransmitidaRepository).spValidaTransmisionArchivo("123", 1, 2, 3, 1, "testUser");
    verifyNoMoreInteractions(mesaTransmitidaRepository);
    verifyNoMoreInteractions(transmisionService);
  }

  @Test
  void storeFile_whenFileIsEmpty_returnsEmptyFileError() {
    // Arrange
    Map<String, Object> validationResponse = new HashMap<>();
    validationResponse.put("PO_RESULTADO", 1);
    validationResponse.put("PO_MENSAJE", "Validación correcta");

    when(mesaTransmitidaRepository.spValidaTransmisionArchivo(
      anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString()
    )).thenReturn(validationResponse);

    // Crear un archivo vacío
    MockMultipartFile emptyFile = new MockMultipartFile(
      "empty.txt",
      "empty.txt",
      "text/plain",
      new byte[0]
    );
    fileRequest.setFile(emptyFile);

    // Act
    MensajeWsResponse result = fileService.storeFile(fileRequest);

    // Assert
    assertEquals(6, result.getCodigo());
    assertEquals("Archivo vacio", result.getMessage());
    verify(transmisionService).setCertificatesFolders();
    verify(mesaTransmitidaRepository).spValidaTransmisionArchivo("123", 1, 2, 3, 1, "testUser");
    verify(mesaTransmitidaRepository).spActualizaFileTransmision(
      "123", 1, 2, 3, 1, "", 0, "empty.txt"
    );
    verifyNoMoreInteractions(transmisionService);
  }

  @Test
  void storeFile_whenDirectoryCreationFails_returnsDirectoryError() {
    // Arrange
    Map<String, Object> validationResponse = new HashMap<>();
    validationResponse.put("PO_RESULTADO", 1);
    validationResponse.put("PO_MENSAJE", "Validación correcta");

    when(mesaTransmitidaRepository.spValidaTransmisionArchivo(
      anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString()
    )).thenReturn(validationResponse);

    // Mockear WsOnpeUtils.mesaDirectoryExistsOrCreate para que devuelva false
    try (var mockedStatic = mockStatic(WsOnpeUtils.class)) {
      mockedStatic.when(() -> WsOnpeUtils.mesaDirectoryExistsOrCreate(anyString())).thenReturn(false);

      // Act
      MensajeWsResponse result = fileService.storeFile(fileRequest);

      // Assert
      assertEquals(7, result.getCodigo());
      assertEquals("Error al crear o acceder el directorio de la mesa", result.getMessage());
      verify(transmisionService).setCertificatesFolders();
      verify(mesaTransmitidaRepository).spValidaTransmisionArchivo("123", 1, 2, 3, 1, "testUser");
      verifyNoMoreInteractions(mesaTransmitidaRepository);
      verifyNoMoreInteractions(transmisionService);
    }
  }

  @Test
  void storeFile_whenSuccessful_returnsValidationResult() throws Exception {
    // Arrange
    Map<String, Object> validationResponse = new HashMap<>();
    validationResponse.put("PO_RESULTADO", 1);
    validationResponse.put("PO_MENSAJE", "Validación correcta");

    MensajeWsResponse fileValidationResponse = new MensajeWsResponse(
      WebService.VALIDATE_FILE, true, "Archivo validado correctamente"
    );

    when(mesaTransmitidaRepository.spValidaTransmisionArchivo(
      anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString()
    )).thenReturn(validationResponse);

    // Mockear WsOnpeUtils.mesaDirectoryExistsOrCreate para que devuelva true
    try (var mockedStatic = mockStatic(WsOnpeUtils.class)) {
      mockedStatic.when(() -> WsOnpeUtils.mesaDirectoryExistsOrCreate(anyString())).thenReturn(true);

      // Mockear transmisionService.validateAndConfirmFile
      when(transmisionService.validateAndConfirmFile(
        anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyString()
      )).thenReturn(fileValidationResponse);

      // Mock MultipartFile.transferTo para evitar escritura real en disco
      MockMultipartFile spyFile = spy(mockFile);
      doNothing().when(spyFile).transferTo(any(Path.class));
      fileRequest.setFile(spyFile);

      // Act
      MensajeWsResponse result = fileService.storeFile(fileRequest);

      // Assert
      assertEquals(1, result.getCodigo());
      assertEquals("Transmision satisfactoria", result.getMessage());
      verify(transmisionService).setCertificatesFolders();
      verify(mesaTransmitidaRepository).spValidaTransmisionArchivo("123", 1, 2, 3, 1, "testUser");
      verify(spyFile).transferTo(any(Path.class));
      verify(transmisionService).validateAndConfirmFile(
        "123", 1, 2, 3, 1, "testUser", "test.txt"
      );
    }
  }

  @Test
  void storeFile_whenValidationFails_returnsValidationError() throws Exception {
    // Arrange
    Map<String, Object> validationResponse = new HashMap<>();
    validationResponse.put("PO_RESULTADO", 1);
    validationResponse.put("PO_MENSAJE", "Validación correcta");

    MensajeWsResponse fileValidationResponse = new MensajeWsResponse(
      3, false, "Error en la validación del archivo"
    );

    when(mesaTransmitidaRepository.spValidaTransmisionArchivo(
      anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString()
    )).thenReturn(validationResponse);

    // Mockear WsOnpeUtils.mesaDirectoryExistsOrCreate para que devuelva true
    try (var mockedStatic = mockStatic(WsOnpeUtils.class)) {
      mockedStatic.when(() -> WsOnpeUtils.mesaDirectoryExistsOrCreate(anyString())).thenReturn(true);

      // Mockear transmisionService.validateAndConfirmFile
      when(transmisionService.validateAndConfirmFile(
        anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyString()
      )).thenReturn(fileValidationResponse);

      // Mock MultipartFile.transferTo para evitar escritura real en disco
      MockMultipartFile spyFile = spy(mockFile);
      doNothing().when(spyFile).transferTo(any(Path.class));
      fileRequest.setFile(spyFile);

      // Act
      MensajeWsResponse result = fileService.storeFile(fileRequest);

      // Assert
      assertEquals(3, result.getCodigo());
      assertEquals("Error en la validación del archivo", result.getMessage());
      verify(transmisionService).setCertificatesFolders();
      verify(mesaTransmitidaRepository).spValidaTransmisionArchivo("123", 1, 2, 3, 1, "testUser");
      verify(spyFile).transferTo(any(Path.class));
      verify(transmisionService).validateAndConfirmFile(
        "123", 1, 2, 3, 1, "testUser", "test.txt"
      );
    }
  }

  @Test
  void storeFile_whenExceptionOccurs_returnsErrorResponse() throws Exception {
    // Arrange
    Map<String, Object> validationResponse = new HashMap<>();
    validationResponse.put("PO_RESULTADO", 1);
    validationResponse.put("PO_MENSAJE", "Validación correcta");

    when(mesaTransmitidaRepository.spValidaTransmisionArchivo(
      anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString()
    )).thenReturn(validationResponse);

    // Mockear WsOnpeUtils.mesaDirectoryExistsOrCreate para que devuelva true
    try (var mockedStatic = mockStatic(WsOnpeUtils.class)) {
      mockedStatic.when(() -> WsOnpeUtils.mesaDirectoryExistsOrCreate(anyString())).thenReturn(true);

      // Mockear MultipartFile.transferTo para lanzar una excepción
      MockMultipartFile spyFile = spy(mockFile);
      doThrow(new IOException("Error al escribir el archivo")).when(spyFile).transferTo(any(Path.class));
      fileRequest.setFile(spyFile);

      // Act
      MensajeWsResponse result = fileService.storeFile(fileRequest);

      // Assert
      assertEquals(8, result.getCodigo());
      assertTrue(result.getMessage().contains("Error al subir el archivo al repositorio"));
      verify(transmisionService).setCertificatesFolders();
      verify(mesaTransmitidaRepository).spValidaTransmisionArchivo("123", 1, 2, 3, 1, "testUser");
      verify(spyFile).transferTo(any(Path.class));
      verifyNoMoreInteractions(transmisionService);
    }
  }
}
