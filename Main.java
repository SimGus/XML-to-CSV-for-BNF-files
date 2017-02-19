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

public class Main {
   public static void main(String[] args) {
      String inputFileName = null, outputFileName = null;

      //====== Create window =======
      Window window = new Window();

      //Initialize the HashMap of how to interpret each tags
      Interpreter.initializeMaps();

      //Parser is called when the ok button is pressed
      //(cf. Window.java)
   }
}
