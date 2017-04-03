package backend.transcripter;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.util.Arrays;

import util.Log;
import gui.Window;
import static util.LogType.*;
import util.LogType;
import backend.files.FileNamesInterpreter;
import backend.files.FileOpener;
import backend.parser.Parser;

public class Translator extends Thread {
   protected final static String threadName = "Translator #";
   protected static int ID = 0;
   protected Window win;

   protected String inputGiven = null, outputGiven = null;
   protected boolean singleFileOutput;
   protected SplitBehavior splitFragments;

   protected int nbErrors = 0;

   public Translator(Window window, String inputFilePath, String outputFilePath, boolean singleFileOutput, SplitBehavior splitFragments) {
      super(threadName+ID);
      String name = threadName+ID;//getName() isn't created until the constructor is fully executed
      ID++;
      win = window;
      inputGiven = inputFilePath;
      outputGiven = outputFilePath;
      this.singleFileOutput = singleFileOutput;
      this.splitFragments = splitFragments;

      Log.fct(2, "<"+getName()+"> Translator.constructor");
      start();
   }

   @Override
   public void run() {
      try {
         runTranslation(inputGiven, outputGiven);
      } catch (IllegalArgumentException e) {
         win.addLog("There was an error while translating the file or directory : 'Illegal argument exception - "+e.getMessage()+"'.",
            "Une erreur s'est produite lors de la traduction du fichier ou dossier : 'Illegal argument exception - "+e.getMessage()+"'.",
            LogType.ERROR);
         nbErrors++;
      } catch (UnsupportedOperationException e) {
         win.addLog("There was an error while translating the file or directory 'Unsupported operation exception - "+e.getMessage()+"'.",
            "Une erreur s'est produite lors de la traduction du fichier ou dossier : 'Unsuppported operation exception - "+e.getMessage()+"'.",
            LogType.ERROR);
         nbErrors++;
      } catch (Exception e) {
         win.addLog("There was an error while translating the file or directory : '"+e.getMessage()+"'.",
            "Une erreur s'est produite lors de la traduction du fichier ou dossier : '"+e.getMessage()+"'.",
            LogType.ERROR);
         nbErrors++;
      }

      nbErrors = 0;
      FileOpener.resetNbDisplayedErrors();
      Parser.resetNbDisplayedErrors();
      Log.log("Thread "+getName()+" over");
   }

