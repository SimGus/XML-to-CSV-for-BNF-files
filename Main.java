import java.util.Arrays;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import util.Log;
import util.EnFrString;

import backend.files.FileNamesInterpreter;
import backend.files.FileOpener;
import backend.parser.Parser;
import backend.transcripter.Lister;
import backend.transcripter.Interpreter;

import gui.Window;
import static util.LogType.*;

public class Main {
   public static void main(String[] args) {
      Log.fct(0, "main");
      //Initialize the HashMap of how to interpret each tags
      Interpreter.initializeMaps();
      //Parser is called when the ok button is pressed
      //(cf. Window.java)

      //====== Create window =======
      Log.log(Thread.currentThread().getName());
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            Window window = new Window();
         }
      });
   }
}
