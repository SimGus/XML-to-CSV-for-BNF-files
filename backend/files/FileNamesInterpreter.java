package backend.files;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import util.Log;
import backend.files.FileOpener;

public class FileNamesInterpreter {
   private static final String XMLExtension = ".xml";
   private static final String[] extensionsAvailable = {".txt", ".tab", ".tsv"};
   private static int extensionChosenID = 0;

   public static String interpretInputFileName(String userInput) {
      if (userInput == null || userInput.length() <= 0)
         throw new IllegalArgumentException("No input file name given.");
      if (!FileOpener.representsAFile(userInput))
         return userInput;
      if (userInput.length() <= 4
         || !userInput.substring(userInput.length()-4).equals(XMLExtension))
         userInput += XMLExtension;

      return userInput;
   }

   public static String interpretOutputFileName(String inputFileName, String userOutputInput) {
      if (userOutputInput == null || userOutputInput.length() <= 0)
         return generateOutputFileName(inputFileName);

      if (!FileOpener.representsAFile(userOutputInput))
         return userOutputInput;

      if (userOutputInput.length() <= 4
         || (!userOutputInput.substring(userOutputInput.length()-4).equals(extensionsAvailable[0])
            && !userOutputInput.substring(userOutputInput.length()-4).equals(extensionsAvailable[1])
            && !userOutputInput.substring(userOutputInput.length()-4).equals(extensionsAvailable[2])))
         userOutputInput += extensionsAvailable[extensionChosenID];

      return userOutputInput;
   }

   public static String generateOutputFileName(String inputFilePath) {
      if (inputFilePath == null || inputFilePath.length() <= 0)
         throw new IllegalArgumentException("Invalid input file path.");

      //------ Check if file or directory ------------
      File tmp = new File(inputFilePath);
      if (tmp.isDirectory())
         return inputFilePath;

      if (inputFilePath.length() <= 4)
         throw new IllegalArgumentException("Invalid input file path (too short).");

      String answer = inputFilePath.substring(0, inputFilePath.length()-4);
      return answer+extensionsAvailable[extensionChosenID];
   }

   public static String generateOutputFileName(String inputFilePath, String outputDirectoryPath) {
      if (inputFilePath == null || inputFilePath.length() <= 0)
         throw new IllegalArgumentException("Invalid input file path.");

      File tmp = new File(inputFilePath);
      if (tmp.isDirectory())
         throw new IllegalArgumentException("Tried to generate an output file name for an input that is not a file.");

      String inputFileName = getFileName(inputFilePath);
      String outputFilePath = outputDirectoryPath+"/"+inputFileName;
      return generateOutputFileName(outputFilePath);
   }

   public static void changeOutputExtension(String extension) {
      switch (extension.toLowerCase()) {
         case "txt":
         case ".txt":
            extensionChosenID = 0;
            break;
         case "tab":
         case ".tab":
            extensionChosenID = 1;
            break;
         case "tsv":
         case ".tsv":
            extensionChosenID = 2;
            break;
         default:
            Log.warn("Tried to change the extension to an unavailable extension.");
            break;
      }
   }

   public static String getOutputExtension() {
      switch (extensionChosenID) {
         case 0:
            return "txt";
         case 1:
            return "tab";
         case 2:
            return "tsv";
         default:
            throw new IllegalArgumentException("The extension selected is not available.");
      }
   }

   public static boolean checkExtensionsCoherence(String outputFileName) {
      return (outputFileName.substring(outputFileName.length()-4).equals(extensionsAvailable[extensionChosenID]));
   }

   public static String getFileName(String filePath) {
      Path path = Paths.get(filePath);
      return path.getFileName().toString();
   }

   public static boolean isAnXMLFile(String filePath) {
      if (filePath == null || filePath.length() <= 4)
         return false;
      return (filePath.substring(filePath.length()-4).equals(XMLExtension));
   }
}
