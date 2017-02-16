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

      //=================== Get arguments ====================== TODO remove
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

      //====== Create window =======
      Window window = new Window();

      //Initialize the HashMap of how to interpret each tags
      Interpreter.initializeMaps();

      //Parser is called when the ok button is pressed
      //(cf. Window.java)
   }
}
