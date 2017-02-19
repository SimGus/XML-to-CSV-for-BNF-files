package backend.transcripter;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import util.Log;
import backend.parser.Parser;
import backend.parser.XMLPart;
import gui.Window;
import static util.LogType.*;

public class Interpreter {
   public enum TagType {
      CONTAINER,//might contain tags with valuable information
      FIELD,//corresponds to a database field, thus contains valuable information
      CONTENT,//that tag is supposed to be inside a bigger tag that corresponds to a database field
      IGNORE;//don't take this tag into account
   }

   private static final HashMap<String, TagType> tagTypesMap = new HashMap<String, TagType>();
   private static final HashMap<String, String> fieldNames = new HashMap<String, String>();

   private static HashMap<String, String> translatedFields = new HashMap<String, String>();

   /*
    * Initializes the HashMap @tagTypesMap that contains information about how a certain tag name should be treated
    * And the HashMap @fieldNames that contains the names of the fields in the database and their corresponding name
    * in the XML file
    */
   public static void initializeMaps() {
      //================== Initialize tagTypesMap ========================
      tagTypesMap.put("ead", TagType.CONTAINER);
      tagTypesMap.put("eadheader", TagType.CONTAINER);
      tagTypesMap.put("eadid", TagType.FIELD);

      tagTypesMap.put("filedesc", TagType.CONTAINER);
      tagTypesMap.put("titlestmt", TagType.FIELD);
      tagTypesMap.put("titleproper", TagType.CONTENT);
      tagTypesMap.put("subtitle", TagType.CONTENT);
      tagTypesMap.put("publicationstmt", TagType.FIELD);
      tagTypesMap.put("publisher", TagType.CONTENT);
      tagTypesMap.put("profiledesc", TagType.FIELD);
      tagTypesMap.put("creation", TagType.CONTENT);
      tagTypesMap.put("langusage", TagType.CONTENT);

      tagTypesMap.put("archdesc", TagType.CONTAINER);
      tagTypesMap.put("did", TagType.CONTAINER);
      tagTypesMap.put("unitid", TagType.FIELD);
      tagTypesMap.put("unittitle", TagType.FIELD);
      tagTypesMap.put("title", TagType.CONTENT);
      tagTypesMap.put("unitdate", TagType.FIELD);
      tagTypesMap.put("langmaterial", TagType.FIELD);
      tagTypesMap.put("repository", TagType.FIELD);

      tagTypesMap.put("physdesc", TagType.FIELD);//not sure
      tagTypesMap.put("geogname", TagType.CONTENT);
      tagTypesMap.put("physfacet", TagType.CONTENT);
      tagTypesMap.put("extent", TagType.CONTENT);
      tagTypesMap.put("dimensions", TagType.CONTENT);
      tagTypesMap.put("origination", TagType.FIELD);

      tagTypesMap.put("bibliography", TagType.FIELD);//not sure
      tagTypesMap.put("bibref", TagType.CONTENT);
      tagTypesMap.put("processinfo", TagType.FIELD);
      tagTypesMap.put("dsc", TagType.IGNORE);//not sure
      tagTypesMap.put("altformavail", TagType.FIELD);
      tagTypesMap.put("dao", TagType.FIELD);
      tagTypesMap.put("custodhist", TagType.FIELD);
      tagTypesMap.put("scopecontent", TagType.FIELD);

      tagTypesMap.put("date", TagType.CONTENT);
      tagTypesMap.put("p", TagType.CONTENT);
      tagTypesMap.put("lb", TagType.CONTENT);
      tagTypesMap.put("num", TagType.CONTENT);
      tagTypesMap.put("emph", TagType.CONTENT);
      tagTypesMap.put("head", TagType.CONTENT);
      tagTypesMap.put("language", TagType.CONTENT);
      tagTypesMap.put("persname", TagType.CONTENT);
      tagTypesMap.put("corpname", TagType.CONTENT);
      tagTypesMap.put("extref", TagType.CONTENT);

      tagTypesMap.put("abbr", TagType.CONTENT);
      tagTypesMap.put("abstract", TagType.FIELD);

      //============== Initialize fieldNames =====================
      fieldNames.put("EAD identifier", "eadid");
      fieldNames.put("Title statement", "titlestmt");
      fieldNames.put("Publication statement", "publicationstmt");
      fieldNames.put("Profile description", "profiledesc");

      fieldNames.put("Identifier of the unit", "unitid");
      fieldNames.put("Title of the unit", "unittitle");
      fieldNames.put("Date of the unit", "unitdate");
      fieldNames.put("Physical description", "physdesc");
      fieldNames.put("Origination", "origination");
      fieldNames.put("Repository", "repository");
      fieldNames.put("Language of the material", "langmaterial");

      fieldNames.put("Bibliography", "bibliography");
      fieldNames.put("Processing information", "processinfo");
      fieldNames.put("Alternative form available", "altformavail");
      fieldNames.put("Digital archival object", "dao");
      fieldNames.put("Custodial history", "custodhist");
      fieldNames.put("Scope and content", "scopecontent");

      fieldNames.put("Abstract", "abstract");
   }

