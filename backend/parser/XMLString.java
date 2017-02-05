package backend.parser;

public class XMLString implements XMLPart {
   protected String content;

   public XMLString(String str) {
      if (str == null)
         throw new IllegalArgumentException("Tried to instantiate an XML string with no content");

      content = str;
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
      //nothing to do
   }

   private void printAlinea(int alinea) {
      for (int i=0; i<alinea; i++)
         System.out.print("  ");
   }
}
