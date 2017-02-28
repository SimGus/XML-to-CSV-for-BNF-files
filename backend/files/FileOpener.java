package backend.files;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.io.File;
import java.util.ArrayList;
import java.io.IOException;

import gui.Window;
import static util.LogType.*;

public class FileOpener {
   public static String currentDirectory = Paths.get(".").toAbsolutePath().getParent().toString();

   /* Checks if the path @filePath is valid according to the OS */
   public static boolean isValidFileName(String filePath) {
      File f = new File(filePath);
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

   public static boolean representsAFile(String filePath) {
      File tmp = new File(filePath);
      return tmp.isFile();
   }

   public static File[] getFilesInDirectory(String dirPath) {
      File tmp = new File(dirPath);
      if (!tmp.isDirectory())
         throw new IllegalArgumentException("Tried to get all files of a directory from a path that represents a file.");
      return tmp.listFiles();
   }

   public static void writeFile(String fileName, ArrayList<String> lines, Window window) {
      try {
         String filePath;
         File tmp = new File(fileName);
         if (tmp.isAbsolute())
            filePath = fileName;
         else
            filePath = currentDirectory+"/"+fileName;

         //add BOM at the beginning of the first line
         if (lines.size() <= 0)
            lines.add(Character.toString('\uFEFF'));
         else
            lines.set(0, Character.toString('\uFEFF')+lines.get(0));

         //Path file = Paths.get(filePath);
         Files.write(Paths.get(filePath), lines, Charset.forName("UTF-8"));// If the file exists, truncates it
      } catch (IOException e) {
         window.addLog("Error while writing output file : '"+e.getMessage()+"'.",
            "Erreur lors de l'écriture du fichier de sortie : '"+e.getMessage()+"'.",
            ERROR);
      }
   }
}
