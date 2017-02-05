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
}
