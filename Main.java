import java.util.Arrays;
import java.util.ArrayList;

import util.Log;
import util.EnFrString;

import backend.files.FileNamesInterpreter;
import backend.files.FileOpener;
import backend.parser.Parser;

import gui.Window;

public class Main {
   public static void main(String[] args) {
      String inputFileName = null, outputFileName = null;

      //=================== Get arguments ======================
      if (args == null || args.length <= 0)
         Log.log("No arguments");
      else {
         Log.log("Arguments : "+Arrays.toString(args));
         if (args.length == 1)
            inputFileName = new String(args[0]);
         else if (args.length == 2) {
            inputFileName = new String(args[0]);
            outputFileName = new String(args[1]);
         }
      }

      boolean validFileNames = false;
      //while (!validFileNames) {
         //============= Run GUI and get file names =======================
         //Window mainWindow = new Window();

         //============== Check file names ======================
         if (inputFileName != null) {
            try {
               inputFileName = FileNamesInterpreter.interpretInputFileName(inputFileName);
               outputFileName = FileNamesInterpreter.interpretOutputFileName(inputFileName, outputFileName);
               Log.log("Input file : "+inputFileName);
               Log.log("Output file : "+outputFileName);

               if (FileOpener.fileExists(inputFileName))
                  Log.log(inputFileName+" exists");
               else
                  Log.log(inputFileName+" doesn't exist");

               validFileNames = true;

               Parser.parse(inputFileName, outputFileName);
            } catch (IllegalArgumentException e) {
               Log.err("Invalid argument : "+e.getMessage());
            }
         }
      //}
   }
}
