package backend.transcripter;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import util.Log;
import backend.parser.Parser;
import backend.parser.XMLPart;
import backend.parser.XMLTag;
import gui.Window;
import static util.LogType.*;

public class Interpreter {
   public enum TagType {
      CONTAINER,//might contain tags with valuable information
      FIELD,//corresponds to a database field, thus contains valuable information
      CONTENT,//that tag is supposed to be inside a bigger tag that corresponds to a database field
      IGNORE;//don't take this tag into account
   }

   private static final String XMLStringTagName = "XMLString";
   private static boolean invalidArchLogged = false;

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
      tagTypesMap.put(XMLStringTagName, TagType.IGNORE);

      tagTypesMap.put("ead", TagType.CONTAINER);
      tagTypesMap.put("eadheader", TagType.CONTAINER);
      tagTypesMap.put("eadid", TagType.FIELD);

      tagTypesMap.put("filedesc", TagType.IGNORE);
      tagTypesMap.put("titlestmt", TagType.IGNORE);
      tagTypesMap.put("titleproper", TagType.IGNORE);
      tagTypesMap.put("subtitle", TagType.IGNORE);
      tagTypesMap.put("publicationstmt", TagType.IGNORE);
      tagTypesMap.put("publisher", TagType.IGNORE);
      tagTypesMap.put("profiledesc", TagType.IGNORE);
      tagTypesMap.put("creation", TagType.IGNORE);
      tagTypesMap.put("langusage", TagType.IGNORE);

      tagTypesMap.put("archdesc", TagType.CONTAINER);
      tagTypesMap.put("did", TagType.CONTAINER);
      tagTypesMap.put("unitid", TagType.FIELD);//different for different attributes
      tagTypesMap.put("unittitle", TagType.FIELD);
      tagTypesMap.put("title", TagType.CONTENT);
      tagTypesMap.put("unitdate", TagType.FIELD);
      tagTypesMap.put("langmaterial", TagType.FIELD);
      tagTypesMap.put("repository", TagType.IGNORE);

      tagTypesMap.put("physdesc", TagType.CONTAINER);
      tagTypesMap.put("geogname", TagType.IGNORE);
      tagTypesMap.put("physfacet", TagType.FIELD);
      tagTypesMap.put("extent", TagType.FIELD);
      tagTypesMap.put("dimensions", TagType.FIELD);
      tagTypesMap.put("origination", TagType.FIELD);

      tagTypesMap.put("bibliography", TagType.FIELD);
      tagTypesMap.put("bibref", TagType.CONTENT);
      tagTypesMap.put("processinfo", TagType.IGNORE);
      tagTypesMap.put("dsc", TagType.IGNORE);
      tagTypesMap.put("altformavail", TagType.FIELD);
      tagTypesMap.put("dao", TagType.IGNORE);//@Thomas "Je ne vois pas ce champ pour l'instant"
      tagTypesMap.put("custodhist", TagType.IGNORE);//@Thomas "Je ne vois pas ce champ pour l'instant"
      tagTypesMap.put("scopecontent", TagType.FIELD);

      tagTypesMap.put("date", TagType.CONTENT);
      tagTypesMap.put("p", TagType.IGNORE);//If not in a field
      tagTypesMap.put("lb", TagType.IGNORE);//If not in a field
      tagTypesMap.put("num", TagType.CONTENT);
      tagTypesMap.put("emph", TagType.CONTENT);
      tagTypesMap.put("head", TagType.CONTENT);
      tagTypesMap.put("language", TagType.CONTENT);
      tagTypesMap.put("persname", TagType.CONTENT);
      tagTypesMap.put("corpname", TagType.CONTENT);
      tagTypesMap.put("extref", TagType.IGNORE);

