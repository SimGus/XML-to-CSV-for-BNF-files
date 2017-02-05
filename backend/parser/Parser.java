package backend.parser;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Stack;

import util.Log;
import backend.files.FileOpener;
import backend.files.Reader;

public class Parser {
   private static final String firstXmlTagName = "?xml", preferredXMLVersion = "\"1.0\"";

   private static String inputFileName = "undefined", outputFileName = "undefined";
   private static ArrayList<XMLTag> rootTags = new ArrayList<XMLTag>();
   private static Stack<XMLTag> stackOfTags = new Stack<XMLTag>();

   public static void parse(String inputFileName, String outputFileName) {
      Parser.inputFileName = inputFileName;
      Parser.outputFileName = outputFileName;

      Scanner inputScanner;
      try {
         inputScanner = new Scanner(new File(inputFileName));
      } catch (FileNotFoundException e) {
         Log.err("Couldn't open input file : '"+Parser.inputFileName+"'");
         return;
      }

      String currentLine = Reader.getNextEffectiveLine(inputScanner);
      if (currentLine == null) {
         Log.warn("File '"+Parser.inputFileName+"' is empty (no effective line).");
         return;
      }
      //============= Check the validity of the file (xml prolog) ==================
      if (!prologTagIsValid(currentLine))
         return;

      //================= Parse all following tags =====================
      int i=0;
      boolean standaloneTag, closingTag;
      while (currentLine != null) {
         currentLine = Reader.getNextEffectiveLine(inputScanner);
         if (currentLine == null)//EOF
            break;
         parseLine(currentLine);
      }
      if (!stackOfTags.isEmpty()) {
         Log.err("Missing closing tag(s) for tags with names :");
         while (!stackOfTags.isEmpty())
            Log.err(stackOfTags.pop().getName());
         Log.warn("The transcription might be wrong.");
      }

      Log.log("Detected tree :");
      for (XMLTag current : rootTags)
         System.out.println(current.contentsToString());

      ArrayList<String> toWrite = new ArrayList<String>();
      for (XMLTag current : rootTags)
         toWrite.add(current.contentsToString());
      FileOpener.writeFile(Parser.outputFileName, toWrite);
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

   /*
    * Checks if the first (effective) line of the file is a valid prolog tag with a valid form and version number
    * Returns true if the prolog tag is valid
    */
   public static boolean prologTagIsValid(String firstLine) {
      if (firstLine.length() <= 0 || firstLine.charAt(0) != '<') {
         Log.err("The file "+inputFileName+" does not contain a valid XML prolog.");
         return false;
      }

      ArrayList<String> parts = splitTag(firstLine, 0);
      if (parts.size() <= 0 || !parts.get(0).equals(firstXmlTagName)) {
         Log.err("The file '"+inputFileName+"' is not a valid XML file. Missing the XML prolog.");
         return false;
      }
      for (int i=1; i<parts.size(); i++) {
         if (parts.get(i).equals("version")) {
            if (!parts.get(i+1).equals(preferredXMLVersion))
               Log.warn("XML version might be out-of-date : version is "+parts.get(i+1)+" while preferred version is "+preferredXMLVersion);
            break;
         }
      }
      //TODO more checking about the encoding?
      return true;
   }

   /*
    * Parses the line @line and puts the tags in @rootTags and @stackOfTags
    */
   public static void parseLine(String line) {
      int i=0;
      boolean standaloneTag = false, closingTag = false;
      while (i < line.length()) {
         //-------------- Add a string element ------------------------
         if (line.charAt(i) != '<') {}
         //--------------- Create a new xml tag ------------------------
         else {
            ArrayList<String> splittedTag = splitTag(line, i);
            //--------- Get index pointer to the next interesting part of the line ----------------
            if (!line.contains(">"))
               i = line.length();
            else
               i = line.indexOf(">")+1;
            //-------------- make tag --------------------
            if (splittedTag.size() <= 0)
               continue;//don't make the new tag

            String tagName = splittedTag.get(0);
            if (tagName.equals("/")) {
               Log.err("Detected an XML tag with name '/'. Ignoring the tag.");
               continue;
            }
            if (tagName.startsWith("/")) {//check if tag is a closing tag
               tagName = tagName.substring(1);
               closingTag = true;
               if (splittedTag.size() > 1)
                  Log.warn("Detected attributes in the closing tag with name '"+tagName+"'");
            }
            if (splittedTag.size() == 1 && isStandaloneWord(tagName)) {
               tagName = tagName.substring(0, tagName.length()-1);//remove last character ('/')
               standaloneTag = true;
            }

            XMLTag newTag = new XMLTag(tagName);

            int j;
            for (j=1; j+1<splittedTag.size(); j+=2) {
               if (j+1>=splittedTag.size()-2 && isStandaloneWord(splittedTag.get(j+1))) {//if it is the last attribute value
                  splittedTag.set(j+1, splittedTag.get(j+1).substring(0, splittedTag.get(j+1).length()-1));//remove last character ('/')
                  standaloneTag = true;
               }
               newTag.putAttribute(splittedTag.get(j), splittedTag.get(j+1));
            }
            if (j<splittedTag.size() && splittedTag.get(j).equals("/")) {//standalone tag
               standaloneTag = true;
            }

            if (!closingTag) {
               //---------- Put tag in the roots if the stack is empty --------------
               if (stackOfTags.isEmpty()) {
                  rootTags.add(newTag);
               }
               //----------- Put tag in the children elements of the tag on top of the stack ---------
               else {
                  XMLTag top = stackOfTags.peek();
                  top.addChildElement(newTag);
               }
               //----------- Put tag onto the stack (if not standalone) -------------------
               if (!standaloneTag) {
                  stackOfTags.push(newTag);
               }
            }
            else {
               //---------- Remove the tag that is on top of the stack (+check) -------------------
               if (stackOfTags.isEmpty()) {
                  Log.err("Missing an opening tag for tag named '"+tagName+"'. Ignoring the closing tag.");
                  continue;
               }
               if (!stackOfTags.peek().getName().equals(tagName)) {
                  Log.err("The closing tag with name "+tagName+" does not close the last opened XML tag. Ignoring the closing tag.");
                  continue;
               }
               //Remove top tag
               else {
                  stackOfTags.pop();
               }
            }
         }
      }
   }

   /*
    * Returns true if the word ends in '/' and false otherwise
    * Meant to be called for the last word inside a tag to see if it is a standalone tag or not
    */
   public static boolean isStandaloneWord(String word) {
      if (word == null || word.length() <= 0) {
         Log.warn("Tried to check if an empty word ended with '/'");
         return false;
      }

      return (word.charAt(word.length()-1) == '/');
   }
}
