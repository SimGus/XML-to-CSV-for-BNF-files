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
}