   /*
    * MAIN FUNCTION - Translates the file whose name is given in the EditText
    */
   public void runTranslation(String inputFilePath, String outputFilePath) {
      //========= DEBUG ===========
      /*Log.log("Translation : "+Thread.currentThread().getName());
      for (int i=0; i<20; i++) {
         try {
            Thread.sleep(1000);
         } catch (InterruptedException e) {
            Log.err("Thread interrupted : "+e.getMessage());
         }
         win.addLog("Test "+i, "Test "+i, LogType.NORMAL);
         Log.log("Test "+i);
      }*/

      //========== Translation ===============
      Log.fct(1, "Translator.runTranslation");
      if (inputFilePath != null && !inputFilePath.equals("")) {
         inputFilePath = FileNamesInterpreter.interpretInputFileName(inputFilePath);
         outputFilePath = FileNamesInterpreter.interpretOutputFileName(inputFilePath, outputFilePath);

         //==================== Translate one file ==============================
         if (FileOpener.representsAFile(inputFilePath)) {
            Log.log("Translate one file : '"+inputFilePath+"'");
            try {
               if (!FileOpener.isValidFileName(inputFilePath)) {
                  win.addLog("The path '"+inputFilePath+"' is not a valid file path.",
                     "Le chemin '"+inputFilePath+"' n'est pas un chemin vers un fichier valide.", LogType.ERROR);
                  nbErrors++;
                  return;
               }

               translate(inputFilePath, outputFilePath);

            } catch (IllegalArgumentException e) {
               win.addLog("There was an error while translating the file : 'Illegal argument exception - "+e.getMessage()+"'.",
                  "Une erreur s'est produite lors de la traduction du fichier : 'Illegal argument exception - "+e.getMessage()+"'.",
                  LogType.ERROR);
               nbErrors++;
            } catch (UnsupportedOperationException e) {
               win.addLog("There was an error while translating the file 'Unsupported operation exception - "+e.getMessage()+"'.",
                  "Une erreur s'est produite lors de la traduction du fichier : 'Unsuppported operation exception - "+e.getMessage()+"'.",
                  LogType.ERROR);
               nbErrors++;
            } catch (Exception e) {
               win.addLog("There was an error while translating the file : '"+e.getMessage()+"'.",
                  "Une erreur s'est produite lors de la traduction du fichier : '"+e.getMessage()+"'.",
                  LogType.ERROR);
               nbErrors++;
            }

         }
         //======================= Translate one directory ==============================
         else {//dirTranslationEnabled
            Log.log("Translate whole directory : '"+inputFilePath+"'");
            win.addLog("\n=====================================\n"
               +"Starting the translation of the XML files in the directory '"+inputFilePath+"'.\n",
               "\n=====================================\n"
               +"Lancement de la traduction des fichiers XML se trouvant dans le dossier '"+inputFilePath+"'.\n",
               LogType.NORMAL
            );

            File[] filesInDir = FileOpener.getFilesInDirectory(inputFilePath);
            Arrays.sort(filesInDir);
            if (filesInDir.length <= 0)
               win.addLog("The directory specified is empty.", "Le dossier spécifié est vide.", LogType.WARNING);

            ArrayList<HashMap<String, String>> allFilesFields = new ArrayList<HashMap<String, String>>();//only for single output

            int i=0;
            Log.log("Beginning the translation of all files ("+filesInDir.length+" files)");
            for (File inputFile : filesInDir) {
               i++;
               try {
                  //--------- Check if current file is an XML file --------------
                  String currentInputFilePath = inputFile.getAbsolutePath();
                  if (FileNamesInterpreter.isAnXMLFile(currentInputFilePath)) {
                     Log.log("Translation of file '"+inputFile.getName()+"' ("+i+"/"+filesInDir.length+")");
                     if (!FileOpener.isValidFileName(currentInputFilePath)) {
                        win.addLog("The path '"+currentInputFilePath+"' is not a valid file path. Moving on to the next file in the directory.",
                           "Le chemin '"+currentInputFilePath+"' n'est pas valide. Passage au fichier suivant dans le dossier.",
                           LogType.ERROR);
                        continue;
                     }

                     //------------ Translate current file --------------------
                     if (!singleFileOutput) {
                        String currentOutputFilePath = FileNamesInterpreter.generateOutputFileName(currentInputFilePath, outputFilePath);
                        translate(currentInputFilePath, currentOutputFilePath);//translates and write the current file
                     }
                     else {//single output for all the files in the directory
                        ArrayList<HashMap<String, String>> currentTranslation = translate(currentInputFilePath);
                        if (currentTranslation != null)
                           allFilesFields.addAll(currentTranslation);
                     }
                  }
                  else//not an XML file
                     continue;
               } catch (IllegalArgumentException e) {
                  win.addLog("There was an error while translating the file '"+inputFile.getName()+"' : 'Illegal argument exception - "+e.getMessage()+"'.",
                     "Une erreur s'est produite lors de la traduction du fichier '"+inputFile.getName()+"' : 'Illegal argument exception - "+e.getMessage()+"'.",
                     LogType.ERROR);
                  nbErrors++;
               } catch (UnsupportedOperationException e) {
                  win.addLog("There was an error while translating the file '"+inputFile.getName()+"' 'Unsupported operation exception - "+e.getMessage()+"'.",
                     "Une erreur s'est produite lors de la traduction du fichier '"+inputFile.getName()+"' : 'Unsuppported operation exception - "+e.getMessage()+"'.",
                     LogType.ERROR);
                  nbErrors++;
               } catch (Exception e) {
                  win.addLog("There was an error while translating the file '"+inputFile.getName()+"' : '"+e.getMessage()+"'.",
                     "Une erreur s'est produite lors de la traduction du fichier '"+inputFile.getName()+"' : '"+e.getMessage()+"'.",
                     LogType.ERROR);
                  nbErrors++;
               } finally {
                  Log.log("Translation of '"+inputFile.getName()+"' over");
                  continue;
               }
            }

            //========== Make and write the single file (if directory is translating to only one single file) ===========
            if (singleFileOutput) {
               Log.log("Creating single file output '"+outputFilePath+"'");
               win.addLog("Writing translations in file '"+outputFilePath+"'.", "Lancement de l'écriture des traductions dans le fichier '"+outputFilePath+"'.", LogType.NORMAL);
               ArrayList<String> linesToWrite = Interpreter.generateLines(allFilesFields, win);
               //---------- Writing ---------------
               if (!FileNamesInterpreter.checkExtensionsCoherence(outputFilePath))
                  win.addLog("The name of the output file provided does not have the same extension as what has been set in the options ('."+FileNamesInterpreter.getOutputExtension()+"'). The name provided will be used.",
                     "Le nom du fichier de sortie fourni n'a pas la même extension que ce qui a été réglé dans les options ('."+FileNamesInterpreter.getOutputExtension()+"'). Le nom fourni sera utilisé.",
                     LogType.WARNING);

               FileOpener.writeFile(outputFilePath, linesToWrite, win);
            }

            //========== Finish ==============
            int nbDisplayedErrors = nbErrors + FileOpener.getNbDisplayedErrors() + Parser.getNbDisplayedErrors();
            win.addLog("\nTranslation of the XML files in the directory '"+inputFilePath+"' over with "+nbDisplayedErrors+" errors.\n"
               +"=====================================\n",
               "\nTraduction des fichiers XML se trouvant dans le dossier '"+inputFilePath+"' terminée avec "+nbDisplayedErrors+" erreurs.\n"
               +"=====================================\n",
               LogType.NORMAL
            );
         }
      }
      else {
         win.addLog("No name for the XML file provided.", "Pas de nom pour le fichier XML fourni.", LogType.ERROR);
      }

      //Make most resources available for the garbage collector
      Parser.reset();
      Interpreter.reset();
      win.displayReadyMsg();
   }

