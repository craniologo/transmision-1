package pe.gob.onpe.wsonpe.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class WsOnpeFileUtils {

  private WsOnpeFileUtils() {
    throw new IllegalStateException("WsOnpeFileUtils class");
  }

  public static boolean deleteWithChildren(String path) {
    File file = new File(path);
    if (!file.exists()) {
      return true;
    }
    if (!file.isDirectory()) {
      return deleteDirectory(file);
    }
    return deleteChildren(file) && deleteDirectory(file);
  }

  private static boolean deleteChildren(File dir) {
    File[] children = dir.listFiles();
    boolean childrenDeleted = true;
    for (int i = 0; children != null && i < children.length; i++) {
      File child = children[i];
      if (child.isDirectory()) {
        childrenDeleted = deleteChildren(child) && childrenDeleted;
      }
      if (child.exists()) {
        childrenDeleted = deleteDirectory(child) && childrenDeleted;
      }
    }
    return childrenDeleted;
  }

  public static void deleteFile(String nameFile) {
    File file = new File(nameFile);
    deleteFile(file);
  }

  public static void deleteFile(File file) {
    try {
      if (file.exists() && file.isFile()) {
        Files.delete(file.toPath());
        log.info("File {} borrado exitosamente", file.getAbsolutePath());
      } else {
        log.warn("File {} no pudo ser borrado", file.getAbsolutePath());
      }
    } catch (Exception e) {
      log.error("Error al eliminar archivo: {}", e.getMessage());
    }
  }

  public static boolean validateDir(String path, boolean action) {
    File file = new File(path);
    boolean isDirectory = file.isDirectory();

    if (action && !file.exists()) {
      file.mkdirs();
      return true;
    }
    return isDirectory;
  }

  public static boolean validateFile(String path) {
    File file = new File(path);
    return file.exists();
  }

  public static boolean createFolderTree(String pathname) {
    File folder = new File(pathname);

    if (folder.exists()) return true;

    File fileAux = new File(Paths.get(pathname, "aux.txt").toString());
    File parent = fileAux.getParentFile();

    return parent == null || parent.exists() || parent.mkdirs();
  }

  public static boolean deleteDirectory(File file) {
    if (file.isDirectory()) {
      try {
        Files.delete(file.toPath());
        return true;
      } catch (IOException e) {
        return false;
      }
    }
    return false;
  }
}
