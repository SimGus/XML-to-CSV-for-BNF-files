package backend.parser;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Stack;

import util.Log;
import backend.files.FileOpener;
import backend.files.Reader;
import gui.Window;
import static util.LogType.*;

public class Parser {
   private static boolean onlyValidSyntax = true;

   private static final String firstXmlTagName = "?xml", preferredXMLVersion = "1.0", doctypeTagName = "!DOCTYPE";

   private static String inputFileName = "undefined";
   public static ArrayList<XMLPart> rootTags = new ArrayList<XMLPart>();
   private static Stack<XMLTag> stackOfTags = new Stack<XMLTag>();

   /*
    * Resets @rootTags, @stackOfTags and @inputFileName
    */
   public static void reset() {
      Log.fct(3, "Parser.reset");
      inputFileName = "undefined";
      rootTags = new ArrayList<XMLPart>();
      stackOfTags = new Stack<XMLTag>();
   }

   /*
    * Parses the file with name @inputFileName and extracts the tree of XML tags
    * Returns true if there is something to translate
    */
   public static boolean parse(String inputFileName, Window window) {
      Log.fct(2, "Parser.parse");
      Parser.inputFileName = inputFileName;

      Scanner inputScanner;
      try {
         inputScanner = new Scanner(new File(inputFileName));
      } catch (FileNotFoundException e) {
         Log.err("Couldn't open input file : '"+Parser.inputFileName+"'");
         window.addLog("Couldn't open the XML file.", "Impossible d'ouvrir le fichier XML.", ERROR);
         return false;
      }

      String currentLine = Reader.getNextEffectiveLine(inputScanner);
      if (currentLine == null) {
         Log.warn("File '"+Parser.inputFileName+"' is empty (no effective line).");
         window.addLog("The XML file is empty.", "Le fichier XML est vide.", WARNING);
         return false;
      }
      //============= Check the validity of the file (xml prolog) ==================
      if (!prologTagIsValid(currentLine, window))
         return false;

      //============ Parse remainder of the first line ======================
      currentLine = removeProlog(currentLine);
      parseLine(currentLine);

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
            Log.err(stackOfTags.pop().getTagName());
         Log.warn("The transcription might be wrong.");

         window.addLog("Certain tags are not properly closed in the XML file. The translation might be invalid.",
            "Certaines balises ne sont pas correctement fermées dans le fichier XML. La traduction pourrait être invalide.",
            WARNING);
      }

      if (!onlyValidSyntax)
         window.addLog("Invalid syntax was detected in the XML file. The translation might be invalid.",
            "Une syntaxe invalide a été détectée dans le fichier XML. La traduction pourrait être invalide.",
            WARNING);

