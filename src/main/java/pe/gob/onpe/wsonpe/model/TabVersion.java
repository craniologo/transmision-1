package pe.gob.onpe.wsonpe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "TAB_VERSION")
@Data
public class TabVersion {

	@Id
	@Column(name = "N_VERSION_PK")
	private Integer nVersionPk;

	@Column(name = "C_VVERSION")
	private String cVVersion;

	@Column(name = "C_VERSION")
	private String cVersion;

	@Column(name = "C_VVERSION_TRANS")
	private String cVVersionTrans;

	@Column(name = "D_FECHA")
	private Timestamp dFecha;

	@Column(name = "N_SIZE_ZIP")
	private Integer nSizeZip;

	@Column(name = "N_ESTADO")
	private Integer nEstado;

	@Column(name = "C_DESCRIPCION")
	private String cDescripcion;

	@Column(name = "N_TIPO_SOLUCCION")
	private Integer nTipoSolucion;

}
