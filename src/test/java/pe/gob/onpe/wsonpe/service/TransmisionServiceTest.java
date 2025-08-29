package pe.gob.onpe.wsonpe.service;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import pe.gob.onpe.wsonpe.constants.WebService;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.projections.FindConfigurationProjection;
import pe.gob.onpe.wsonpe.repository.TabConfTxRepository;
import pe.gob.onpe.wsonpe.repository.TabMesaTransmitidaRepository;
import pe.gob.onpe.wsonpe.repository.TabTareasMesaRepository;
import pe.gob.onpe.wsonpe.utils.WsOnpeFileUtils;
import pe.gob.onpe.wsonpe.utils.WsOnpeUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

class TransmisionServiceTest {

  @Mock
  private TabConfTxRepository confTxRepository;

  @Mock
  private TabMesaTransmitidaRepository mesaTransmitidaRepository;

  @Mock
  private TabTareasMesaRepository tabTareasMesaRepository;

  @TempDir
  Path tempDir;

  private TransmisionService transmisionService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    transmisionService = new TransmisionService(
      confTxRepository,
      mesaTransmitidaRepository,
      tabTareasMesaRepository
    );

    // Inyectar valores para las propiedades
    ReflectionTestUtils.setField(transmisionService, "storageFolder", tempDir.toString());
    ReflectionTestUtils.setField(transmisionService, "electionName", "EG2024");
    ReflectionTestUtils.setField(transmisionService, "certsFolderRoot", tempDir.resolve("certs").toString());
    ReflectionTestUtils.setField(transmisionService, "certificateType", "A");
  }

  @Test
  void testGetEncryptionKey_WithActiveConfigurations() {
    // Arrange
    FindConfigurationProjection mockProjection = mock(FindConfigurationProjection.class);
    when(mockProjection.getCEncFile()).thenReturn("test-encryption-key");

    List<FindConfigurationProjection> mockConfigList = Arrays.asList(mockProjection);
    when(confTxRepository.findBynEstado(WebService.ACTIVE)).thenReturn(mockConfigList);

    // Act
    String result = transmisionService.getEncryptionKey();

    // Assert
    assertEquals("test-encryption-key", result);
    verify(confTxRepository).findBynEstado(WebService.ACTIVE);
  }

  @Test
  void testGetEncryptionKey_WithNoConfigurations() {
    // Arrange
    when(confTxRepository.findBynEstado(WebService.ACTIVE)).thenReturn(List.of());

    // Act
    String result = transmisionService.getEncryptionKey();

    // Assert
    assertEquals("", result);
    verify(confTxRepository).findBynEstado(WebService.ACTIVE);
  }

  @Test
  void testValidateAndConfirmFile_DirectoryCreationFails() {
    // Arrange
    String mesa = "0001";
    int tipoSolucion = 1;
    int tipoModulo = 2;
    int tipoTrama = 3;
    int idFlujo = 4;
    String username = "testuser";
    String fileName = "file.7z";

    try (MockedStatic<WsOnpeUtils> mockedUtils = Mockito.mockStatic(WsOnpeUtils.class)) {
      mockedUtils.when(() -> WsOnpeUtils.mesaDirectoryExistsOrCreate(anyString()))
        .thenReturn(false);

      // Act
      MensajeWsResponse result = transmisionService.validateAndConfirmFile(
        mesa, tipoSolucion, tipoModulo, tipoTrama, idFlujo, username, fileName);

      // Assert
      assertEquals(7, result.getCodigo());
      assertFalse(result.getSuccess());
      assertEquals("Error al crear o acceder el directorio de la mesa", result.getMessage());

      // Verificar que se llamó al método con el path correcto
      String expectedPath = Paths.get(tempDir.toString(), mesa) + WebService.FILE_SEPARATOR;
      mockedUtils.verify(() -> WsOnpeUtils.mesaDirectoryExistsOrCreate(eq(expectedPath)));
    }
  }

  @Test
  void testSetCertificatesFolders_RootFolderCreationFails() {
    // Arrange
    String rootFolder = tempDir.resolve("certs").toString();

    // Inyectar valor y asegurar que los campos están en null
    ReflectionTestUtils.setField(transmisionService, "certsFolderRoot", rootFolder);
    ReflectionTestUtils.setField(transmisionService, "certsFolderMesa", null);
    ReflectionTestUtils.setField(transmisionService, "certsFolderEF", null);

    try (MockedStatic<WsOnpeFileUtils> mockedFileUtils = Mockito.mockStatic(WsOnpeFileUtils.class)) {
      // Configurar createFolderTree para devolver fallo en la carpeta raíz
      mockedFileUtils.when(() -> WsOnpeFileUtils.createFolderTree(rootFolder))
        .thenReturn(false);

      // Act
      transmisionService.setCertificatesFolders();

      // Assert - Verificar que los campos siguen siendo null
      assertNull(ReflectionTestUtils.getField(transmisionService, "certsFolderMesa"),
        "certsFolderMesa debería seguir siendo null");
      assertNull(ReflectionTestUtils.getField(transmisionService, "certsFolderEF"),
        "certsFolderEF debería seguir siendo null");

      // Verificar que solo se llamó al método para la carpeta raíz
      mockedFileUtils.verify(() -> WsOnpeFileUtils.createFolderTree(rootFolder));

      // Verificar que no se llamaron los métodos para las otras carpetas
      mockedFileUtils.verify(() -> WsOnpeFileUtils.createFolderTree(
        argThat(path -> path != null && !path.equals(rootFolder))), Mockito.never());
    }
  }

  @Test
  void testSetCertificatesFolders_MesaFolderCreationFails() {
    // Arrange
    String rootFolder = tempDir.resolve("certs").toString();
    String electionName = "EG2024";
    String certificateType = "A";

    // Inyectar valores
    ReflectionTestUtils.setField(transmisionService, "certsFolderRoot", rootFolder);
    ReflectionTestUtils.setField(transmisionService, "electionName", electionName);
    ReflectionTestUtils.setField(transmisionService, "certificateType", certificateType);

    // Establecer un valor inicial para certsFolderMesa
    String initialMesaFolder = "initial-mesa-folder";
    ReflectionTestUtils.setField(transmisionService, "certsFolderMesa", initialMesaFolder);

    // Calcular paths esperados
    String expectedMesaPath = new File(new File(new File(rootFolder), electionName), certificateType).getPath();
    String expectedEFPath = new File(new File(new File(new File(rootFolder), electionName), certificateType), "EF").getPath();

    try (MockedStatic<WsOnpeFileUtils> mockedFileUtils = Mockito.mockStatic(WsOnpeFileUtils.class)) {
      // Configurar createFolderTree
      mockedFileUtils.when(() -> WsOnpeFileUtils.createFolderTree(rootFolder))
        .thenReturn(true);

      // La creación de la carpeta mesa falla
      mockedFileUtils.when(() -> WsOnpeFileUtils.createFolderTree(
          argThat(path -> path != null && path.equals(expectedMesaPath))))
        .thenReturn(false);

      // La creación de la carpeta EF funciona
      mockedFileUtils.when(() -> WsOnpeFileUtils.createFolderTree(
          argThat(path -> path != null && path.equals(expectedEFPath))))
        .thenReturn(true);

      // Act
      transmisionService.setCertificatesFolders();

      // Assert - Verificar que certsFolderMesa no cambió
      String currentMesaFolder = (String) ReflectionTestUtils.getField(transmisionService, "certsFolderMesa");
      assertEquals(initialMesaFolder, currentMesaFolder,
        "certsFolderMesa no debería cambiar si la creación de la carpeta falla");

      // Verificar que certsFolderEF se actualizó (ya que su creación sí funciona)
      String currentEFFolder = (String) ReflectionTestUtils.getField(transmisionService, "certsFolderEF");
      assertEquals(expectedEFPath, currentEFFolder,
        "certsFolderEF debería actualizarse ya que su creación funciona");

      // Verificar que se intentó crear ambas carpetas
      mockedFileUtils.verify(() -> WsOnpeFileUtils.createFolderTree(expectedMesaPath));
      mockedFileUtils.verify(() -> WsOnpeFileUtils.createFolderTree(expectedEFPath));
    }
  }

  @Test
  void testSetCertificatesFolders_EFolderCreationFails() {
    // Arrange
    String rootFolder = tempDir.resolve("certs").toString();
    String electionName = "EG2024";
    String certificateType = "A";

    // Inyectar valores
    ReflectionTestUtils.setField(transmisionService, "certsFolderRoot", rootFolder);
    ReflectionTestUtils.setField(transmisionService, "electionName", electionName);
    ReflectionTestUtils.setField(transmisionService, "certificateType", certificateType);

    // Establecer un valor inicial para certsFolderEF
    String initialEFFolder = "initial-ef-folder";
    ReflectionTestUtils.setField(transmisionService, "certsFolderEF", initialEFFolder);

    // Calcular paths esperados
    String expectedMesaPath = new File(new File(new File(rootFolder), electionName), certificateType).getPath();
    String expectedEFPath = new File(new File(new File(new File(rootFolder), electionName), certificateType), "EF").getPath();

    try (MockedStatic<WsOnpeFileUtils> mockedFileUtils = Mockito.mockStatic(WsOnpeFileUtils.class)) {
      // Configurar comportamiento por defecto
      mockedFileUtils.when(() -> WsOnpeFileUtils.createFolderTree(anyString()))
        .thenReturn(true);

      // Cualquier llamada con un path que contiene EF falla
      mockedFileUtils.when(() -> WsOnpeFileUtils.createFolderTree(
          argThat(path -> path != null && path.contains("EF"))))
        .thenReturn(false);

      // Act
      transmisionService.setCertificatesFolders();

      // Assert - Verificar que el campo de mesa se actualizó
      String mesaFolder = (String) ReflectionTestUtils.getField(transmisionService, "certsFolderMesa");
      assertNotNull(mesaFolder, "certsFolderMesa should not be null");
      assertEquals(expectedMesaPath, mesaFolder,
        "certsFolderMesa debería actualizarse al path esperado");

      // Verificar que el campo de EF no cambió
      String efFolder = (String) ReflectionTestUtils.getField(transmisionService, "certsFolderEF");
      assertEquals(initialEFFolder, efFolder,
        "certsFolderEF debería mantener su valor original si la creación falla");

      // Verificar que se intentó crear ambas carpetas
      mockedFileUtils.verify(() -> WsOnpeFileUtils.createFolderTree(expectedMesaPath));
      mockedFileUtils.verify(() -> WsOnpeFileUtils.createFolderTree(expectedEFPath));
    }
  }

  @Test
  void testMoveCertificates_EmptySourceDirectory() {
    // Arrange
    String emptySource = "";

    // Spy en el servicio para verificar que processCertificateFile no se llama
    TransmisionService spyService = Mockito.spy(transmisionService);

    // Act
    spyService.moveCertificates(emptySource);

    // Assert - Verificar que processCertificateFile no se llamó
    Mockito.verify(spyService, Mockito.never())
      .processCertificateFile(Mockito.any(File.class), Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void testMoveCertificates_NonExistentSourceDirectory() {
    // Arrange
    String nonExistentSource = tempDir.resolve("non-existent-dir").toString();

    // Spy en el servicio para verificar que processCertificateFile no se llama
    TransmisionService spyService = Mockito.spy(transmisionService);

    // Act
    spyService.moveCertificates(nonExistentSource);

    // Assert - Verificar que processCertificateFile no se llamó
    Mockito.verify(spyService, Mockito.never())
      .processCertificateFile(Mockito.any(File.class), Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void testMoveCertificates_WithFilesToProcess() throws Exception {
    // Arrange
    // Crear un directorio fuente temporal con archivos
    Path sourceDir = tempDir.resolve("source-dir");
    Files.createDirectories(sourceDir);

    // Crear archivos de certificado
    Path mesaCertFile = sourceDir.resolve("mesa.crt");
    Path certFile = sourceDir.resolve("certificado.crt");
    Path nonCertFile = sourceDir.resolve("other.txt");

    Files.write(mesaCertFile, "mesa cert content".getBytes());
    Files.write(certFile, "certificate content".getBytes());
    Files.write(nonCertFile, "other content".getBytes());

    // Inyectar valores
    String rootFolder = tempDir.resolve("certs").toString();
    String electionName = "EG2024";
    String certificateType = "A";

    ReflectionTestUtils.setField(transmisionService, "certsFolderRoot", rootFolder);
    ReflectionTestUtils.setField(transmisionService, "electionName", electionName);
    ReflectionTestUtils.setField(transmisionService, "certificateType", certificateType);

    // Crear directorios de destino
    Path mesaFolder = tempDir.resolve(Paths.get("certs", electionName, certificateType));
    Path efFolder = tempDir.resolve(Paths.get("certs", electionName, certificateType, "EF"));
    Files.createDirectories(mesaFolder);
    Files.createDirectories(efFolder);

    // Spy en el servicio para verificar las llamadas a processCertificateFile y poder acceder a un método privado
    TransmisionService spyService = Mockito.spy(transmisionService);

    // Mockear el método privado processCertificateFile para verificar las llamadas
    Mockito.doNothing().when(spyService).processCertificateFile(
      Mockito.any(File.class), Mockito.anyString(), Mockito.anyString());

    // Act
    spyService.moveCertificates(sourceDir.toString());

    // Assert - Verificar que processCertificateFile se llamó para cada archivo
    Mockito.verify(spyService).processCertificateFile(
      Mockito.eq(mesaCertFile.toFile()),
      Mockito.anyString(),
      Mockito.anyString());

    Mockito.verify(spyService).processCertificateFile(
      Mockito.eq(certFile.toFile()),
      Mockito.anyString(),
      Mockito.anyString());

    Mockito.verify(spyService).processCertificateFile(
      Mockito.eq(nonCertFile.toFile()),
      Mockito.anyString(),
      Mockito.anyString());
  }

  @Test
  void testMoveCertificates_WithSubdirectories() throws Exception {
    // Arrange
    // Crear una estructura de directorios con subdirectorios
    Path sourceDir = tempDir.resolve("source-dir");
    Path subDir = sourceDir.resolve("sub-dir");
    Files.createDirectories(sourceDir);
    Files.createDirectories(subDir);

    // Crear archivos en el directorio principal y subdirectorio
    Path mainFile = sourceDir.resolve("main.crt");
    Path subFile = subDir.resolve("sub.crt");

    Files.write(mainFile, "main cert content".getBytes());
    Files.write(subFile, "sub cert content".getBytes());

    // Inyectar valores
    String rootFolder = tempDir.resolve("certs").toString();
    String electionName = "EG2024";
    String certificateType = "A";

    ReflectionTestUtils.setField(transmisionService, "certsFolderRoot", rootFolder);
    ReflectionTestUtils.setField(transmisionService, "electionName", electionName);
    ReflectionTestUtils.setField(transmisionService, "certificateType", certificateType);

    // Crear directorios de destino
    Path mesaFolder = tempDir.resolve(Paths.get("certs", electionName, certificateType));
    Path efFolder = tempDir.resolve(Paths.get("certs", electionName, certificateType, "EF"));
    Files.createDirectories(mesaFolder);
    Files.createDirectories(efFolder);

    // Spy en el servicio para verificar llamadas recursivas
    TransmisionService spyService = Mockito.spy(transmisionService);

    // Mockear el método processCertificateFile para evitar efectos secundarios
    Mockito.doNothing().when(spyService).processCertificateFile(
      Mockito.any(File.class), Mockito.anyString(), Mockito.anyString());

    // Act
    spyService.moveCertificates(sourceDir.toString());

    // Assert - Verificar que moveCertificates se llamó recursivamente y processCertificateFile para cada archivo
    Mockito.verify(spyService).moveCertificates(subDir.toString());

    Mockito.verify(spyService).processCertificateFile(
      Mockito.eq(mainFile.toFile()),
      Mockito.anyString(),
      Mockito.anyString());
  }

  // Test para el método privado processCertificateFile
  @Test
  void testProcessCertificateFile() throws Exception {
    // Obtener acceso al método privado
    Method processMethod = TransmisionService.class.getDeclaredMethod(
      "processCertificateFile", File.class, String.class, String.class);
    processMethod.setAccessible(true);

    // Crear archivos de certificado de prueba
    Path mesaCertFile = tempDir.resolve("mesa.crt");
    Path efCertFile = tempDir.resolve("certificado.crt");
    Path nonCertFile = tempDir.resolve("other.txt");

    Files.write(mesaCertFile, "mesa cert content".getBytes());
    Files.write(efCertFile, "certificate content".getBytes());
    Files.write(nonCertFile, "other content".getBytes());

    // Crear directorios de destino
    Path mesaFolder = tempDir.resolve("mesa-folder");
    Path efFolder = tempDir.resolve("ef-folder");
    Files.createDirectories(mesaFolder);
    Files.createDirectories(efFolder);

    // Mockear FileUtils para verificar que se llama al método copyFile
    try (MockedStatic<FileUtils> mockedFileUtils = Mockito.mockStatic(FileUtils.class)) {
      // Act - Invocar el método para cada archivo
      ((Method) processMethod).invoke(transmisionService, mesaCertFile.toFile(),
        mesaFolder.toString(), efFolder.toString());

      processMethod.invoke(transmisionService, efCertFile.toFile(),
        mesaFolder.toString(), efFolder.toString());

      processMethod.invoke(transmisionService, nonCertFile.toFile(),
        mesaFolder.toString(), efFolder.toString());

      // Assert - Verificar que FileUtils.copyFile se llamó para los archivos correctos
      mockedFileUtils.verify(() -> FileUtils.copyFile(
        Mockito.eq(mesaCertFile.toFile()),
        Mockito.any(File.class)));

      mockedFileUtils.verify(() -> FileUtils.copyFile(
        Mockito.eq(efCertFile.toFile()),
        Mockito.any(File.class)));

      // No se debería copiar el archivo que no es .crt o .crl
      mockedFileUtils.verify(() -> FileUtils.copyFile(
          Mockito.eq(nonCertFile.toFile()),
          Mockito.any(File.class)),
        Mockito.never());
    }
  }

  // Test para probar una condición de error en processCertificateFile
  @Test
  void testProcessCertificateFile_Exception() throws Exception {
    // Obtener acceso al método privado
    Method processMethod = TransmisionService.class.getDeclaredMethod(
      "processCertificateFile", File.class, String.class, String.class);
    processMethod.setAccessible(true);

    // Crear un archivo de certificado
    Path mesaCertFile = tempDir.resolve("mesa.crt");
    Files.write(mesaCertFile, "mesa cert content".getBytes());

    // Crear un directorio de destino
    Path mesaFolder = tempDir.resolve("mesa-folder");
    Files.createDirectories(mesaFolder);

    // Mockear FileUtils para que lance una excepción
    try (MockedStatic<FileUtils> mockedFileUtils = Mockito.mockStatic(FileUtils.class)) {
      mockedFileUtils.when(() -> FileUtils.copyFile(
          Mockito.any(File.class), Mockito.any(File.class)))
        .thenThrow(new IOException("Test exception"));

      // Act - Invocar el método (no debería lanzar la excepción)
      processMethod.invoke(transmisionService, mesaCertFile.toFile(),
        mesaFolder.toString(), mesaFolder.toString());

      // No hay Assert específico, solo verificamos que no se lanza la excepción
      // y que se ha llamado al método FileUtils.copyFile
      mockedFileUtils.verify(() -> FileUtils.copyFile(
        Mockito.any(File.class), Mockito.any(File.class)));
    }
  }


}
