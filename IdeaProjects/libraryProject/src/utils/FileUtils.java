package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class FileUtils {
    public static String generateUniqueFileName(String originalFileName) {
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = dateFormat.format(new Date());
        Random random = new Random();
        int randomNumber = random.nextInt(1000);
        return "user_" + timeStamp + "_" + randomNumber + extension;
    }

    public static String saveImage(File sourceFile) {
        if (!sourceFile.exists()) {
            System.err.println("Source file does not exist: " + sourceFile.getAbsolutePath());
            return null;
        }

        String uniqueFileName = generateUniqueFileName(sourceFile.getName());
        File destinationFile = new File("IdeaProjects/libraryProject/src/images/profiles/" + uniqueFileName);

        // Ensure the destination directory exists
        File destinationDir = destinationFile.getParentFile();
        if (!destinationDir.exists()) {
            destinationDir.mkdirs(); // Create the directory if it doesn't exist
        }

        try {
            Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return uniqueFileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}