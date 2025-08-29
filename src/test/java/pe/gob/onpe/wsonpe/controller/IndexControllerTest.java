package pe.gob.onpe.wsonpe.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import pe.gob.onpe.wsonpe.service.IndexService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

class IndexControllerTest {

  @Mock
  private IndexService indexService;

  @Mock
  private Model model;

  @InjectMocks
  private IndexController indexController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void indexShouldCallServiceAndReturnResult() {
    // Given
    String expectedViewName = "index";
    when(indexService.index(any(Model.class))).thenReturn(expectedViewName);

    // When
    String result = indexController.index(model);

    // Then
    assertEquals(expectedViewName, result);
    verify(indexService, times(1)).index(model);
  }
}
