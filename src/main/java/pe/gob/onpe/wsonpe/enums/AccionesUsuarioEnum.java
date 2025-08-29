package pe.gob.onpe.wsonpe.enums;

import lombok.Getter;

@Getter
public enum AccionesUsuarioEnum {

  LOGIN_USER("Ul", "Login de usuario"),
  UPDATE_ESTADO("Ucs", "Actualiza estado"),
  VERIFICAR_PUESTA_CERO("Uvp", "Verificar puesta cero"),
  VERIFICAR_PUESTA_CERO_DIAGNOSTICO("UvpD", "Verificar puesta cero diagnostico"),
  VERIFICAR_PUESTA_CERO_Y_TRANSMITIDO("UvpTrans", "Verificar puesta cero y transmitido"),
  VERIFICAR_PUESTA_CERO_Y_TRANSMITIDO2("UvpTransD", "Verificar puesta cero y transmitido 2"),
  EJECUTAR_PUESTA_CERO("Upc", "Ejecutar puesta cero"),
  EJECUTAR_PUESTA_CERO_DIAGNOSTICO("UpcD", "Ejecutar puesta cero y diagnostico"),
  VERIFICAR_HORA_INGRESO("vT", "Verificar hora de ingreso escrutinio mayor a las 4 p.m."), //VERIFICAR HORA DE INGRESO ESCRUTINIO MAYOR A LAS 4:00 PM USUARIO
  VSA("Vsa", "Vsa");

  private String code;
  private String description;

  AccionesUsuarioEnum(String code, String description) {
    this.code = code;
    this.description = description;
  }
}