   /*
    * Resets @translatedFields
    */
   public static void reset() {
      translatedFields = new HashMap<String, String>();
   }

   /*
    * @pre :   The input XML file has been successfully read
    *          and the tags tree @Parser.rootTags is initialized
    * Iterate through the tag tree and translates it to the Strings
    * that will be written in the output file
    * Each string in the ArrayList returned is a line of the file
    */
   public static ArrayList<String> translateTree(Window window) {
      if (Parser.rootTags.size() <= 0) {
         window.addLog("There seems to be nothing to translate in the XML file.",
            "Il semble qu'il n'y ait rien à traduire dans le fichier XML.",
            WARNING);
         return new ArrayList<String>();
      }

      XMLPart currentTag;
      for (XMLPart currentRoot : Parser.rootTags) {
         translateTag(currentRoot, window);
      }

      ArrayList<String> answer = new ArrayList<String>();

      //============ Write first line (field names) ===============
      String currentLine = "";
      for (String fieldName : fieldNames.keySet()) {
         if (translatedFields.get(fieldNames.get(fieldName)) != null) {
            currentLine += fieldName;
            currentLine += "\t";
         }
      }
      currentLine = currentLine.substring(0, currentLine.length()-1);//Remove last '\t'
      answer.add(currentLine);

      //============ Write next lines (each object) ===============
      currentLine = "";
      String currentValue;
      for (String fieldName : fieldNames.keySet()) {
         currentValue = translatedFields.get(fieldNames.get(fieldName));
         if (currentValue != null) {
            currentLine += currentValue;
            currentLine += "\t";
         }
      }
      if (currentLine.length() > 0) {
         currentLine = currentLine.substring(0, currentLine.length()-1);//Remove last '\t'
         answer.add(currentLine);
      }

      return answer;
   }

   /*
    * Iterates recursively through the tags tree (thanks to argument @tag)
    * and put the interesting translations in the HashMap @translatedFields
    */
   private static void translateTag(XMLPart tag, Window window) {
      /*if (tag.getTagName() == null) {//getTagName never returns null
         Log.err("The tag tree seems to be invalid. The input file must have an invalid architecture.");
         return;
      }*/

      if (tagTypesMap.get(tag.getTagName()) == null) {
         Log.warn("Found an unknown tag in the input file : '"+tag.getTagName()+"'. Ignoring it.");
         window.addLog("Ignoring an unknown tag in the XML file : '"+tag.getTagName()+"'.",
            "La balise '"+tag.getTagName()+"' trouvée dans le fichier XML est inconnue et ne sera pas prise en compte.",
            MINOR);
         return;
      }
      switch (tagTypesMap.get(tag.getTagName())) {
         case CONTENT:
            Log.err("The input file seems to have an invalid architecture.");
            window.addLog("The XML file seems to have an invalid architecture. The program will try to keep running anyway.",
               "Le fichier XML semble avoir une architecture invalide. Le programme va tout de même essayer de continuer son exécution.",
               WARNING);
            break;
         case CONTAINER:
            for (XMLPart currentTag : tag.getChildrenElements()) {
               translateTag(currentTag, window);
            }
            break;
         case IGNORE://nothing to do
            break;
         case FIELD:
            String fieldValue = tag.getContentsFormatted();
            if (fieldValue != null) {
               String currentStoredValue = translatedFields.get(tag.getTagName());
               if (currentStoredValue != null)
                  fieldValue = currentStoredValue + "\\n" + fieldValue;
               translatedFields.put(tag.getTagName(), fieldValue);
            }
            System.out.println("=> "+fieldValue);
            break;
         default:
            Log.err("There was an error translating a tag.");
            break;
      }
   }
}
