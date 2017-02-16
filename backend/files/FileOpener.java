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

   public static boolean isValidFileName(String fileName) {
      File f = new File(fileName);
      try {
         f.getCanonicalPath();
         return true;
      } catch (IOException e) {
         return false;
      }
   }

   public static boolean fileExists(String fileName) {
      String filePath;
      File tmp = new File(fileName);
      if (tmp.isAbsolute())
         filePath = fileName;
      else
         filePath = currentDirectory+"/"+fileName;
      File file = new File(filePath);
      return (file.exists() && !file.isDirectory());
   }

   public static void writeFile(String fileName, ArrayList<String> lines) {
      try {
         String filePath;
         File tmp = new File(fileName);
         if (tmp.isAbsolute())
         filePath = fileName;
         else
            filePath = currentDirectory+"/"+fileName;
         //Path file = Paths.get(filePath);
         Files.write(Paths.get(filePath), lines, Charset.forName("UTF-8"));
      } catch (IOException e) {
         Log.err("Error while writing output file : '"+e.getMessage()+"'.");
      }
   }
}
