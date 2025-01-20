package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class FileUtils {
    public static String generateUniqueFileName(String originalFilName){
        String extension="";
        int dotIndex= originalFilName.lastIndexOf('.');
        if (dotIndex>0){
            extension = originalFilName.substring(dotIndex);
        }
        SimpleDateFormat dateFormat =new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp =dateFormat.format(new Date());
        Random random =new Random();
        int randomNumber =random.nextInt(1000);
        return "user_"+timeStamp+"_"+randomNumber+extension;
    }
    public static String saveImage(File sourceFile){
        String uniqueFileName=generateUniqueFileName(sourceFile.getName());
        File destinationFile = new File("src/images/profiles/"+uniqueFileName);
        try {
            Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return uniqueFileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
