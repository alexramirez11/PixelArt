import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileTest {
    
    public static void main(String[] args) throws FileNotFoundException {
        approachOne();
        approachTwo();
    }

    public static void approachOne() {
        System.out.println("------------------------------ Approach One ------------------------------");
        File dir = new File("Saved_Images_Data");

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    System.out.println("File: " + f.getName() + "\n" + "Absolute Path: " + f.getAbsolutePath() + "\n");
                }
            } else {
                System.out.println("SavedImagesData is empty");
            }
            System.out.println("Test PASSED");
        } else {
            System.out.println("Could not get to directory");
            System.out.println("dir.exists: " + dir.exists());
            System.out.println("dir.isDirectory: " + dir.isDirectory());
        }
    }

    public static void approachTwo(){
        System.out.println("------------------------------ Approach Two ------------------------------");
        Scanner scan;
        try {
            scan = new Scanner(new File("Saved_Images_Data"));
            while (scan.hasNextLine()) {
                System.out.println(scan.nextLine());
            }   
            scan.close();
        } catch (FileNotFoundException e) {
            System.out.println("Test FAILED");
        }
        
    }
}
