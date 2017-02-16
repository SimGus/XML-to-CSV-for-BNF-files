package backend.files;

import util.Log;

public class FileNamesInterpreter {
   private static String[] extensionsAvailable = {"txt", "tab", "tsv"};
   private static int extensionChosenID = 0;

   public static String interpretInputFileName(String userInput) {
      if (userInput == null || userInput.length() <= 0)
         throw new IllegalArgumentException("No input file name given.");
      if (userInput.length() <= 4
         || !userInput.substring(userInput.length()-4).equals(".xml"))
         userInput += ".xml";

      return userInput;
   }

   public static String interpretOutputFileName(String inputFileName, String userOutputInput) {
      if (userOutputInput == null || userOutputInput.length() <= 0)
         return generateOutputFileName(inputFileName);

      if (userOutputInput.length() <= 4
         || (!userOutputInput.substring(userOutputInput.length()-4).equals(".txt")
            && !userOutputInput.substring(userOutputInput.length()-4).equals(".tsv")))
         userOutputInput += ".txt";

      return userOutputInput;
   }

   public static String generateOutputFileName(String inputFileName) {
      if (inputFileName == null || inputFileName.length() <= 4)
         throw new IllegalArgumentException("Invalid input file name.");

      String answer = inputFileName.substring(0, inputFileName.length()-3);
      return answer+extensionsAvailable[extensionChosenID];
   }

   public static void changeExtension(String extension) {
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
}
