package pe.gob.onpe.wsonpe.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;

@Slf4j
public class FTPService {
  private static FTPClient ftpconnection;


  public static int connectToFTP(String host, String user, String pass){
    final Logger logger = LoggerFactory.getLogger(FTPService.class);
    ftpconnection = new FTPClient();
    ftpconnection.addProtocolCommandListener(new PrintCommandListener(
      new PrintWriter(new LoggingPrintWriter(logger))));
    int reply;
    try {
      ftpconnection.connect(host);
    } catch (IOException e) {
      return 1;
    }

    reply = ftpconnection.getReplyCode();
    if (!FTPReply.isPositiveCompletion(reply)) {
      try {
        ftpconnection.disconnect();
      } catch (IOException e) {
        return 2;
      }
    }
    try {
      ftpconnection.login(user, pass);
    } catch (IOException e) {
      return 3;
    }
    try {
      ftpconnection.setFileType(FTP.BINARY_FILE_TYPE);
    } catch (IOException e) {
      return 4;
    }
    ftpconnection.enterLocalPassiveMode();
    return 0;
  }

  @SuppressWarnings("java:S2629")
  public static int uploadFileToFTP(MultipartFile file, String ftpHostDir, String filename){
    try {
      InputStream input = new FileInputStream((File) file);
      ftpconnection.storeFile(ftpHostDir + filename, input);
    } catch (IOException e) {
      return 5;
    }
    return 0;
  }

  public static int disconnectFTP(){
    if (ftpconnection.isConnected()) {
      try {
        ftpconnection.logout();
        ftpconnection.disconnect();
      } catch (IOException f) {
        return 6;
      }
    }
    return 0;
  }

  public static void setFtpClient(FTPClient client) {
    ftpconnection = client;
  }

  //@param ftpRelativePath Relative path of file to download into FTP server.
  //@param copytoPath Path to copy the file in download process.
  public int downloadFileFromFTP(String ftpRelativePath, String copytoPath){
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(copytoPath);
    } catch (FileNotFoundException e) {
      return 6;
    }
    try {
      ftpconnection.retrieveFile(ftpRelativePath, fos);
    } catch (IOException e) {
      return 7;
    }
    return 0;
  }

  private static class LoggingPrintWriter extends Writer {
    private final Logger logger;
    private StringBuilder buffer = new StringBuilder();

    public LoggingPrintWriter(Logger logger) {
      this.logger = logger;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
      buffer.append(cbuf, off, len);
    }
    @SuppressWarnings("java:S2629")
    @Override
    public void flush() {
      if (buffer.length() > 0) {
        if (logger.isDebugEnabled()) {
          logger.debug(buffer.toString());
        }
        buffer = new StringBuilder();
      }
    }
    @SuppressWarnings("java:S2629")
    @Override
    public void close() {
      flush();
    }
  }

}
