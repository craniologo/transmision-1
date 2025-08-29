package pe.gob.onpe.wsonpe.projections;

import lombok.Value;

@Value
public class FindConfigurationProjection {

  private String cDescripcion;
  private String cEncSea;
  private String cEncFile;
  private String cEncVep;
  private String cEncFirma;
  private String cEncQr;
  private String cMasterKey;
  private Integer nTxMesa;
  private Integer nEstado;
  private String chsqlUsr;
  private String chsqlPwd;

}
