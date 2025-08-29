package pe.gob.onpe.wsonpe.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ActaRequest {

	private String usuario;
	private String key;
	private String mesa;
	private String trama;
	@Setter(AccessLevel.NONE)
	private String trama2;
	private String firma;
	private String meta;
	private Integer tipoSolucion;
	private Integer tipoModulo;
	private Integer tipoTrama;
	private Integer paginaNro;
	private String pdfDigest;

	public void setTrama2(String trama2) {
		this.trama2 = trama2;
		int max = 30000;
		if (this.trama.length() > max) {
			this.trama2 = this.trama.substring(max, this.trama.length());
			this.trama = this.trama.substring(0, max);
		}
	}

}
