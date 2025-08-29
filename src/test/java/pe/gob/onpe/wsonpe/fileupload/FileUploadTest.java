package pe.gob.onpe.wsonpe.fileupload;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import pe.gob.onpe.wsonpe.utils.WsOnpeFileUtils;
import pe.gob.onpe.wsonpe.utils.WsOnpeUtils;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

@SpringBootTest
class FileUploadTest {

	@Test
	void readAndCopyFileJava() throws IOException {
		/*
		 * Representa acciones de TX
		 */
		String inputFile = makeTestFile("filetest1");

		Path inputPath = Paths.get(inputFile);
    File fInputFile = new File(inputFile);

		byte[] inputBytes = Files.readAllBytes(inputPath);
		String inputHash = WsOnpeUtils.getFileHash(inputPath.toString());
		System.out.println("inputHash: " + inputHash);

		/*
		 * Representa acciones de wsOnpe
		 */
		String pathName = makeTestFile("filetest2");
    File fOutputFile = new File(pathName);

		File outputFile = new File(pathName);

		OutputStream outputStream = new FileOutputStream(outputFile);
		outputStream.write(inputBytes);

		Path outputPath = Paths.get(pathName);
		String outputHash = WsOnpeUtils.getFileHash(outputPath.toString());

		System.out.println("outputHash: " + outputHash);

    WsOnpeFileUtils.deleteFile(fInputFile);
    WsOnpeFileUtils.deleteFile(fOutputFile);

		assertEquals(inputHash, outputHash);
	}

	@Test
	void readAndCopyFileCommonsIO() throws IOException {
		/*
		 * Representa acciones de TX
		 */
		String inputFile = makeTestFile("test1");
    File fInputFile = new File(inputFile);

		Path inputPath = Paths.get(inputFile);

		String inputHash = WsOnpeUtils.getFileHash(inputPath.toString());

		System.out.println("inputHash: " + inputHash);

		/*
		 * Representa acciones de wsOnpe
		 */

		InputStream inputStream;

		inputStream = FileUtils.openInputStream(new File(inputFile));

		String pathName = makeTestFile("testfile2");
    File fOutputFile = new File(inputFile);
    System.out.println("pathname: " + pathName);

		File outputFile = new File(pathName);

		FileUtils.copyInputStreamToFile(inputStream, outputFile);

		Path outputPath = Paths.get(pathName);

		String outputHash = WsOnpeUtils.getFileHash(outputPath.toString());

		System.out.println("outputHash: " + outputHash);

    WsOnpeFileUtils.deleteFile(fInputFile);
    WsOnpeFileUtils.deleteFile(fOutputFile);

		assertEquals(inputHash, outputHash);
	}

  private String makeTestFile(String fileName) throws IOException {
    String m = System.getProperty("user.dir");
    String testDir = Paths.get(m, "testFiles").toString();
    File dir = new File(testDir);

    if (!dir.exists()){
      dir.mkdir();
    }

    Path filePath = Paths.get(testDir, fileName);
    File f =  new File(filePath.toString());

    if (f.exists()) return f.getAbsolutePath();

    Files.createFile(filePath);

    try (FileWriter fw = new FileWriter(filePath.toString())) {
      fw.write("Test content in my file in " + fileName);
    }

    return filePath.toString();

  }

}
