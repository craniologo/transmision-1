package pe.gob.onpe.wsonpe.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.view.RedirectView;
import pe.gob.onpe.wsonpe.dto.FileRequest;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.service.IFileService;
import pe.gob.onpe.wsonpe.utils.WsOnpeUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")


class FileControllerTest {

  @Mock
  private IFileService fileService;

  @InjectMocks
  private FileController fileController;

  private final String correctMasterKey = "testMasterKey";
  private final String requestUser = "testUser";

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    ReflectionTestUtils.setField(fileController, "masterKey", correctMasterKey);

    // Mock estático para WsOnpeUtils.getRequestUser()
    try (var mockedStatic = mockStatic(WsOnpeUtils.class)) {
      mockedStatic.when(WsOnpeUtils::getRequestUser).thenReturn(requestUser);
    }
  }

  @Test
  void getMethodShouldReturnRedirectToRoot() {
    // When
    RedirectView result = fileController.getMethod();

    // Then
    assertNotNull(result);
    assertEquals("/", result.getUrl());
  }


  @Test
  void storeFileShouldCreateProperFileRequest() {
    String mesa = "M001";
    Integer tipoSolucion = 1;
    Integer tipoModulo = 2;
    Integer tipoTrama = 3;
    Integer npaginaNro = 1;
    MockMultipartFile file = new MockMultipartFile(
      "file", "test.txt", "text/plain", "test content".getBytes());

    MensajeWsResponse expectedResponse = new MensajeWsResponse(0, true, "Archivo guardado correctamente");

    try (var mockedStatic = mockStatic(WsOnpeUtils.class)) {
      mockedStatic.when(WsOnpeUtils::getRequestUser).thenReturn(requestUser);

      ArgumentCaptor<FileRequest> requestCaptor = ArgumentCaptor.forClass(FileRequest.class);
      when(fileService.storeFile(requestCaptor.capture())).thenReturn(expectedResponse);

      // When
      fileController.storeFile(correctMasterKey, mesa, tipoSolucion, tipoModulo, tipoTrama, npaginaNro, file);

      // Then
      FileRequest capturedRequest = requestCaptor.getValue();
      assertEquals(correctMasterKey, capturedRequest.getKey());
      assertEquals(mesa, capturedRequest.getMesa());
      assertEquals(tipoSolucion, capturedRequest.getTipoSolucion());
      assertEquals(tipoModulo, capturedRequest.getTipoModulo());
      assertEquals(tipoTrama, capturedRequest.getTipoTrama());
      assertEquals(npaginaNro, capturedRequest.getNpaginaNro());
      assertEquals(file, capturedRequest.getFile());
    }
  }

}
