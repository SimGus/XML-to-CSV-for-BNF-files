package backend.files;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.io.File;
import java.util.ArrayList;
import java.io.IOException;

import util.Log;

public class FileOpener {
   public static String currentDirectory = Paths.get(".").toAbsolutePath().getParent().toString();

   public static boolean fileExists(String fileName) {
      String filePath = currentDirectory+"/"+fileName;
      File file = new File(filePath);
      return (file.exists() && !file.isDirectory());
   }

   public static void writeFile(String fileName, ArrayList<String> lines) {
      try {
         String filePath = currentDirectory+"/"+fileName;
         //Path file = Paths.get(filePath);
         Files.write(Paths.get(filePath), lines, Charset.forName("UTF-8"));
      } catch (IOException e) {
         Log.err("Error while writing output file");
      }
   }
}
