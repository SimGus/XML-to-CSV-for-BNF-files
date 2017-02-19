package backend.parser;

import java.util.HashMap;
import java.util.ArrayList;

import util.Log;

public class XMLTag implements XMLPart {
   protected static char stringSpacing = '\t';

   protected String name;
   protected HashMap<String, String> attributes = new HashMap<String, String>();
   protected ArrayList<XMLPart> childrenElements = new ArrayList<XMLPart>();

   public XMLTag(String name) {
      if (name == null || name.length() <= 0)
         throw new IllegalArgumentException("Tried to instantiate an XML tag with no name.");
      this.name = name;
   }

   public String getTagName() {
      if (name == null)
         throw new UnsupportedOperationException("Tried to get the name of an empty tag.");
      return name;
   }

   public void putAttribute(String key, String value) {
      if (key == null || key.length() <= 0)
         throw new IllegalArgumentException("Tried to add an empty XML attribute to an XML tag.");
      if (value == null) {
         Log.warn("Tried to add an XML attribute without value. Ignoring it.");
         return;
      }
      if (attributes.containsKey(key))
         Log.log("Replacing value of the attribute '"+key+"'");

      attributes.put(key, value);
   }

   public String getValue(String attribute) {
      if (attribute == null || attribute.length() <= 0) {
         Log.warn("Tried to get value of an empty attribute.");
         return null;
      }
      return attributes.get(attribute);
   }

   public void addChildElement(XMLPart child) {
      if (child == null)
         throw new IllegalArgumentException("Tried to add an empty XML tag as a child element of another one.");

      childrenElements.add(child);
   }

   public ArrayList<XMLPart> getChildrenElements() {
      return childrenElements;
   }

   public String contentsToString() {
      String answer = "";
      answer += name;
      answer += stringSpacing;
      for (String attributeName : attributes.keySet())
         answer += attributeName + stringSpacing;

      for (XMLPart currentChild : childrenElements)
         answer += currentChild.contentsToString() + stringSpacing;

      return answer;
   }

   public String tagNamesToString() {
      String answer = "";
      answer += name + stringSpacing;

      String tmp;
      for (XMLPart currentChild : childrenElements) {
         tmp = currentChild.tagNamesToString();
         if (tmp != null)
            answer += tmp + stringSpacing;
      }

      return answer;
   }

   public void printContents(int alinea) {
      printAlinea(alinea);
      System.out.print("["+name+" (");

      for (String attributeName : attributes.keySet())
         System.out.print(attributeName+" ");
      System.out.println(")");

      for (XMLPart currentChild : childrenElements)
         currentChild.printContents(alinea+1);

      printAlinea(alinea);
      System.out.println("]");
   }

   public void printTagNames(int alinea) {
      printAlinea(alinea);
      System.out.println("["+name);

      for (XMLPart currentChild : childrenElements)
         currentChild.printTagNames(alinea+1);

      printAlinea(alinea);
      System.out.println("]");
   }

   private void printAlinea(int alinea) {
      for (int i=0; i<alinea; i++)
         System.out.print("  ");
   }

   public String getContentsFormatted() {
      if (childrenElements.size() == 0) {
         if (name.equals("lb"))
            return "\\n";
         if (name.equals("dao")) {
            if (attributes.get("href") != null)
               return attributes.get("href");
            return "";
         }
      }
      else if (childrenElements.size() == 1) {
         if (name.equals("persname") || name.equals("title")) {
            if (attributes.get("normal") != null)
               return attributes.get("normal");
            return childrenElements.get(0).getContentsFormatted();
         }
         else if (name.equals("abbr")) {
            String answer = childrenElements.get(0).getContentsFormatted();
            if (attributes.get("expan") != null)
               answer += " ("+attributes.get("expan")+")";
            return answer;
         }
      }

      String answer = "";
      boolean wasLastChildAString = false, dontPutSpaceAtBeginning = true;
      for (XMLPart currentChild : childrenElements) {
         if ((!(currentChild instanceof XMLString) && !currentChild.getTagName().equals("lb"))
            && !wasLastChildAString
            && !dontPutSpaceAtBeginning)
               answer += " ";

         answer += currentChild.getContentsFormatted();

         if (currentChild.getTagName().equals("p")
            || currentChild.getTagName().equals("head")) {
               answer += "\\n";
               dontPutSpaceAtBeginning = true;
         }
         else if (currentChild.getTagName().equals("lb"))
            dontPutSpaceAtBeginning = true;
         else
            dontPutSpaceAtBeginning = false;

         if (currentChild instanceof XMLString)
            wasLastChildAString = true;
         else
            wasLastChildAString = false;
      }
      answer = trim(answer);

      return answer;
   }

   /*
    * Return a string that is the same string as @str but without the whitespaces (' ', '\t' and '\n') at the beginning and the end
    * Removes also the literal "\\n" and "\\t"
    */
   public static String trim(String str) {
      if (str == null || str.length() <= 0)
         return "";

      int i;
      for (i=0; i<str.length(); i++) {
         if (str.charAt(i) != ' ' && str.charAt(i) != '\t' && str.charAt(i) != '\n') {
            if (i+1 >= str.length())
               break;
            if (str.charAt(i) == '\\' && (str.charAt(i+1) == 'n' || str.charAt(i+1) == 't')) {
               i++;
               continue;
            }
            else
               break;
         }
      }

      int j;
      for (j=str.length()-1; j>=0; j--) {
         if (str.charAt(j) != ' ' && str.charAt(j) != '\t' && str.charAt(j) != '\n') {
            if (j-1 <= 0)
               break;
            if (str.charAt(j-1) == '\\' && (str.charAt(j) == 'n' || str.charAt(j) == 't')) {
               j--;
               continue;
            }
            else
               break;
         }
      }

      if (j<i)
         return "";
      return str.substring(i, j+1);
   }
}
