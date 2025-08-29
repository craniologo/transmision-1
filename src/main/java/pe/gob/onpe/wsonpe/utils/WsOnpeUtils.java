package pe.gob.onpe.wsonpe.utils;

import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.springframework.security.core.context.SecurityContextHolder;
import pe.gob.onpe.wsonpe.utils.exceptions.AlgorithmException;

@Slf4j
public final class WsOnpeUtils {

	private WsOnpeUtils() {
	}

  public static String getSHA(String input){
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-512");
      byte[] messageDigest = md.digest(input.getBytes());


      BigInteger number = new BigInteger(1, messageDigest);

      // Convert message digest into hex value
      StringBuilder hexString = new StringBuilder(number.toString(16));

      // Pad with leading zeros
      while (hexString.length() < 64)
      {
        hexString.insert(0, '0');
      }

      return hexString.toString();

    } catch (NoSuchAlgorithmException e) {
      throw new AlgorithmException(e);
    }
  }

	public static String getMd5(String input) {
		try {

			// Static getInstance method is called with hashing MD5
			MessageDigest md = MessageDigest.getInstance("MD5");

			// digest() method is called to calculate message digest
			// of an input digest() return array of byte
			byte[] messageDigest = md.digest(input.getBytes());

			// Convert byte array into signum representation
			BigInteger no = new BigInteger(1, messageDigest);

			// Convert message digest into hex value
			String hashtext = no.toString(16);
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		}

		// For specifying wrong message digest algorithms
		catch (NoSuchAlgorithmException e) {
			throw new AlgorithmException(e);
		}
	}

	public static void writeLog(String user, String action, String message) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
		String now = dateFormat.format(new Date());
		String today = dateFormat2.format(new Date());

		try {
			String filename = ".//logs//" + user + "_" + today + ".jel";
			File file = new File(filename);

      if (!file.createNewFile()) {
        throw new IOException("No se pudo crear el archivo " + filename);
      }

			Writer writer = new BufferedWriter(new FileWriter(file, true));
			String contents = "[" + now + "] " + action + ": " + message + "\n";
			writer.write(contents);
			writer.close();
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
	}

	public static int getCurrentTimestampInteger() {
		return (int) ( (new Timestamp(System.currentTimeMillis())).getTime() / 1000.0);
	}

	public static String getFileHash(String filePath) throws IOException {

    String hash = DigestUtils.sha256Hex(Files.readAllBytes(Paths.get(filePath)));

		log.info("WsOnpeUtils: file: {},  getFileHash: {} ", filePath, hash);
		return hash;
	}

  public static String getRequestUser() {
    String encryptedUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
    return Crypto.decryptStringAES(encryptedUser);
  }

  public static boolean mesaDirectoryExistsOrCreate(String pathMesa)
  {
    File results = new File(pathMesa);
    try {
      if (!results.exists()) {
        Files.createDirectory(Paths.get(pathMesa));
      }
    } catch (Exception e ){
      return false;
    }
    return true;
  }

  public static boolean isValidSevenFileZip(File file) throws IOException {
    try{
      SevenZFile sevenZFile = new SevenZFile(file);
      return validateSevenZipEntries(sevenZFile);
    } catch (IOException e) {
      log.info("Error en la  validacion. Error {}, StackTrace: {}", e.getMessage(), (Object) e.getStackTrace());
      return false;
    }
  }

  private static boolean validateSevenZipEntries(SevenZFile sevenZFile) {
    try {
      SevenZArchiveEntry entry = sevenZFile.getNextEntry();
      while (entry != null) {
        byte[] content = new byte[(int) entry.getSize()];
        sevenZFile.read(content, 0, content.length);
        entry = sevenZFile.getNextEntry();
      }
      sevenZFile.close();
      return true;
    } catch (IOException e) {
      log.info("Error en la primera validación {}", e.getMessage());
      return false;
    }
  }

  public static boolean saveFilesFromCompressed(File file, String path) {
    boolean filesSaved = false;

    try(SevenZFile sevenZFile = new SevenZFile(file)){
      SevenZArchiveEntry entry;

      while((entry = sevenZFile.getNextEntry()) != null){

        if (entry.isDirectory()){
          continue;
        }

        Path localPath = Paths.get(path, entry.getName());
        File destFile = new File(localPath.toString());

        File parent = destFile.getParentFile();

        boolean parentExists = true;
        if (!parent.exists()) {
          parentExists = parent.mkdirs();
        }

        if (!parentExists) throw new IOException("No se puede  crear subcarpetas");

        saveEntryToFile(sevenZFile, entry, destFile, path);
      }
      filesSaved = true;

    } catch (Exception e) {
      log.error("Error al guardar los archivos {}", (Object) e.getStackTrace());
    }
    return filesSaved;
  }

  private static void saveEntryToFile(SevenZFile sevenZFile, SevenZArchiveEntry entry,
                                      File destFile, String path) {
    try(FileOutputStream outputStream = new FileOutputStream(destFile)){
      byte[] content = new byte[(int) entry.getSize()];
      sevenZFile.read(content, 0, content.length);
      outputStream.write(content);
      outputStream.flush();

      log.info("Archivo {} fue guardado en {}", entry.getName(), path);
    } catch (Exception e) {
      log.error("Error al procesar los archivos {}", (Object) e.getStackTrace());
    }
  }

  public static <T> T getJsonToObject(String json, Class<T> classOfT) {
    return new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create().fromJson(json, classOfT);
  }
}
