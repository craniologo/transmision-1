package pe.gob.onpe.wsonpe.enums;

import lombok.Getter;

@Getter
public enum AccionesVersionEnum {

  OBTENER_VERSION("vV", "Obtener version");

  private String code;
  private String description;

  AccionesVersionEnum(String code, String description) {
    this.code = code;
    this.description = description;
  }
}
