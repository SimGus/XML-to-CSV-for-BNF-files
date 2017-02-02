package backend.parser;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import util.Log;
import backend.files.FileOpener;
import backend.files.Reader;

public class Parser {
   private static final String firstXmlTagName = "?xml", preferredXMLVersion = "\"1.0\"";

   public static void parse(String inputFileName, String outputFileName) {
      Scanner inputScanner;
      try {
         inputScanner = new Scanner(new File(inputFileName));
      } catch (FileNotFoundException e) {
         Log.err("Couldn't open input file : '"+inputFileName+"'");
         return;
      }

      String currentLine = Reader.getNextEffectiveLine(inputScanner);
      if (currentLine == null) {
         Log.warn("File '"+inputFileName+"' is empty (no effective line).");
         return;
      }
      //============= Check the validity of the file ==================
      ArrayList<String> firstTagParts = splitTag(currentLine, 0);
      if (firstTagParts.size() <= 0 || !firstTagParts.get(0).equals(firstXmlTagName)) {
         Log.err("The file '"+inputFileName+"' is not a valid XML file. Missing the first XML tag.");
         return;
      }
      for (int i=1; i<firstTagParts.size(); i++) {
         if (firstTagParts.get(i).equals("version")) {
            if (!firstTagParts.get(i+1).equals(preferredXMLVersion))
               Log.warn("XML version might be out-of-date : version is "+firstTagParts.get(i+1)+" while preferred version is "+preferredXMLVersion);
            break;
         }
      }
      //TODO more checking about the encoding?

      //================= Parse all following tags =====================
      while (currentLine != null) {
         Reader.getNextEffectiveLine(inputScanner);

      }
   }

   /*
    * Returns the name, attributes and values of attributes of the tag that begins ('<') at index @tagBeginningIndex in @line
    * Returns an ArrayList with the tag name in place #0 and a pair attribute-value in all 2 next places
    * If there is nothing in the tag, returns an empty ArrayList
    */
   public static ArrayList<String> splitTag(String line, int tagBeginningIndex) {
      if (line == null || line.length() <= 0 || tagBeginningIndex < 0 || tagBeginningIndex >= line.length())
         throw new IllegalArgumentException("Tried to get a tag name from an invalid line.");
      if (line.charAt(tagBeginningIndex) != '<')
         throw new IllegalArgumentException("Tried to get a tag name in a place there is no tag.");
      if (tagBeginningIndex+1 == line.length()) {
         Log.warn("Missing closing tag character (>).");
         return new ArrayList<String>();
      }

      ArrayList<String> answer = new ArrayList<String>();
      int i = tagBeginningIndex+1;
      char c;

      //---------- Find tag name -------------
      String tagName = "";
      while (tagName.equals("")) {
         c = line.charAt(i);
         while (i<line.length()) {
            if (c==' ' || c=='\t' || c=='>')
               break;
            tagName += c;
            i++;
            if (i<line.length())
               c = line.charAt(i);
         }

         if (!tagName.equals("")) {
            tagName = tagName.toLowerCase();
            answer.add(tagName);
         }
         if (i == line.length()) {
            Log.warn("Missing closing tag character (>).");
            return answer;
         }
         if (c == '>')
            return answer;
         if (c == ' ' || c == '\t')//always executed
            i++;
      }

      String currentAttributeName = "", currentAttributeValue = "";
      while (true) {
         //----------- Find attribute -------------
         if (i == line.length())
            return answer;
         c = line.charAt(i);
         while (i<line.length()) {
            if (c=='=' || c==' ' || c=='\t' || c=='>')
               break;
            currentAttributeName += c;
            i++;
            if (i<line.length())
               c = line.charAt(i);
         }

         if (!currentAttributeName.equals("")) {
            currentAttributeName = currentAttributeName.toLowerCase();
            answer.add(currentAttributeName);
         }
         if (c == '>')
            return answer;
         if (currentAttributeName.equals("") && c == '=') {
            Log.err("Couldn't retrieve XML attribute associated with the '"+tagName+"' XML tag.");
            throw new IllegalArgumentException("Invalid syntax");
         }
         if (c == ' ' || c == '\t')
            i++;

         //----------- Find value --------------
         if (c == '=' && i+1 < line.length()) {
            i++;
            c = line.charAt(i);
            while (i<line.length()) {
               if (c==' ' || c=='\t' || c=='>')
                  break;
               currentAttributeValue += c;
               i++;
               if (i<line.length())
                  c = line.charAt(i);
            }

            //currentAttributeValue = currentAttributeValue.toLowerCase();//We should keep the case
            answer.add(currentAttributeValue);
            if (currentAttributeValue.equals(""))
               Log.warn("Attribute '"+currentAttributeName+"' of tag '"+tagName+"' has no value.");

            if (c == '>')
               return answer;
            if (c == ' ' || c == '\t')
               i++;
         }

         //--------- Reset for next loop ----------
         currentAttributeName = "";
         currentAttributeValue = "";
      }
   }
}
