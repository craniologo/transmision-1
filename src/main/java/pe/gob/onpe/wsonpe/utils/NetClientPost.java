package pe.gob.onpe.wsonpe.utils;

import lombok.extern.slf4j.Slf4j;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.dto.SynchroRequest;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

@Slf4j
public class NetClientPost implements Serializable {
  private transient URLConnection urlConnection;
  private transient HttpURLConnection httpURLConnection;
  private transient HttpsURLConnection httpsURLConnection;
  private String params;
  private String urlString;

  public NetClientPost(String urlString) {


    try {

      URL url = new URL(urlString);

      this.urlString = urlString;
      this.urlConnection = url.openConnection();

      this.urlConnection.setConnectTimeout(30000);
      this.urlConnection.setDoInput(true);
      this.urlConnection.setDoOutput(true);
      this.urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
      this.urlConnection.setRequestProperty("User-Agent", "wsOnpe Agent");

      this.urlConnection.setUseCaches(false);
    } catch (IOException e) {
      log.error("{}: {}", this.getClass().getName(), e.getMessage());
    }
  }

  public void setRequestParams(SynchroRequest params) {
    StringBuilder sb = new StringBuilder();

    sb.append(String.format("masterKey=%s&", params.getMasterKey()));
    sb.append(String.format("mesa=%s&", params.getMesa()));
    sb.append(String.format("tipoSolucion=%s&", params.getTipoSolucion()));
    sb.append(String.format("tipoModulo=%s&", params.getTipoModulo()));
    sb.append(String.format("tipoTrama=%s&", params.getTipoTrama()));
    sb.append(String.format("force=%s&", params.getForce()));

    this.params = sb.toString();
  }

  public String sendRequestWithSSL() {
    String response = "";

    try {
      this.httpsURLConnection = (HttpsURLConnection) this.urlConnection;
      this.httpsURLConnection.connect();

      OutputStream os = this.httpsURLConnection.getOutputStream();
      os.write(this.params.getBytes());
      os.flush();

      BufferedReader br = new BufferedReader(new InputStreamReader(this.httpsURLConnection.getInputStream(), StandardCharsets.UTF_8));
      String output;

      while ((output = br.readLine()) != null) {
        response = output;
      }

      this.httpsURLConnection.disconnect();

      os.close();
      br.close();

    } catch (Exception ex) {
      log.error("Error enviando con SSL: {}", ex.getMessage());
    }

    return response;
  }

  public MensajeWsResponse getResult() {

    MensajeWsResponse response = new MensajeWsResponse();
    String strResponse = "";

    try {
      if (isSSL()) {
        strResponse = this.sendRequestWithSSL();
      } else {
        strResponse = this.sendRequest();
      }

      response = WsOnpeUtils.getJsonToObject(strResponse, MensajeWsResponse.class);

      if (response == null) {
        response = new MensajeWsResponse();
        response.setSuccess(false);
        response.setMessage(strResponse);
      }

    } catch (Exception e) {
      log.error("Error conectando con la sincronizacion: {}", e.getMessage());
      response = new MensajeWsResponse();
      response.setSuccess(false);
      response.setMessage(e.getMessage());
    }

    return response;
  }

  public String sendRequest() {
    StringBuilder sb = new StringBuilder();

    try {
      this.httpURLConnection = (HttpURLConnection) this.urlConnection;
      this.httpURLConnection.connect();

      BufferedReader br;

      try (OutputStream os = this.httpURLConnection.getOutputStream()) {
        os.write(this.params.getBytes());
        os.flush();
        br = new BufferedReader(new InputStreamReader((this.httpURLConnection.getInputStream()), StandardCharsets.UTF_8));
        String s;
        while ((s = br.readLine()) != null) {
          sb.append(s);
        }
        this.httpURLConnection.disconnect();
      }
      br.close();

    } catch (IOException e) {
      log.error("{}: {}: {}", this.getClass().getName(), "getResponse", e.getMessage());
    }

    return sb.toString();
  }

  private boolean isSSL() {
    List<String> urlParts = Arrays.asList(this.urlString.split(":"));
    return !urlParts.isEmpty() && urlParts.get(0).equalsIgnoreCase("https");
  }
}
