package backend.parser;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import util.Log;
import backend.files.FileOpener;

public class Parser {
   public static void parse(String inputFileName, String outputFileName) {
      Scanner inputScanner;
      try {
         inputScanner = new Scanner(new File(inputFileName));
      } catch (FileNotFoundException e) {
         Log.err("Couldn't open input file : '"+inputFileName+"'");
         return;
      }
      String currentLine;
      ArrayList<String> linesToWrite = new ArrayList<String>();
      while (inputScanner.hasNextLine()) {
         //============== Check first line to be XML version ===============
         //String firstLine =
         currentLine = Reader.getNextEffectiveLine(inputScanner);
         Log.log("'"+currentLine+"'");
         linesToWrite.add(currentLine);
      }
      FileOpener.writeFile(outputFileName, linesToWrite);
   }

   private static void makeTagsTree(Scanner inputScanner) {

   }
}