   protected void translate(String inputFilePath, String outputFilePath) {
      Log.fct(2, "Window.translate");
      String inputFileName = FileNamesInterpreter.getFileOrDirName(inputFilePath);
      String outputFileName = FileNamesInterpreter.getFileOrDirName(outputFilePath);
      if (!FileOpener.fileExists(inputFilePath)) {
         win.addLog("The specified file named '"+inputFileName+"' does not exist.",
            "Le fichier spécifié '"+inputFileName+"' n'existe pas.", LogType.WARNING);
         return;
      }

      win.addLog("---------- Starting translation of the file '"+inputFileName+"' to the file '"+outputFileName+"'. ---------------",
         "---------- Lancement de la traduction du fichier '"+inputFileName+"' vers le fichier '"+outputFileName+"'. ---------------",
         LogType.NORMAL
      );

      //============ Reset parser and interpreter =============
      Parser.reset();
      Interpreter.reset();

      //--------- Parsing -----------
      boolean somethingToTranslate = Parser.parse(inputFilePath, win);

      if (somethingToTranslate) {
         //----------- Translation -------------
         ArrayList<String> linesToWrite = Interpreter.translateTreeAndMakeLines(win, splitFragments);

         //---------- Writing ---------------
         if (!FileNamesInterpreter.checkExtensionsCoherence(outputFilePath))
            win.addLog("The name of the output file provided does not have the same extension as what has been set in the options ('."+FileNamesInterpreter.getOutputExtension()+"'). The name provided will be used.",
               "Le nom du fichier de sortie fourni n'a pas la même extension que ce qui a été réglé dans les options ('."+FileNamesInterpreter.getOutputExtension()+"'). Le nom fourni sera utilisé.",
               LogType.WARNING);

         FileOpener.writeFile(outputFilePath, linesToWrite, win);

         win.addLog("---------- ... Translation of the file '"+inputFileName+"' to the file '"+outputFileName+"' done. ---------------",
            "---------- ... Traduction du fichier '"+inputFileName+"' vers le fichier '"+outputFileName+"' terminée. --------------",
            LogType.NORMAL
         );
      }
      else {
         win.addLog("----------- ... The file '"+inputFileName+"' was NOT translated. --------------",
            "----------- ... Le fichier '"+inputFileName+"' n'a PAS été traduit --------------",
            LogType.NORMAL
         );
      }
   }

   protected ArrayList<HashMap<String, String>> translate(String inputFilePath) {
      Log.fct(3, "Window.translate(ArrayList)");
      String inputFileName = FileNamesInterpreter.getFileOrDirName(inputFilePath);
      if (!FileOpener.fileExists(inputFilePath)) {
         win.addLog("The specified file name '"+inputFileName+"' does not exist.",
            "Le fichier spécifié '"+inputFileName+"' n'existe pas.", LogType.WARNING);
         return null;
      }

      win.addLog("---------- Starting translation of the file '"+inputFileName+"'. ---------------",
         "---------- Lancement de la traduction du fichier '"+inputFileName+"'. ---------------",
         LogType.NORMAL
      );

      //============ Reset parser and interpreter ===================
      Parser.reset();
      Interpreter.reset();

      //----------- Parsing ----------------
      boolean somethingToTranslate = Parser.parse(inputFilePath, win);

      if (somethingToTranslate) {
         ArrayList<HashMap<String, String>> answer = Interpreter.translateTree(win, splitFragments);

         win.addLog("---------- ... Translation of the file '"+inputFileName+"' done. ---------------",
            "---------- ... Traduction du fichier '"+inputFileName+"' terminée. --------------",
            LogType.NORMAL
         );
         return answer;
      }
      else {
         win.addLog("----------- ... The file '"+inputFileName+"' was NOT translated. --------------",
            "----------- ... Le fichier '"+inputFileName+"' n'a PAS été traduit --------------",
            LogType.NORMAL
         );
         return null;
      }
   }
}
