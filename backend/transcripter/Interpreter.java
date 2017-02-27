package backend.transcripter;

import java.util.HashMap;
import java.util.HashSet;
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
      FIELD_MAIN,//corresponds to a database field and should have the same value for all the materials (main and sub-materials)
      CONTENT,//that tag is supposed to be inside a bigger tag that corresponds to a database field
      IGNORE,//don't take this tag into account
      LEVEL,//changes the level of description
      FEEDBACK;//put it in a debug field so that Thomas can give feedback
   }
   public enum ParentFieldBehavior {
      FILL,//if the child material has this field unset, set it to the value of the parent material
      IGNORE;
   }

   private static final String XMLStringTagName = "XMLString", feedbackFieldName = "DEBUG";
   private static boolean invalidArchLogged = false;

   private static final HashMap<String, TagType> tagTypesMap = new HashMap<String, TagType>();
   private static final HashMap<String, String> fieldNames = new HashMap<String, String>();
   private static final HashMap<String, ParentFieldBehavior> parentFieldsBehaviors = new HashMap<String, ParentFieldBehavior>();

   private static ArrayList<HashMap<String, String>> translatedFields = new ArrayList<HashMap<String, String>>();
   private static int currentMapIndex = 0;
   private static boolean mainMaterialDescriptionEncountered = false;
   private static ArrayList<Integer> parentDescriptionPointers = new ArrayList<Integer>();//stores which of the other description in @translatedFields you should fall back on for the fields that haven't been assigned (some points are described in the parent material)

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

      tagTypesMap.put("bibliography", TagType.FIELD_MAIN);
      tagTypesMap.put("bibref", TagType.CONTENT);
      tagTypesMap.put("processinfo", TagType.IGNORE);
      tagTypesMap.put("dsc", TagType.CONTAINER);
      tagTypesMap.put("altformavail", TagType.FIELD);
      tagTypesMap.put("dao", TagType.IGNORE);//@Thomas "Je ne vois pas ce champ pour l'instant"
      tagTypesMap.put("custodhist", TagType.FIELD);
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
      tagTypesMap.put("abstract", TagType.FEEDBACK);
      tagTypesMap.put("accessrestrict", TagType.FEEDBACK);
      tagTypesMap.put("accruals", TagType.FEEDBACK);
      tagTypesMap.put("controlaccess", TagType.IGNORE);
      tagTypesMap.put("acqinfo", TagType.FEEDBACK);
      tagTypesMap.put("address", TagType.FEEDBACK);
      tagTypesMap.put("addressline", TagType.FEEDBACK);
      tagTypesMap.put("appraisal", TagType.FEEDBACK);
      tagTypesMap.put("arc", TagType.FEEDBACK);
      tagTypesMap.put("archdescgrp", TagType.FEEDBACK);
      tagTypesMap.put("archref", TagType.FEEDBACK);
      tagTypesMap.put("arrangement", TagType.FEEDBACK);
      tagTypesMap.put("author", TagType.FEEDBACK);
      tagTypesMap.put("bibseries", TagType.FEEDBACK);
      tagTypesMap.put("bioghist", TagType.FEEDBACK);
      tagTypesMap.put("blockquote", TagType.FEEDBACK);
      tagTypesMap.put("change", TagType.FEEDBACK);
      tagTypesMap.put("chronitem", TagType.FEEDBACK);
      tagTypesMap.put("chronlist", TagType.FEEDBACK);
      tagTypesMap.put("colspec", TagType.FEEDBACK);
      tagTypesMap.put("container", TagType.FEEDBACK);
      tagTypesMap.put("daodesc", TagType.FEEDBACK);
      tagTypesMap.put("daogrp", TagType.FEEDBACK);
      tagTypesMap.put("daoloc", TagType.FEEDBACK);
      tagTypesMap.put("defitem", TagType.FEEDBACK);
      tagTypesMap.put("descgrp", TagType.FEEDBACK);
      tagTypesMap.put("descrules", TagType.FEEDBACK);
      tagTypesMap.put("div", TagType.FEEDBACK);
      tagTypesMap.put("dscgrp", TagType.FEEDBACK);
      tagTypesMap.put("eadgrp", TagType.FEEDBACK);
      tagTypesMap.put("edition", TagType.FEEDBACK);
      tagTypesMap.put("editionstmt", TagType.FEEDBACK);
      tagTypesMap.put("entry", TagType.FEEDBACK);
      tagTypesMap.put("event", TagType.FEEDBACK);
      tagTypesMap.put("eventgrp", TagType.FEEDBACK);
      tagTypesMap.put("expan", TagType.FEEDBACK);
      tagTypesMap.put("extptr", TagType.FEEDBACK);
      tagTypesMap.put("extptrloc", TagType.FEEDBACK);
      tagTypesMap.put("extrefloc", TagType.FEEDBACK);
      tagTypesMap.put("famname", TagType.FEEDBACK);
      tagTypesMap.put("fileplan", TagType.FEEDBACK);
      tagTypesMap.put("frontmatter", TagType.FEEDBACK);
      tagTypesMap.put("function", TagType.FEEDBACK);
      tagTypesMap.put("genreform", TagType.FEEDBACK);
      tagTypesMap.put("imprint", TagType.FEEDBACK);
      tagTypesMap.put("index", TagType.FEEDBACK);
      tagTypesMap.put("indexentry", TagType.FEEDBACK);
      tagTypesMap.put("item", TagType.FEEDBACK);
      tagTypesMap.put("label", TagType.FEEDBACK);
      tagTypesMap.put("legalstatus", TagType.FEEDBACK);
      tagTypesMap.put("linkgrp", TagType.FEEDBACK);
      tagTypesMap.put("list", TagType.FEEDBACK);
      tagTypesMap.put("listhead", TagType.FEEDBACK);
      tagTypesMap.put("materialspec", TagType.FEEDBACK);
      tagTypesMap.put("name", TagType.FEEDBACK);
      tagTypesMap.put("namegrp", TagType.FEEDBACK);
      tagTypesMap.put("note", TagType.FEEDBACK);
      tagTypesMap.put("notestmt", TagType.FEEDBACK);
      tagTypesMap.put("occupation", TagType.FEEDBACK);
      tagTypesMap.put("odd", TagType.FEEDBACK);
      tagTypesMap.put("originalsloc", TagType.FEEDBACK);
      tagTypesMap.put("otherfindaid", TagType.FEEDBACK);
      tagTypesMap.put("physloc", TagType.FEEDBACK);
      tagTypesMap.put("phystech", TagType.FEEDBACK);
      tagTypesMap.put("prefercite", TagType.FEEDBACK);
      tagTypesMap.put("processdesc", TagType.FEEDBACK);
      tagTypesMap.put("ptr", TagType.FEEDBACK);
      tagTypesMap.put("ptrgrp", TagType.FEEDBACK);
      tagTypesMap.put("ptrloc", TagType.FEEDBACK);
      tagTypesMap.put("ref", TagType.FEEDBACK);
      tagTypesMap.put("refloc", TagType.FEEDBACK);
      tagTypesMap.put("relatedmaterial", TagType.FEEDBACK);
      tagTypesMap.put("resource", TagType.FEEDBACK);
      tagTypesMap.put("revisiondesc", TagType.FEEDBACK);
      tagTypesMap.put("row", TagType.FEEDBACK);
      tagTypesMap.put("runner", TagType.FEEDBACK);
      tagTypesMap.put("separatedmaterial", TagType.FEEDBACK);
      tagTypesMap.put("seriesstmt", TagType.FEEDBACK);
      tagTypesMap.put("sponsor", TagType.FEEDBACK);
      tagTypesMap.put("subarea", TagType.FEEDBACK);
      tagTypesMap.put("subject", TagType.FEEDBACK);
      tagTypesMap.put("table", TagType.FEEDBACK);
      tagTypesMap.put("tbody", TagType.FEEDBACK);
      tagTypesMap.put("tgroup", TagType.FEEDBACK);
      tagTypesMap.put("thead", TagType.FEEDBACK);
      tagTypesMap.put("titlepage", TagType.FEEDBACK);
      tagTypesMap.put("userestrict", TagType.FEEDBACK);

      tagTypesMap.put("c", TagType.LEVEL);
      tagTypesMap.put("c01", TagType.LEVEL);
      tagTypesMap.put("c02", TagType.LEVEL);
      tagTypesMap.put("c03", TagType.LEVEL);
      tagTypesMap.put("c04", TagType.LEVEL);
      tagTypesMap.put("c05", TagType.LEVEL);
      tagTypesMap.put("c06", TagType.LEVEL);
      tagTypesMap.put("c07", TagType.LEVEL);
      tagTypesMap.put("c08", TagType.LEVEL);
      tagTypesMap.put("c09", TagType.LEVEL);
      tagTypesMap.put("c10", TagType.LEVEL);
      tagTypesMap.put("c11", TagType.LEVEL);
      tagTypesMap.put("c12", TagType.LEVEL);
      tagTypesMap.put("head01", TagType.CONTAINER);
      tagTypesMap.put("head02", TagType.CONTAINER);


      //============== Initialize fieldNames =====================
      fieldNames.put("eadid", "Nom BNF");
      fieldNames.put("unitid", "Cote actuelle");//manage differently for different attributes
      fieldNames.put("unittitle", "Contenu");
      fieldNames.put("scopecontent", "Contenu");
      fieldNames.put("unitdate", "Siècle");//special managment TODO (not the time)
      fieldNames.put("physfacet", "Description physique");//manage differently for different attributes
      fieldNames.put("extent", "Nombre de feuillets");
      fieldNames.put("dimensions", "Dimensions");
      fieldNames.put("origination", "Provenance moderne");
      // fieldNames.put("repository", "Repository");
      fieldNames.put("langmaterial", "Langue");

      fieldNames.put("bibliography", "Bibliographie");//separate the fields by '/'
      fieldNames.put("altformavail", "Reproduction");
      // fieldNames.put("dao", "Digital archival object");//@Thomas "Je ne vois pas ce champ pour l'instant"
      fieldNames.put("custodhist", "Contenu");//CHECK in contenu as well?

      // fieldNames.put("abstract", "Abstract");
      // fieldNames.put("accessrestrict", "Conditions governing access");
      // fieldNames.put("accruals", "Accruals");
      // fieldNames.put("controlaccess", "Controlled access headings");

      //================= Initialize parentFieldsBehaviors ================
      parentFieldsBehaviors.put(fieldNames.get("eadid"), ParentFieldBehavior.FILL);
      parentFieldsBehaviors.put(fieldNames.get("unitid"), ParentFieldBehavior.FILL);
      parentFieldsBehaviors.put(fieldNames.get("unittitle"), ParentFieldBehavior.FILL);
      parentFieldsBehaviors.put(fieldNames.get("scopecontent"), ParentFieldBehavior.FILL);
      parentFieldsBehaviors.put(fieldNames.get("unitdate"), ParentFieldBehavior.FILL);
      parentFieldsBehaviors.put(fieldNames.get("physfacet"), ParentFieldBehavior.FILL);
      parentFieldsBehaviors.put(fieldNames.get("extent"), ParentFieldBehavior.IGNORE);
      parentFieldsBehaviors.put(fieldNames.get("dimensions"), ParentFieldBehavior.FILL);
      parentFieldsBehaviors.put(fieldNames.get("origination"), ParentFieldBehavior.FILL);
      parentFieldsBehaviors.put(fieldNames.get("langmaterial"), ParentFieldBehavior.FILL);
      parentFieldsBehaviors.put(fieldNames.get("bibliography"), ParentFieldBehavior.FILL);
      parentFieldsBehaviors.put(fieldNames.get("altformavail"), ParentFieldBehavior.FILL);
      parentFieldsBehaviors.put(fieldNames.get("custodhist"), ParentFieldBehavior.FILL);

      parentFieldsBehaviors.put("Décor", ParentFieldBehavior.FILL);
      parentFieldsBehaviors.put("Reliure", ParentFieldBehavior.FILL);
      parentFieldsBehaviors.put("Support", ParentFieldBehavior.FILL);
      parentFieldsBehaviors.put("Histoire BNF", ParentFieldBehavior.FILL);


      parentFieldsBehaviors.put(feedbackFieldName, ParentFieldBehavior.IGNORE);
   }

   /*
    * Resets @translatedFields
    */
   public static void reset() {
      translatedFields = new ArrayList<HashMap<String, String>>();
      translatedFields.add(new HashMap<String, String>());

      currentMapIndex = 0;

      parentDescriptionPointers = new ArrayList<Integer>();
      parentDescriptionPointers.add(-1);
   }

   /*
    * @pre :   The input XML file has been successfully read
    *          and the tags tree @Parser.rootTags is initialized
    * Iterate through the tag tree and translates it to the Strings
    * that will be written in the output file
    * Each string in the ArrayList returned is a line of the file
    */
   public static ArrayList<String> translateTreeAndMakeLines(Window window) {
      if (Parser.rootTags.size() <= 0) {
         window.addLog("There seems to be nothing to translate in the XML file.",
            "Il semble qu'il n'y ait rien à traduire dans le fichier XML.",
            WARNING);
         return new ArrayList<String>();
      }

      if (translatedFields.size() == 0) {
         translatedFields.add(new HashMap<String, String>());
         parentDescriptionPointers.add(-1);
      }

      runTranslation(window);

      return generateLines(window);
   }

   /*
    * @pre :   The input XML file has been successfully read
    *          and the tags tree @Parser.rootTags is initialized
    * Run the translation on that tag, producing a HashMap @translatedFields of the fields
    * and their values in the output file.
    * @return that HashMap.
    */
   public static ArrayList<HashMap<String, String>> translateTree(Window window) {
      if (Parser.rootTags.size() <= 0) {
         window.addLog("There seems to be nothing to translate in the XML file.",
            "Il semble qu'il n'y ait rien à traduire dans le fichier XML.",
            WARNING);
         return new ArrayList<HashMap<String, String>>();
      }

      if (translatedFields.size() == 0) {
         translatedFields.add(new HashMap<String, String>());
         parentDescriptionPointers.add(-1);
      }

      runTranslation(window);

      return translatedFields;
   }

   /*
    * @pre :   The XML file has been successfully translated in @translatedFields
    * Generates an ArrayList of all the lines that will be written in the output file
    * when translating only one file at a time.
    */
   private static ArrayList<String> generateLines(Window window) {
      return generateLines(translatedFields, window);
   }

   /*
    * @allFilesFields represent the translations of all the files you wanted to translate
    * and write these translations in one single output file.
    * @return all the lines that will be written in the output file
    */
   public static ArrayList<String> generateLines(ArrayList<HashMap<String, String>> allFilesFields, Window window) {
      //============== Get the field names that are not empty ====================
      HashSet<String> fieldsNames = new HashSet<String>();
      for (HashMap<String, String> currentTranslation : allFilesFields) {
         for (String fieldName : currentTranslation.keySet()) {
            if (fieldName == null)
               continue;
            if (currentTranslation.get(fieldName) != null)
               fieldsNames.add(fieldName);
         }
      }
      if (fieldsNames.size() <= 0) {
         window.addLog("All the fields in all the XML input files seem to be empty.",
            "Tous les champs dans tous les fichiers XML d'entrée semblent être vides.",
            WARNING);
         return new ArrayList<String>();
      }

      ArrayList<String> fieldsOrder = new ArrayList<String>();
      for (String currentFieldName : fieldsNames)
         fieldsOrder.add(currentFieldName);

      //READABLE TRANSLATION TODO REMOVE
      // for (HashMap currentTranslation : allFilesFields) {
      //    for (String fieldName : fieldsOrder) {
      //       answer.add(fieldName + "\t" + currentTranslation.get(fieldName));
      //    }
      //    answer.add("\nNew entry");
      // }

      //=========== Write first line ============================
      ArrayList<String> answer = new ArrayList<String>();

      String currentLine = "";
      for (String fieldName : fieldsOrder) {
         currentLine += fieldName;
         currentLine += "\t";
      }
      currentLine = currentLine.substring(0, currentLine.length()-1);//remove last '\t'
      answer.add(currentLine);

      //========== Write next lines ============================
      String currentValue;
      for (HashMap<String, String> currentTranslation : allFilesFields) {
         currentLine = "";
         for (String currentFieldName : fieldsOrder) {
            currentValue = currentTranslation.get(currentFieldName);
            if (currentValue != null)
               currentLine += currentValue;
            currentLine += "\t";
         }
         if (currentLine.length() > 0) {
            currentLine = currentLine.substring(0, currentLine.length()-1);//Remove last '\t'
            answer.add(currentLine);
         }
      }

      return answer;
   }

   public static void runTranslation(Window window) {
      for (XMLPart currentRoot : Parser.rootTags) {
         translateTags(currentRoot, 0, window);//Translates tags recursively
      }

      fillMinorMaterial();
   }

   /*
    * Iterates recursively through the tags tree (thanks to argument @tag)
    * and put the interesting translations in the HashMap @translatedFields
    */
   private static void translateTags(XMLPart tag, int parentDescriptionIndex, Window window) {
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
               translateTags(currentTag, parentDescriptionIndex, window);
            }
            break;
         case IGNORE://nothing to do
            break;
         case FEEDBACK:
            {
               String fieldValue = tag.getContentsFormatted();
               if (!fieldValue.equals("") && !fieldValue.equals(" ") && !fieldValue.equals("\t")) {
                  fieldValue = "["+tag.getTagName()+"] "+fieldValue;
                  updateMainMaterialField(feedbackFieldName, fieldValue);
               }
            }
            break;
         case FIELD:
            if (!specialTreatement(tag, window)) {
               String fieldName = fieldNames.get(tag.getTagName());
               String fieldValue = tag.getContentsFormatted();
               if (fieldValue != null)
                  updateField(fieldName, fieldValue);
            }
            break;
         case FIELD_MAIN:
            if (!specialTreatement(tag, window)) {
               String fieldName = fieldNames.get(tag.getTagName());
               String fieldValue = tag.getContentsFormatted();
               if (fieldValue != null)
                  updateMainMaterialField(fieldName, fieldValue);
            }
            break;
         case LEVEL:
            //create a new description (is it al<ays the use of c?)
            translatedFields.add(new HashMap<String, String>());
            parentDescriptionPointers.add(parentDescriptionIndex);
            currentMapIndex++;
            //translate children
            for (XMLPart currentTag : tag.getChildrenElements()) {
               translateTags(currentTag, currentMapIndex, window);
            }
            break;
         default:
            Log.err("Couldn't translate the tag named : '"+tag.getTagName()+"'. Ignoring it and its contents.");
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
                  else if (attribute.equals("ancienne cote")) {
                     String fieldName = "Ancienne cote " + getOldCoteSystem(fieldValue);
                     updateField(fieldName, fieldValue);
                  }
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

   /*
    * Add @fieldValue for the field @fieldName of the current piece of material described in the input file
    */
   private static void updateField(String fieldName, String fieldValue) {
      if (translatedFields.size() == 0)
         translatedFields.add(new HashMap<String, String>());

      String currentStoredValue = translatedFields.get(currentMapIndex).get(fieldName);
      if (currentStoredValue != null)
         fieldValue = currentStoredValue + " / " + fieldValue;
      translatedFields.get(currentMapIndex).put(fieldName, fieldValue);
   }

   /*
    * Add @fieldValue for the field @fieldName of the main piece of material described in the input file
    */
   private static void updateMainMaterialField(String fieldName, String fieldValue) {
      if (translatedFields.size() == 0)
         translatedFields.add(new HashMap<String, String>());

      String currentStoredValue = translatedFields.get(0).get(fieldName);
      if (currentStoredValue != null)
         fieldValue = currentStoredValue + " / " + fieldValue;
      translatedFields.get(0).put(fieldName, fieldValue);
   }

   private static String getOldCoteSystem(String coteValue) {
      if (coteValue == null || coteValue.length() <= 0)
         throw new IllegalArgumentException("Tried to get the old cote system name from an empty field.");

      int i = 0;
      char c = coteValue.charAt(0);
      while (c==' ' || c=='\t') {//remove first spaces
         if (++i >= coteValue.length())
            return "";
         c = coteValue.charAt(i);
      }
      int beginningIndex = i;

      //find first actual space
      while (c!=' ' && c!='\t') {
         if (++i >= coteValue.length())
            break;
         c = coteValue.charAt(i);
      }

      return coteValue.substring(beginningIndex, i);
   }

   /*
    * Fills the minor material with the info of the main material if they haven't been replaced in the minor material description
    */
   private static void fillMinorMaterial() {
      //if one of the HashMaps in @translatedFields doesn't have a value for one of the fields that are tagged as FIELD_MAIN,
      //we place the value of the field from the main material description in that HashMap
      HashMap<String, String> mainMaterial = translatedFields.get(0);
      for (String fieldName : mainMaterial.keySet()) {
         if (parentFieldsBehaviors.get(fieldName) == ParentFieldBehavior.FILL || fieldName.startsWith("Ancienne cote")) {
            for (int i=1; i<translatedFields.size(); i++) {
               if (translatedFields.get(i).get(fieldName) == null) {
                  translatedFields.get(i).put(fieldName, translatedFields.get(parentDescriptionPointers.get(i)).get(fieldName));
               }
            }
         }
      }
   }

   //============= Not used =====================
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