      return true;
   }

   /*
    * Returns the name, attributes and values of attributes of the tag that begins ('<') at index @tagBeginningIndex in @line
    * Returns an ArrayList with the tag name in place #0 and a pair attribute-value in all 2 next places
    * If there is nothing in the tag, returns an empty ArrayList
    */
   private static ArrayList<String> splitTag(String line, int tagBeginningIndex) {
      Log.fct(5, "Parser.splitTag");

      if (line == null || line.length() <= 0 || tagBeginningIndex < 0 || tagBeginningIndex >= line.length())
         throw new IllegalArgumentException("Tried to get a tag name from an invalid line.");
      if (line.charAt(tagBeginningIndex) != '<')
         throw new IllegalArgumentException("Tried to get a tag name in a place there is no tag.");
      if (tagBeginningIndex+1 == line.length()) {
         Log.warn("Missing closing tag character (>).");
         onlyValidSyntax = false;
         return new ArrayList<String>();
      }

      ArrayList<String> answer = new ArrayList<String>();
      int i = tagBeginningIndex+1;
      char c = 0;

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
            //tagName = tagName.toLowerCase();//tags should be case sensitive
            answer.add(tagName);
         }
         if (i == line.length()) {
            Log.warn("Missing closing tag character (>).");
            onlyValidSyntax = false;
            return answer;
         }
         if (c == '>')
            return answer;
         if (c == ' ' || c == '\t')//always executed
            i++;
      }

      //DOCTYPE is not a normal xml tag
      if (tagName.equals(doctypeTagName)) {
         if (i >= line.length())
            return answer;
         int beginningAttributesIndex = i;
         while (c != '>' && ++i < line.length())
            c = line.charAt(i);

         String attribute = line.substring(beginningAttributesIndex, i-1);
         if (attribute.endsWith(">"))
            attribute = attribute.substring(0, attribute.length()-1);
         answer.add(attribute);
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
            //currentAttributeName = currentAttributeName.toLowerCase();//should be case sensitive
            answer.add(currentAttributeName);
         }
         if (c == '>')
            return answer;
         if (currentAttributeName.equals("") && c == '=') {
            Log.err("Couldn't retrieve XML attribute associated with the '"+tagName+"' XML tag.");
            throw new IllegalArgumentException("Invalid syntax in the input file (line "+Reader.getCurrentLineNb()+").");
         }
         while (c == ' ' || c == '\t') {
            i++;
            if (i >= line.length())
               return answer;
            c = line.charAt(i);
         }

         //----------- Find value --------------
         if (c == '=') {
            i++;
            if (i >= line.length())
               return answer;
            c = line.charAt(i);
         }
         while (c == ' ' || c == '\t') {
            i++;
            if (i >= line.length())
               return answer;
            c = line.charAt(i);
         }
         char quotingSymbol;
         if (c == '"')
            quotingSymbol = '"';
         else if (c == '\'')
            quotingSymbol = '\'';
         else {
            Log.err("Attribute '"+currentAttributeName+"' of tag '"+tagName+"' has a value that is not quoted.");
            throw new IllegalArgumentException("Invalid syntax in the input file (line "+Reader.getCurrentLineNb()+"). Missing quotes.");
         }
         if (i+1 < line.length()) {
            i++;
            c = line.charAt(i);
            while (i<line.length()) {
               if (c == quotingSymbol)
                  break;
               currentAttributeValue += c;
               i++;
               if (i<line.length())
                  c = line.charAt(i);
            }

            //currentAttributeValue = currentAttributeValue.toLowerCase();//should be case sensitive
            answer.add(currentAttributeValue);

            if (c == quotingSymbol) {
               i++;
               if (i >= line.length())
                  return answer;
               c = line.charAt(i);
            }
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
   private static boolean prologTagIsValid(String firstLine, Window window) {
      Log.fct(5, "Parser.prologTagIsValid");

      if (firstLine.length() <= 0 || firstLine.charAt(0) != '<') {
         Log.err("The file "+inputFileName+" does not contain a valid XML prolog.");
         window.addLog("The XML file does not contain a valid XML prolog.",
            "Le fichier XML ne contient pas un prologue valide.",
            ERROR);
         return false;
      }

      ArrayList<String> parts = splitTag(firstLine, 0);
      if (parts.size() <= 0 || !parts.get(0).equals(firstXmlTagName)) {
         Log.err("The file '"+inputFileName+"' is not a valid XML file. Missing the XML prolog.");
         window.addLog("The XML file does not contain a valid XML prolog.",
            "Le fichier XML ne contient pas un prologue valide.",
            ERROR);
         return false;
      }
      for (int i=1; i<parts.size(); i++) {
         if (parts.get(i).equals("version")) {
            if (!parts.get(i+1).equals(preferredXMLVersion)) {
               Log.warn("XML version might be out-of-date : version is "+parts.get(i+1)+" while preferred version is "+preferredXMLVersion);
               window.addLog("The XML version ("+parts.get(i+1)+") might be out-of-date. The preferred version is '"+preferredXMLVersion+"'.",
                  "La version de XML ("+parts.get(i+1)+") pourrait être obsolète. La version préférentielle est '"+preferredXMLVersion+"'.",
                  WARNING);
            }
            break;
         }
      }

      //TODO more checking about the encoding?
      return true;
   }

   /*
    * Returns the remainder of @line without the XML prolog tags
    */
   private static String removeProlog(String line) {
      Log.fct(5, "Parser.removeProlog");

      if (line.length() <= 0)
         return "";

      char c = line.charAt(0);
      int i = 0;
      ArrayList<String> parts;
      while (true) {
         if (c == '<') {
            parts = splitTag(line, i);
            if (!parts.get(0).equals(firstXmlTagName) && !parts.get(0).equals(doctypeTagName))
               break;
         }
         if (++i >= line.length())
            return "";
         c = line.charAt(i);
      }

      //get to next tag beginning
      while (++i < line.length() && c != '<')
         c = line.charAt(i);
      if (i >= line.length())
         return "";
      return line.substring(i-1);
   }

   /*
    * Parses the line @line and puts the tags in @rootTags and @stackOfTags
    */
   private static void parseLine(String line) {
      Log.fct(5, "Parser.parseLine");

      int i=0;
      boolean standaloneTag = false, closingTag = false;
      while (i < line.length()) {
         standaloneTag = false;
         closingTag = false;
         //-------------- Add a string element ------------------------
         if (line.charAt(i) != '<') {
            String element = extractStringElement(line, i);
            i += element.length();

            //element = trim(element);//don't trim inside the XMLString object
            //Translate special characters
            element = translateSpecialChar(element);

            if (element.length() <= 0)
               continue;

            if (stackOfTags.isEmpty())
               rootTags.add(new XMLString(element));
            else {
               XMLTag top = stackOfTags.peek();
               top.addChildElement(new XMLString(element));
            }
         }
         //--------------- Create a new xml tag ------------------------
         else {
            ArrayList<String> splittedTag = splitTag(line, i);
            //--------- Get index pointer to the next interesting part of the line ----------------
            i = nextTagClosingChar(line, i)+1;
            //-------------- make tag --------------------
            if (splittedTag.size() <= 0)
               continue;//don't make the new tag

            // Translate special characters
            for (int j=0; j<splittedTag.size(); j++)
               splittedTag.set(j, translateSpecialChar(splittedTag.get(j)));

            String tagName = splittedTag.get(0);
            if (tagName.equals("/")) {
               Log.err("Detected an XML tag with name '/'. Ignoring the tag.");
               onlyValidSyntax = false;
               continue;
            }
            if (tagName.startsWith("/")) {//check if tag is a closing tag
               tagName = tagName.substring(1);
               closingTag = true;
               if (splittedTag.size() > 1) {
                  Log.warn("Detected attributes in the closing tag with name '"+tagName+"'");
                  onlyValidSyntax = false;
               }
            }
            if (splittedTag.size() == 1 && isStandaloneWord(tagName)) {
               tagName = tagName.substring(0, tagName.length()-1);//remove last character ('/')
               standaloneTag = true;
            }

            XMLTag newTag = new XMLTag(tagName);

            int j;
            for (j=1; j+1<splittedTag.size(); j+=2) {
               newTag.putAttribute(splittedTag.get(j), splittedTag.get(j+1));
            }
            if (j<splittedTag.size() && isStandaloneWord(splittedTag.get(j))) {//standalone tag
               standaloneTag = true;
            }

            //---------- Add tag in the tags tree --------------
            if (standaloneTag) {
               if (stackOfTags.isEmpty()) {
                  Log.err("There is a problem in the architecture of the input file. Detected a standalone tag as root of the file.");
                  throw new IllegalArgumentException("Invalid architecture of the input file (first tag being self-closing).");
               }
               else {
                  XMLTag top = stackOfTags.peek();
                  top.addChildElement(newTag);
               }
            }
            else if (!closingTag) {
               //---------- Put tag in the roots if the stack is empty --------------
               if (stackOfTags.isEmpty()) {
                  rootTags.add(newTag);
               }
               //----------- Put tag in the children elements of the tag on top of the stack ---------
               else {
                  XMLTag top = stackOfTags.peek();
                  top.addChildElement(newTag);
               }
               //----------- Put tag onto the stack -------------------
               stackOfTags.push(newTag);
            }
            else {
               //---------- Remove the tag that is on top of the stack (+check) -------------------
               if (stackOfTags.isEmpty()) {
                  Log.err("Missing an opening tag for tag named '"+tagName+"'. Ignoring the closing tag.");
                  onlyValidSyntax = false;
                  continue;
               }
               if (!stackOfTags.peek().getTagName().equals(tagName)) {
                  Log.err("The closing tag with name "+tagName+" does not close the last opened XML tag ("+stackOfTags.peek().getTagName()+"). Ignoring the closing tag.");
                  onlyValidSyntax = false;
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
   private static boolean isStandaloneWord(String word) {
      Log.fct(6, "Parser.isStandaloneWord");

      if (word == null || word.length() <= 0) {
         Log.warn("Tried to check if an empty word ended with '/'");
         return false;
      }

      return (word.charAt(word.length()-1) == '/');
   }

   /*
    * Returns the part of @source that goes from @index to the first following '<' in @source (or the end of @source)
    * Exception thrown if @source or @index are invalid or if there isn't a '<' at @index
    */
   private static String extractStringElement(String source, int index) {
      Log.fct(6, "Parser.extractStringElement");

      if (source == null || index < 0 || index >= source.length())
         throw new IllegalArgumentException("Tried to extract a string from an empty line or with invalid arguments");

      if (!source.contains("<"))
         return source.substring(index);
      int i;
      for (i=index; i<source.length() && source.charAt(i) != '<'; i++)
      {}
      return source.substring(index, i);
   }

   /*
    * Returns the index of the next character '>' after index @beginningIndex in @str
    * Returns @str.length() if no '>' was found or if there was a problem
    */
   private static int nextTagClosingChar(String str, int beginningIndex) {
      Log.fct(6, "Parser.nextTagClosingChar");

      if (str == null || str.length() <= 0 || beginningIndex < 0 || beginningIndex >= str.length()) {
         Log.warn("Tried to get the index of a character with invalid parameters.");
         return str.length();
      }

      int i;
      for (i=beginningIndex; i<str.length() && str.charAt(i)!='>'; i++) {}

      return i;
   }

   /*
    * Changes all special characters (&gt; and so on)
    * into their Unicode representation (> and so on) in @str
    */
   private static String translateSpecialChar(String str) {
      Log.fct(6, "Parser.translateSpecialChar");

      if (str == null || str.length() <= 0)
         return str;

      String answer = str.replaceAll("&lt;", "<");
      answer = answer.replaceAll("&gt;", ">");
      answer = answer.replaceAll("&amp;", "&");
      answer = answer.replaceAll("&apos;", "'");
      answer = answer.replaceAll("&quot;", "\"");
      return answer;
   }
}
