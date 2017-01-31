import java.util.Arrays;

import util.Log;
import util.EnFrString;

import backend.files.FileNamesInterpreter;

import gui.Window;

public class Main {
   public static void main(String[] args) {
      String inputFileName = null, outputFileName = null;

      if (args == null || args.length <= 0)
         Log.log("No arguments");
      else {
         Log.log("Arguments : "+Arrays.toString(args));
         /*if (args.length == 1) {
            if (args[0].equals("English") || args[0].equals("english") || args[0].equals("en") || args[0].equals("En") || args[0].equals("EN"))
               EnFrString.setCurrentLanguage("English");
            else if (args[0].equals("French") || args[0].equals("french") || args[0].equals("fr") || args[0].equals("Fr") || args[0].equals("FR"))
               EnFrString.setCurrentLanguage("French");
         }*/
         if (args.length == 1)
            inputFileName = new String(args[0]);
         else if (args.length == 2) {
            inputFileName = new String(args[0]);
            outputFileName = new String(args[1]);
         }
      }

      //Window mainWindow = new Window();

      if (inputFileName != null) {
         try {
            inputFileName = FileNamesInterpreter.interpretInputFileName(inputFileName);
            outputFileName = FileNamesInterpreter.interpretOutputFileName(inputFileName, outputFileName);
            Log.log("Input file : "+inputFileName);
            Log.log("Output file : "+outputFileName);
         } catch (IllegalArgumentException e) {
            Log.err("Invalid argument : "+e.getMessage());
         }
      }
   }
}
