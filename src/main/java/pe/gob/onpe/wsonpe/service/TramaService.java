package pe.gob.onpe.wsonpe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.gob.onpe.wsonpe.constants.WebService;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.dto.TramaLogRequest;
import pe.gob.onpe.wsonpe.repository.TabTareasMesaRepository;

import java.util.Map;

@Service
@Slf4j
public class TramaService implements ITramaService{

  private static final String PO_RESULTADO = "PO_RESULTADO";
  private static final String PO_MENSAJE = "PO_MENSAJE";

  private TabTareasMesaRepository tramaRepository;

  public TramaService(TabTareasMesaRepository tramaRepository) {
    this.tramaRepository = tramaRepository;
  }

  @Override
  public MensajeWsResponse receiveTrama(String usuario, String trama) {

    if (trama == null || trama.isEmpty()) {
      return new MensajeWsResponse(2, false, "Trama vacía");
    }

    ObjectMapper objectMapper = new ObjectMapper();
    int codigo =  2;
    String descripcion = "";
    String descripcionJson = "";
    String mesa = trama;
    int strama = 0;

    try {
        TramaLogRequest[] array;
        array = objectMapper.readValue(trama, TramaLogRequest[].class);

        mesa = String.valueOf(array[0].getMesa());
        //Tareas de la cantidad de tramas presidenciales
        strama = array.length*2;

        // Verificación de las tareas pendientes de la mesa
        log.info("Accion valida verificacion de las tareas pendientes de {}.", mesa);
        log.info("Llamando stored procedure de verificacion de tareas para la mesa {}", mesa);

        log.info("verificacionTareas: mesa: {}", mesa);

        Map<String, Object> verificaTareasResponse = tramaRepository.spVerificaTareas(mesa, strama);

        codigo = (Integer) verificaTareasResponse.get(PO_RESULTADO);

        // Registro de Trareas Pendientes
        boolean successRegistroTareas = codigo == WebService.REGISTRO_TAREAS;
        // Lista las Trareas Pendientes
        boolean successListaTareas = codigo == WebService.LISTA_TAREAS;
        // Registro duplicado de Trareas Pendientes
        boolean successRegistroDTareas = codigo == WebService.REGISTRO_D_TAREAS;
        // La mesa ya fue transmitida
        //Tramas de la Primera eleccion completadas
        boolean successTramas = codigo == WebService.REGISTRO_TRAMAS;

        descripcion = (String) verificaTareasResponse.get(PO_MENSAJE);

        if (successRegistroTareas || successTramas){
          // Inserción de las tareas pendientes de la mesa
          log.info("Mesa:{}. Accion Insercion de las tareas pendientes.", mesa);
          log.info("Llamando stored procedure.");

          for (int i = 0; i <array.length; i++) {
            Integer tipoSolucion = Integer.valueOf(String.valueOf(array[i].getTipoSolucion()));
            Integer tipoTrama = Integer.valueOf(array[i].getTipoTrama());
            Integer modulo = Integer.valueOf(array[i].getModulo());
            Integer tipoFlujo = Integer.valueOf(array[i].getIdFlujo());

            String logHead = String.format(WebService.LOG_HEAD_FORMAT, mesa, tipoSolucion, modulo, tipoTrama);

            log.info("{}. RegistroTareas: mesa: {}, tipoFlujo: {}", logHead, mesa, tipoFlujo);

            Map<String, Object> registraTareasResponse =
              tramaRepository.spInsercionTareas(mesa,usuario,tipoSolucion,tipoTrama,modulo,tipoFlujo);

            codigo = (Integer) registraTareasResponse.get(PO_RESULTADO);
            boolean success = codigo == WebService.REGISTRO_TAREAS;
            descripcion = (String) verificaTareasResponse.get(PO_MENSAJE);

            if (!success) {
              log.error("{}. Error en el regristro de tareas: {}", logHead, descripcionJson);
              return new MensajeWsResponse(codigo, success, descripcion);
            }
          }

          log.info("Mesa: {}.Registro de las tareas pendientes exitoso", mesa);

        } else if (successListaTareas) {
          // Json de las tareas pendientes de la mesa
          log.info("Accion Json de las tareas pendientes de mesa {}", mesa);
          log.info("Llamando stored procedure.");

          descripcion = "";
          for (int i = 0; i <array.length; i++) {
            Integer tipoSolucion = Integer.valueOf(String.valueOf(array[i].getTipoSolucion()));
            Integer tipoTrama = Integer.valueOf(array[i].getTipoTrama());
            Integer modulo = Integer.valueOf(array[i].getModulo());
            Integer tipoFlujo = Integer.valueOf(array[i].getIdFlujo());

            String logHead = String.format(WebService.LOG_HEAD_FORMAT, mesa, tipoSolucion, modulo, tipoTrama);

            log.info("{}. listaTareas: mesa: {} tipoFlujo: {}", logHead, mesa, tipoFlujo);

            Map<String, Object> listaTareasMetadataResponse =
              tramaRepository.spListaTareas(mesa,tipoSolucion,tipoTrama,modulo,tipoFlujo,"M");

            codigo = (Integer) listaTareasMetadataResponse.get(PO_RESULTADO);
            // Lista del json
            boolean successJson = codigo == WebService.JSON_NOT_NULL;
            // Lista vacía del json
            boolean successJsonNull = codigo == WebService.JSON_NULL;
            descripcionJson = (String) listaTareasMetadataResponse.get(PO_MENSAJE);

            if (successJson ) {
              descripcion = descripcion + descripcionJson + ",";
            } else if (!successJsonNull) {
              log.error("{}. Error al listar las tareas pendientes de metadata", logHead);
              return new MensajeWsResponse(codigo, false, descripcionJson);
            }

            Map<String, Object> listaTareasFileResponse =
              tramaRepository.spListaTareas(mesa,tipoSolucion,tipoTrama,modulo,tipoFlujo,"F");

            codigo = (Integer) listaTareasFileResponse.get(PO_RESULTADO);
            // Lista del json
            successJson = codigo == WebService.JSON_NOT_NULL;
            // Lista vacía del json
            successJsonNull = codigo == WebService.JSON_NULL;
            descripcionJson = (String) listaTareasFileResponse.get(PO_MENSAJE);

            if (successJson) {
              descripcion = descripcion + descripcionJson + ",";
            } else if (!successJsonNull) {
              log.error("{}. Error al listar las tareas pendientes de archivo", descripcionJson);
              return new MensajeWsResponse(codigo, false, descripcionJson);
            }
          }
          codigo = WebService.LISTA_TAREAS;
          if (descripcion.endsWith(",")){
            descripcion = descripcion.substring(0, descripcion.length()- 1);
          }
          descripcion = "[" + descripcion + "]";

          log.info("Mesa:{}. Tareas pendientes: {}", mesa, descripcion);

        } else if (successRegistroDTareas) {
          log.info("Mesa:{}. registro duplicado de las tareas pendientes: {}", mesa, descripcion);
        /*} else if (successMesaTrans) {
          log.info("Mesa:{}. Mesa ya transmitida: {}", mesa, descripcion);*/
        } else {
          log.info("Mesa:{}. error en la verificación de las tareas pendientes: {}", mesa, descripcion);
          return new MensajeWsResponse(codigo, false, descripcion);
        }
    } catch (JsonProcessingException e) {
        log.error("Mesa:{}. Error en receive trama: {} - {}", mesa, e.getMessage(), e.getStackTrace());
    }

    return new MensajeWsResponse(codigo, true, descripcion);
  }
}
