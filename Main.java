import java.util.Arrays;
import java.util.ArrayList;

import util.Log;
import util.EnFrString;

import backend.files.FileNamesInterpreter;
import backend.files.FileOpener;
import backend.parser.Parser;
import backend.transcripter.Lister;
import backend.transcripter.Interpreter;

import gui.Window;

import static util.LogType.*;
import java.util.concurrent.TimeUnit;

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

      //====== Test =======
      Window window = new Window();

      //Initialize the HashMap of how to interpret each tags
      Interpreter.initializeMaps();

      try {
         TimeUnit.SECONDS.sleep(1);
         window.addLog("Test", "Test", ERROR);
         TimeUnit.SECONDS.sleep(1);
         window.addLog("Warn", "avertissement", WARNING);
         TimeUnit.SECONDS.sleep(1);
         window.addLog("Not important", "Pas important", MINOR);
         TimeUnit.SECONDS.sleep(1);
         window.addLog("Norm", "narmol", NORMAL);

         EnFrString.setCurrentLanguage("French");
         window.setLabels();

         TimeUnit.SECONDS.sleep(1);
         window.addLog("retest", "retest", NORMAL);
      } catch(Exception e) {
         Log.log("exception in main : "+e.getMessage());
      }
   }
}