      tagTypesMap.put("abbr", TagType.CONTENT);
      tagTypesMap.put("abstract", TagType.IGNORE);
      tagTypesMap.put("accessrestrict", TagType.IGNORE);
      tagTypesMap.put("accruals", TagType.IGNORE);
      tagTypesMap.put("controlaccess", TagType.IGNORE);
      tagTypesMap.put("acqinfo", TagType.IGNORE);
      tagTypesMap.put("address", TagType.IGNORE);
      tagTypesMap.put("addressline", TagType.IGNORE);
      tagTypesMap.put("appraisal", TagType.IGNORE);
      tagTypesMap.put("arc", TagType.IGNORE);
      tagTypesMap.put("archdescgrp", TagType.IGNORE);
      tagTypesMap.put("archref", TagType.IGNORE);
      tagTypesMap.put("arrangement", TagType.IGNORE);
      tagTypesMap.put("author", TagType.IGNORE);
      tagTypesMap.put("bibseries", TagType.IGNORE);
      tagTypesMap.put("bioghist", TagType.IGNORE);
      tagTypesMap.put("blockquote", TagType.IGNORE);
      tagTypesMap.put("change", TagType.IGNORE);
      tagTypesMap.put("chronitem", TagType.IGNORE);
      tagTypesMap.put("chronlist", TagType.IGNORE);
      tagTypesMap.put("colspec", TagType.IGNORE);
      tagTypesMap.put("container", TagType.IGNORE);
      tagTypesMap.put("daodesc", TagType.IGNORE);
      tagTypesMap.put("daogrp", TagType.IGNORE);
      tagTypesMap.put("daoloc", TagType.IGNORE);
      tagTypesMap.put("defitem", TagType.IGNORE);
      tagTypesMap.put("descgrp", TagType.IGNORE);
      tagTypesMap.put("descrules", TagType.IGNORE);
      tagTypesMap.put("div", TagType.IGNORE);
      tagTypesMap.put("dscgrp", TagType.IGNORE);
      tagTypesMap.put("eadgrp", TagType.IGNORE);
      tagTypesMap.put("edition", TagType.IGNORE);
      tagTypesMap.put("editionstmt", TagType.IGNORE);
      tagTypesMap.put("entry", TagType.IGNORE);
      tagTypesMap.put("event", TagType.IGNORE);
      tagTypesMap.put("eventgrp", TagType.IGNORE);
      tagTypesMap.put("expan", TagType.IGNORE);
      tagTypesMap.put("extptr", TagType.IGNORE);
      tagTypesMap.put("extptrloc", TagType.IGNORE);
      tagTypesMap.put("extrefloc", TagType.IGNORE);
      tagTypesMap.put("famname", TagType.IGNORE);
      tagTypesMap.put("fileplan", TagType.IGNORE);
      tagTypesMap.put("frontmatter", TagType.IGNORE);
      tagTypesMap.put("function", TagType.IGNORE);
      tagTypesMap.put("genreform", TagType.IGNORE);
      tagTypesMap.put("imprint", TagType.IGNORE);
      tagTypesMap.put("index", TagType.IGNORE);
      tagTypesMap.put("indexentry", TagType.IGNORE);
      tagTypesMap.put("item", TagType.IGNORE);
      tagTypesMap.put("label", TagType.IGNORE);
      tagTypesMap.put("legalstatus", TagType.IGNORE);
      tagTypesMap.put("linkgrp", TagType.IGNORE);
      tagTypesMap.put("list", TagType.IGNORE);
      tagTypesMap.put("listhead", TagType.IGNORE);
      tagTypesMap.put("materialspec", TagType.IGNORE);
      tagTypesMap.put("name", TagType.IGNORE);
      tagTypesMap.put("namegrp", TagType.IGNORE);
      tagTypesMap.put("note", TagType.IGNORE);
      tagTypesMap.put("notestmt", TagType.IGNORE);
      tagTypesMap.put("occupation", TagType.IGNORE);
      tagTypesMap.put("odd", TagType.IGNORE);
      tagTypesMap.put("originalsloc", TagType.IGNORE);
      tagTypesMap.put("otherfindaid", TagType.IGNORE);
      tagTypesMap.put("physloc", TagType.IGNORE);
      tagTypesMap.put("phystech", TagType.IGNORE);
      tagTypesMap.put("prefercite", TagType.IGNORE);
      tagTypesMap.put("processdesc", TagType.IGNORE);
      tagTypesMap.put("ptr", TagType.IGNORE);
      tagTypesMap.put("ptrgrp", TagType.IGNORE);
      tagTypesMap.put("ptrloc", TagType.IGNORE);
      tagTypesMap.put("ref", TagType.IGNORE);
      tagTypesMap.put("refloc", TagType.IGNORE);
      tagTypesMap.put("relatedmaterial", TagType.IGNORE);
      tagTypesMap.put("resource", TagType.IGNORE);
      tagTypesMap.put("revisiondesc", TagType.IGNORE);
      tagTypesMap.put("row", TagType.IGNORE);
      tagTypesMap.put("runner", TagType.IGNORE);
      tagTypesMap.put("separatedmaterial", TagType.IGNORE);
      tagTypesMap.put("seriesstmt", TagType.IGNORE);
      tagTypesMap.put("sponsor", TagType.IGNORE);
      tagTypesMap.put("subarea", TagType.IGNORE);
      tagTypesMap.put("subject", TagType.IGNORE);
      tagTypesMap.put("table", TagType.IGNORE);
      tagTypesMap.put("tbody", TagType.IGNORE);
      tagTypesMap.put("tgroup", TagType.IGNORE);
      tagTypesMap.put("thead", TagType.IGNORE);
      tagTypesMap.put("titlepage", TagType.IGNORE);
      tagTypesMap.put("userestrict", TagType.IGNORE);

