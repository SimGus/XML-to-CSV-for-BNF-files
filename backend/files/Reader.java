package backend.files;

import java.util.Scanner;

public class Reader {
   private static final String commentBeginning = "<!--", commentEnding = "-->";
   private static boolean currentLineBeginsInAComment = false, nextLineBeginsInAComment = false;
   private static int currentLineNb = -1;

   /*
    * Returns the  next line that is not empty  or not fully a comment in the file read by @inputScanner
    * If there is no more "effective" line, returns @null
    */
   public static String getNextEffectiveLine(Scanner inputScanner) {
      String answer;
      while (inputScanner.hasNextLine()) {
         currentLineNb++;
         answer = inputScanner.nextLine();
         currentLineBeginsInAComment = nextLineBeginsInAComment;

         //check if the next line will have to be neglected
         if (answer.contains(commentBeginning) && !answer.contains(commentEnding))
            nextLineBeginsInAComment = true;
         else if (answer.contains(commentBeginning) && answer.contains(commentEnding)
            && answer.lastIndexOf(commentBeginning) > answer.lastIndexOf(commentEnding)) {
               nextLineBeginsInAComment = true;
         }
         else if (currentLineBeginsInAComment && !answer.contains(commentEnding))
            nextLineBeginsInAComment = true;
         else
            nextLineBeginsInAComment = false;

         answer = tidyLine(answer);
         if (!answer.equals(""))
            return answer;
      }
      return null;
   }

   //Removes the first position spaces and tabs and removes the comments
   private static String tidyLine(String line) {
      if (line == null || line.length() <= 0)
         return new String("");
      line = removeComments(line);
      line = removeFirstSpaces(line);
      return line;
   }

   private static String removeFirstSpaces(String line) {
      int i;
      for (i=0; i<line.length(); i++) {
         if (line.charAt(i) != ' ' && line.charAt(i) != '\t')
            return line.substring(i);
      }
      if (i == line.length())
         return new String("");
      return line;//unreachable
   }

   private static String removeComments(String line) {
      if (!line.contains(commentBeginning) && !line.contains(commentEnding) && !currentLineBeginsInAComment)
         return line;

      if (currentLineBeginsInAComment) {
         if (line.contains(commentEnding))
            line = line.substring(line.indexOf(commentEnding)+3);
         else
            line = "";
      }

      int commentBeginningIndex, i;
      while (line.contains(commentBeginning)) {
         commentBeginningIndex = line.indexOf(commentBeginning);
         for (i=commentBeginningIndex; i<line.length() && !line.substring(i).startsWith(commentEnding); i++)
         {}
         if (line.substring(i).startsWith(commentEnding))
            i += 3;
         line = line.substring(0, commentBeginningIndex) + line.substring(i);
      }

      return line;
   }

   public static int getCurrentLineNb() {return currentLineNb;}
}
