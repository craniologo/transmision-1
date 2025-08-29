package pe.gob.onpe.wsonpe.dto;

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
public class TramaLogRequest {

    private String mesa;

    // Tipo de Solucion(sea =>1, saje =>2, ve =>3)
    private String tipoSolucion;

    // Modulo(instalacion =>1, sufragio =>2, escrutinio =>3)
    private String modulo;

    //Tipo de Trama( trama Escrutinio=>1 )    
    private String tipoTrama;

    //Data en formato json
    private String data;

    //Flag de transmision(0=no transmitido,1=transmitido)
    private Integer transmitio;

    //Tipo de flujo
    private String idFlujo;

}