      tagTypesMap.put("c", TagType.CONTAINER);
      tagTypesMap.put("c01", TagType.CONTAINER);
      tagTypesMap.put("c02", TagType.CONTAINER);
      tagTypesMap.put("c03", TagType.CONTAINER);
      tagTypesMap.put("c04", TagType.CONTAINER);
      tagTypesMap.put("c05", TagType.CONTAINER);
      tagTypesMap.put("c06", TagType.CONTAINER);
      tagTypesMap.put("c07", TagType.CONTAINER);
      tagTypesMap.put("c08", TagType.CONTAINER);
      tagTypesMap.put("c09", TagType.CONTAINER);
      tagTypesMap.put("c10", TagType.CONTAINER);
      tagTypesMap.put("c11", TagType.CONTAINER);
      tagTypesMap.put("c12", TagType.CONTAINER);
      tagTypesMap.put("head01", TagType.CONTAINER);
      tagTypesMap.put("head02", TagType.CONTAINER);


      //============== Initialize fieldNames =====================
      fieldNames.put("eadid", "Nom BNF");
      fieldNames.put("unitid", "Cote actuelle");//manage differently for different attributes
      fieldNames.put("unittitle", "Contenu");
      fieldNames.put("scopecontent", "Contenu");
      fieldNames.put("unitdate", "Siècle");//special managment TODO
      fieldNames.put("physfacet", "Physical facet");//manage differently for different attributes
      fieldNames.put("extent", "Nombre de feuillets");
      fieldNames.put("dimensions", "Dimensions");
      fieldNames.put("origination", "Provenance moderne");
      // fieldNames.put("repository", "Repository");
      fieldNames.put("langmaterial", "Langue");

      fieldNames.put("bibliography", "Bibliographie");//separate the fields by '/'
      fieldNames.put("altformavail", "Reproduction");
      // fieldNames.put("dao", "Digital archival object");//@Thomas "Je ne vois pas ce champ pour l'instant"
      // fieldNames.put("custodhist", "Custodial history");//@Thomas "Je ne vois pas ce champ pour l'instant"

      // fieldNames.put("abstract", "Abstract");
      // fieldNames.put("accessrestrict", "Conditions governing access");
      // fieldNames.put("accruals", "Accruals");
      // fieldNames.put("controlaccess", "Controlled access headings");
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

      runTranslation(window);

      ArrayList<String> answer = new ArrayList<String>();

