package backend.parser;

import java.util.ArrayList;

public class XMLString implements XMLPart {
   protected String content;

   public XMLString(String str) {
      if (str == null)
         throw new IllegalArgumentException("Tried to instantiate an XML string with no content");

      content = str;
   }

   public String getTagName() {
      return null;
   }

   public ArrayList<XMLPart> getChildrenElements() {
      return new ArrayList<XMLPart>();
   }

   public String contentsToString() {
      return content;
   }

   public String tagNamesToString() {
      return null;
   }

   public void printContents(int alinea) {
      printAlinea(alinea);
      System.out.println(content);
   }

   public void printTagNames(int alinea) {
      printAlinea(alinea);
      System.out.println("String");
   }

   private void printAlinea(int alinea) {
      for (int i=0; i<alinea; i++)
         System.out.print("  ");
   }

   public String getContentsFormatted() {
      return content;
   }
}
