package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVUtils {


    // Type arrayList string, and it belongs to the class mean no need to create
    // instance ot the class to use
    public static List<String[]> readCSV(String filePath){
        // holds array of strings , in this cast we can use List interface as the reference
//        ArrayList<String []> data =new ArrayList<>();
        List<String[]> data =new ArrayList<>();
        // No need finally to close the file this called try-catch with resources
        try(BufferedReader br =new BufferedReader(new FileReader(filePath))){
            String line;
            br.readLine();
            while ((line=br.readLine())!=null){
                // Skip empty lines
                if (line.trim().isEmpty()) {
//                    System.out.println("Skipping empty line");
                    continue;
                }
//                System.out.println("Raw line "+line);
                String[] fields =line.split(",");
//                System.out.println("Parsed fields: "+String.join("|",fields));
                data.add(fields); // add array string.
            }

        } catch (IOException e) {
            System.out.println("Error reading csv file"+e.getMessage());
        }
//        System.out.println("Total row read: "+data.size());
        return data;
    }
    public static void writeCSV(String filePath,List<String[]>data){
        // checked exception need to check at compile time, cuz file might not exist.
        try(BufferedWriter bw=new BufferedWriter(new FileWriter(filePath,true))){
            for (String[] row : data){
                bw.write(String.join(",",row));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void updateCSV(String filePath,List<String []> updatedData,String Header){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))){
            List<String []> data =readCSV(filePath);
            bw.write(Header);
            bw.newLine();
            for(String[] row:updatedData){
                bw.write(String.join(",",row));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public static void main(String[] args) {
////        CSVUtils csvUtils =new CSVUtils();
////        private static final String REGISTER_INFO ="C:\\Users\\Asus\\IdeaProjects\\libraryProject\\src\\data\\registerInfo.csv";
//        List<String []> data=CSVUtils.readCSV("C:\\Users\\Asus\\IdeaProjects\\libraryProject\\src\\data\\registerInfo.csv");
//        for (String[] row:data){
//            System.out.println(String.join(" | ",row));
//        }
//    }
}