      ArrayList<String> fieldOrder = new ArrayList<String>();
      for (String fieldName : translatedFields.keySet()) {
         if (fieldName == null)
            continue;
         if (translatedFields.get(fieldName) != null)
            fieldOrder.add(fieldName);
      }
      if (fieldOrder.size() <= 0) {
         window.addLog("All the fields in the XML input file seem to be empty.",
            "Tous les champs dans le fichier XML d'entrée semblent être vdes.",
            WARNING);
         return new ArrayList<String>();
      }

      //READABLE TRANSLATION TODO REMOVE
      // for (String fieldName : fieldOrder) {
      //    answer.add(fieldName + "\t" + translatedFields.get(fieldName));
      // }

      //============ Write first line (field names) ===============
      String currentLine = "";
      for (String fieldName : fieldOrder) {
         currentLine += fieldName;
         currentLine += "\t";
      }
      currentLine = currentLine.substring(0, currentLine.length()-1);//Remove last '\t'
      answer.add(currentLine);

      //============ Write next lines (each object) ===============
      currentLine = "";
      String currentValue;
      for (String fieldName : fieldOrder) {
         currentLine += translatedFields.get(fieldName);
         currentLine += "\t";
      }
      if (currentLine.length() > 0) {
         currentLine = currentLine.substring(0, currentLine.length()-1);//Remove last '\t'
         answer.add(currentLine);
      }

