package backend.parser;

public interface XMLPart {
   /*
    * A string that's formed recursively of the tag name,
    * the attribute names and the contents of the XMLPart
    * (just the content if it has no name or attribute)
    * separated by a tabulation '\t'
    */
   String contentsToString();
   String tagNamesToString();//same thing just with the tag names

   /*
    * Prints a representation of the XMLPart and its contents
    * @alinea is used for layout
    */
   void printContents(int alinea);
   void printTagNames(int alinea);//same thing only with the tag names
}
