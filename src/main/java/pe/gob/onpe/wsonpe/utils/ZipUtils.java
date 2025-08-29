package pe.gob.onpe.wsonpe.utils;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;


@Slf4j
public class ZipUtils {

  static final String SEPARATOR = System.getProperty("file.separator");
  static final String USER_DIR = System.getProperty("user.dir");
  static final String PATH_ROOT_ZIP = USER_DIR + SEPARATOR;

  private ZipUtils() {
    throw new UnsupportedOperationException("Utility class should not be instantiated");
  }

  public static String unZipPassword(String nameEncrypt, String input, String output, String key)
  {

    String nameUnzip = "decrypted";
    String nameZip = PATH_ROOT_ZIP + nameZip();
    String directoryUnzip = Paths.get(output, nameUnzip).toString();
    String pathEncrypted = Paths.get(input, nameEncrypt).toString();
    File fileIn;
    File fileOut;

    try {
      
      //Delete zip si existe
      WsOnpeFileUtils.deleteFile(nameZip);

      fileIn = new File(pathEncrypted);
      fileOut = new File(nameZip);

      //Create Directory temp
      WsOnpeFileUtils.validateDir(output, true);

      //Create Directory Out
      WsOnpeFileUtils.validateDir(directoryUnzip, true);

      Crypto.decryptFileSTAE(fileIn, fileOut, key);

      try(ZipFile zipFile = new ZipFile(nameZip)){
        zipFile.extractAll(directoryUnzip);
      }

      //Delete zip temporal creado
      WsOnpeFileUtils.deleteFile(nameZip);

      return directoryUnzip;

    } catch (Exception e) {
      log.error("Error en unZipPassword {}, {}", e.getMessage(), e.getStackTrace());
      WsOnpeFileUtils.deleteFile(nameZip);
    }

    return directoryUnzip;
  }

  public static String nameZip() {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd.HHmmss");
    String d = dateFormat.format(new Date());

    return d + Math.random();
  }


}