      return answer;
   }

   public static void runTranslation(Window window) {
      for (XMLPart currentRoot : Parser.rootTags) {
         translateTags(currentRoot, window);//Translates tags recursively
      }
   }

   /*
    * Iterates recursively through the tags tree (thanks to argument @tag)
    * and put the interesting translations in the HashMap @translatedFields
    */
   private static void translateTags(XMLPart tag, Window window) {
      /*if (tag.getTagName() == null) {//getTagName never returns null
         Log.err("The tag tree seems to be invalid. The input file must have an invalid architecture.");
         return;
      }*/

      if (!tag.getTagName().equals(XMLStringTagName) && tagTypesMap.get(tag.getTagName()) == null) {
         Log.warn("Found an unknown tag in the input file : '"+tag.getTagName()+"'. Ignoring it.");
         window.addLog("Ignoring an unknown tag in the XML file : '"+tag.getTagName()+"'.",
            "La balise '"+tag.getTagName()+"' trouvée dans le fichier XML est inconnue et ne sera pas prise en compte.",
            MINOR);
         return;
      }
      switch (tagTypesMap.get(tag.getTagName())) {
         case CONTENT:
            if (!invalidArchLogged) {
               Log.err("The input file seems to have an invalid architecture. Tag '"+tag.getTagName()+"' is misplaced (content : '"+tag.getContentsFormatted()+"').");
               window.addLog("The XML file seems to have an invalid architecture. The program will try to keep running anyway. A tag with tag name '"+tag.getTagName()+"' is misplaced.",
                  "Le fichier XML semble avoir une architecture invalide. Le programme va tout de même essayer de continuer son exécution. Une balise nommée '"+tag.getTagName()+"' est mal placée.",
                  WARNING);
               invalidArchLogged = true;
            }
            break;
         case CONTAINER:
            for (XMLPart currentTag : tag.getChildrenElements()) {
               translateTags(currentTag, window);
            }
            break;
         case IGNORE://nothing to do
            break;
         case FIELD:
            if (!specialTreatement(tag, window)) {
               String fieldName = fieldNames.get(tag.getTagName());
               String fieldValue = tag.getContentsFormatted();
               if (fieldValue != null)
                  updateField(fieldName, fieldValue);
            }
            break;
         default:
            Log.err("There was an error translating a tag.");
            break;
      }
   }

   /*
    * If @currentTag must have a special treatement (several fields for different attributes of the tag),
    * put the different things in @translatedFields and return true
    * Else return false
    */
   private static boolean specialTreatement(XMLPart currentTag, Window window) {
      if (currentTag.getTagName().equals("string"))
         return false;

      XMLTag tag = (XMLTag) currentTag;
      switch (tag.getTagName()) {
         case "unitid":
            {
               String attribute = tag.getAttribute("type");
               if (attribute == null)
                  return false;//will get in @translatedFields the regular way

               String fieldValue = tag.getContentsFormatted();
               if (fieldValue != null) {
                  if (attribute.equals("cote"))
                     updateField("Cote actuelle", fieldValue);
                  else if (attribute.equals("ancienne cote"))
                     updateField("Ancienne cote", fieldValue);
                  else
                     return false;
               }
               return true;
            }
         case "physfacet":
            {
               String attribute = tag.getAttribute("type");
               if (attribute == null)
                  return false;//will get in @translatedFields the regular way

               String fieldValue = tag.getContentsFormatted();
               if (fieldValue != null) {
                  if (attribute.equals("décoration"))
                     updateField("Décor", fieldValue);
                  else if (attribute.equals("support"))
                     updateField("Support", fieldValue);
                  else if (attribute.equals("reliure"))
                     updateField("Reliure", fieldValue);
                  else if (attribute.equals("sceau"))
                     updateField("Histoire BNF", fieldValue);
                  else
                     return false;
               }
               return true;
            }
         // case "unitdate"://TODO
         //    {
         //       String fieldValue = tag.getContentsFormatted();
         //       if (fieldValue.contains("(") && fieldValue.contains(")")) {
         //          if (fieldValue.contains("moitié")) {
         //             //TODO parse dates
         //             String precisionPart = fieldValue.substring(fieldValue.indexOf("(")+1, fieldValue.indexOf(")"));
         //             String century = fieldValue.substring(0, fieldValue.indexOf("("));
         //
         //             updateField("Date approximative", fieldValue);
         //             return true;
         //          }
         //          else if (fieldValue.contains("quart")) {
         //             //TODO parse dates
         //             String precisionPart = fieldValue.substring(fieldValue.indexOf("(")+1, fieldValue.indexOf(")"));
         //
         //             updateField("Date approximative", fieldValue);
         //             return true;
         //          }
         //       }
         //       //else
         //       String currentStoredValue = translatedFields.get("Date approximative");
         //       if (currentStoredValue != null)
         //          fieldValue = currentStoredValue + " / " + fieldValue;
         //       translatedFields.put("Date approximative", fieldValue);
         //       return true;
         //    }
         case "origination":
            updateField(fieldNames.get("origination"), tag.getContentsFormatted());
            return true;
      }
      return false;
   }

   private static void updateField(String fieldName, String fieldValue) {
      String currentStoredValue = translatedFields.get(fieldName);
      if (currentStoredValue != null)
         fieldValue = currentStoredValue + " / " + fieldValue;
      translatedFields.put(fieldName, fieldValue);
   }

   private static void romanToDecimal(java.lang.String romanNumber) {
      int decimal = 0;
      int lastNumber = 0;
      String romanNumeral = romanNumber.toUpperCase();
      for (int x = romanNumeral.length() - 1; x >= 0 ; x--) {
         char convertToDecimal = romanNumeral.charAt(x);
         switch (convertToDecimal) {
            case 'M':
               decimal = processDecimal(1000, lastNumber, decimal);
               lastNumber = 1000;
               break;
            case 'D':
               decimal = processDecimal(500, lastNumber, decimal);
               lastNumber = 500;
               break;
            case 'C':
               decimal = processDecimal(100, lastNumber, decimal);
               lastNumber = 100;
               break;
            case 'L':
               decimal = processDecimal(50, lastNumber, decimal);
               lastNumber = 50;
               break;
            case 'X':
               decimal = processDecimal(10, lastNumber, decimal);
               lastNumber = 10;
               break;
            case 'V':
               decimal = processDecimal(5, lastNumber, decimal);
               lastNumber = 5;
               break;
            case 'I':
               decimal = processDecimal(1, lastNumber, decimal);
               lastNumber = 1;
               break;
         }
      }
   }

   private static int processDecimal(int decimal, int lastNumber, int lastDecimal) {
        if (lastNumber > decimal) {
            return lastDecimal - decimal;
        } else {
            return lastDecimal + decimal;
        }
    }
}
