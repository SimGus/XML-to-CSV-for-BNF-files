package backend.transcripter;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;

import backend.parser.Parser;
import backend.files.FileOpener;
import backend.parser.XMLPart;

public class Interpreter {
   private static final String[] ignoredList = {"p", "lb", "num", "emph", "language", "head", "persname", "corpname", "extref"};

   /*
    * Writes the noticeable tag names in the file named @outputFileName
    */
   public static void writeNoticeableTagNames(String outputFileName) {
      ArrayList<String> namesToWrite = new ArrayList<String>();

      for (String currentTagName : getNoticeableTagNames())
         namesToWrite.add(currentTagName);

      FileOpener.writeFile(outputFileName, namesToWrite);
   }

   /*
    * Returns a list (without duplicates) of all the tag names in the current input file
    */
   public static HashSet<String> getNoticeableTagNames() {
      if (Parser.rootTags.size() <= 0)
         throw new UnsupportedOperationException("Tried to get all the tag names before the input file was read (or with an empty input file)");

      HashSet<String> answer = new HashSet<String>();
      for (XMLPart currentXMLPart : Parser.rootTags)
         recursivelyAddTags(answer, currentXMLPart);
      return answer;
   }

   private static void recursivelyAddTags(HashSet<String> set, XMLPart partToAdd) {
      if (set == null || partToAdd == null)
         throw new IllegalArgumentException("Tried to get noticeable tags in a problematic way");

      System.out.println(partToAdd.getTagName());
      if (partToAdd.getTagName() != null && !Arrays.asList(ignoredList).contains(partToAdd.getTagName())) {
         System.out.println("add");
         set.add(partToAdd.getTagName());
      }

      for (XMLPart current : partToAdd.getChildrenElements())
         recursivelyAddTags(set, current);
   }
}
