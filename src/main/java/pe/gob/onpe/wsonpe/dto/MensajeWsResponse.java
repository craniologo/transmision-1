package pe.gob.onpe.wsonpe.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"success", "message","codigo"})
public class MensajeWsResponse {

  @JsonProperty("Codigo")
  private Integer codigo;

  @JsonProperty("Success")
  private Boolean success;

  @JsonProperty("Message")
  private String message;



}
