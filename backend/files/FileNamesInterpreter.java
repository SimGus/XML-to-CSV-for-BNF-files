package backend.files;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import util.Log;
import backend.files.FileOpener;

public class FileNamesInterpreter {
   private static final String XMLExtension = ".xml";
   private static final String[] extensionsAvailable = {".txt", ".tab"};
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

   /*
    * Generates an output file path by replacing the extension of the input file stored in @inputFilePath
    */
   public static String generateOutputFileName(String inputFilePath) {
      if (inputFilePath == null || inputFilePath.length() <= 0)
         throw new IllegalArgumentException("Invalid input file path.");

      //------ Check if file or directory ------------
      File tmp = new File(inputFilePath);
      if (tmp.isDirectory()) {
         String inputFileName = getFileOrDirName(inputFilePath);
         return inputFilePath+"/"+inputFileName+extensionsAvailable[extensionChosenID];
      }
      else {
         if (inputFilePath.length() <= 4)
            throw new IllegalArgumentException("Invalid input file path (too short).");

         String answer = inputFilePath.substring(0, inputFilePath.length()-4);
         return answer+extensionsAvailable[extensionChosenID];
      }
   }

   /*
    * Generates an output file path that will be placed in @outputDirectoryPath and will have the same name
    * as the file stored in @inputFilePath with its extension replaced by an output extension
    */
   public static String generateOutputFileName(String inputFilePath, String outputDirectoryPath) {
      if (inputFilePath == null || inputFilePath.length() <= 0)
         throw new IllegalArgumentException("Invalid input file path.");

      boolean isDirectory = false;
      File tmpInputChecker = new File(inputFilePath);
      if (tmpInputChecker.isDirectory())
         isDirectory = true;

      String inputFileName = getFileOrDirName(inputFilePath);
      String outputFilePath;

      File tmpOutputChecker = new File(outputDirectoryPath);
      if (tmpOutputChecker.isFile())
         outputDirectoryPath = tmpOutputChecker.getParent();

      if (isDirectory)
         outputFilePath = outputDirectoryPath+"/"+inputFileName+XMLExtension;
      else
         outputFilePath = outputDirectoryPath+"/"+inputFileName;
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
         default:
            throw new IllegalArgumentException("The extension selected is not available.");
      }
   }

   public static boolean checkExtensionsCoherence(String outputFileName) {
      return (outputFileName.substring(outputFileName.length()-4).equals(extensionsAvailable[extensionChosenID]));
   }

   public static String getFileOrDirName(String fileOrDirPath) {
      Path path = Paths.get(fileOrDirPath);
      return path.getFileName().toString();
   }

   public static boolean isAnXMLFile(String filePath) {
      if (filePath == null || filePath.length() <= 4)
         return false;
      return (filePath.substring(filePath.length()-4).equals(XMLExtension));
   }
}
