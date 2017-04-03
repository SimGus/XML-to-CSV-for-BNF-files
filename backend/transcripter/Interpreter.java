package backend.transcripter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Map;

import util.Log;
import backend.parser.Parser;
import backend.parser.XMLPart;
import backend.parser.XMLTag;
import backend.parser.XMLString;
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

   private static final HashMap<String, TagType> tagTypesMap = new HashMap<String, TagType>(148);
   private static final HashMap<String, String> fieldNames = new HashMap<String, String>(14);
   private static final HashMap<String, ParentFieldBehavior> parentFieldsBehaviors = new HashMap<String, ParentFieldBehavior>(18);
   private static ArrayList<String> oldCoteFieldNames = new ArrayList<String>(85);

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
      Log.fct(3, "Interpreter.initializeMaps");
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
      fieldNames.put("physdesc", "Description physique");
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
      parentFieldsBehaviors.put(fieldNames.get("unitdate"), ParentFieldBehavior.IGNORE);
      parentFieldsBehaviors.put(fieldNames.get("physfacet"), ParentFieldBehavior.IGNORE);
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

      //==================== Initialize oldCoteFieldNames ==========================
      oldCoteFieldNames.add("Ancien Supplément");
      oldCoteFieldNames.add("Antoine Faure");
      oldCoteFieldNames.add("Anquetil");
      oldCoteFieldNames.add("Baluze");
      oldCoteFieldNames.add("Béthune");
      oldCoteFieldNames.add("Bigot");
      oldCoteFieldNames.add("Blancs Manteaux");
      oldCoteFieldNames.add("Blochet");
      oldCoteFieldNames.add("Bouhier");
      oldCoteFieldNames.add("Bourgogne et Bar");
      oldCoteFieldNames.add("Caix");
      oldCoteFieldNames.add("Cangé");
      oldCoteFieldNames.add("Carmes de la place Maubert");
      oldCoteFieldNames.add("Carmes");
      oldCoteFieldNames.add("Capucins de Saint-Honoré");
      oldCoteFieldNames.add("Cartulaire");
      oldCoteFieldNames.add("Célestins");
      oldCoteFieldNames.add("Colbert");
      oldCoteFieldNames.add("Collot");
      oldCoteFieldNames.add("Compiègne");
      oldCoteFieldNames.add("Corbie");
      oldCoteFieldNames.add("Cordeliers");
      oldCoteFieldNames.add("Cordier médecine");
      oldCoteFieldNames.add("Cordier non médecine");
      oldCoteFieldNames.add("de la Mare");//put "Delamare" in it too
      oldCoteFieldNames.add("de Mesmes");
      oldCoteFieldNames.add("de Thou");
      oldCoteFieldNames.add("Drouin");
      oldCoteFieldNames.add("Ducaurroy");
      oldCoteFieldNames.add("Dupuy");
      oldCoteFieldNames.add("Feuillants");
      oldCoteFieldNames.add("Flandres");
      oldCoteFieldNames.add("Fourmont");
      oldCoteFieldNames.add("Gaignières");
      oldCoteFieldNames.add("Galland");
      oldCoteFieldNames.add("Gentil");
      oldCoteFieldNames.add("Gevres");//put "Gesvres" in it too
      oldCoteFieldNames.add("Grandpré");
      oldCoteFieldNames.add("Grands-Augustins");
      oldCoteFieldNames.add("Griaule");
      oldCoteFieldNames.add("Grimblot");
      oldCoteFieldNames.add("Guérin");
      oldCoteFieldNames.add("Harbonnières");
      oldCoteFieldNames.add("Harlay");
      oldCoteFieldNames.add("Hurault");
      oldCoteFieldNames.add("Jacobins de Saint-Honoré");
      oldCoteFieldNames.add("Jacobins de Saint-Jacques");
      oldCoteFieldNames.add("La Vallière");
      oldCoteFieldNames.add("Lancelot");
      oldCoteFieldNames.add("Le Tellier de Reims");
      oldCoteFieldNames.add("Maq");
      oldCoteFieldNames.add("Mazarin");
      oldCoteFieldNames.add("Minimes");
      oldCoteFieldNames.add("Missions-étrangères");
      oldCoteFieldNames.add("Mortemart");
      oldCoteFieldNames.add("Navarre");
      oldCoteFieldNames.add("Noailles");
      oldCoteFieldNames.add("Notre-Dame");
      oldCoteFieldNames.add("Oratoire");
      oldCoteFieldNames.add("Petits-Pères");
      oldCoteFieldNames.add("Rançon de René d'Anjou");
      oldCoteFieldNames.add("Rigault");
      oldCoteFieldNames.add("Résidu");
      oldCoteFieldNames.add("Regius");
      oldCoteFieldNames.add("Richelieu");
      oldCoteFieldNames.add("S. Martin");
      oldCoteFieldNames.add("Saint-Germain");//put "Résidu Saint-Germain" in it too
      oldCoteFieldNames.add("Saint-Magloire");
      oldCoteFieldNames.add("Saint-Mansuy");
      oldCoteFieldNames.add("Saint-Martial");
      oldCoteFieldNames.add("Saint-VIctor");
      oldCoteFieldNames.add("Séguier-Coislin");
      oldCoteFieldNames.add("Senart");
      oldCoteFieldNames.add("SMAF");
      oldCoteFieldNames.add("Sorbonne");
      oldCoteFieldNames.add("Supplément");//transform "suppl." in it if found
      oldCoteFieldNames.add("Supplément français");//idem
      oldCoteFieldNames.add("Supplément latin");//idem
      oldCoteFieldNames.add("Targny");
      oldCoteFieldNames.add("Toul chapitre");
      oldCoteFieldNames.add("Toul évêché");
      oldCoteFieldNames.add("Versailles");
      oldCoteFieldNames.add("Vaudemont tutelle");
      oldCoteFieldNames.add("Verdun évêques");
      oldCoteFieldNames.add("Verdun");
   }

   /*
    * Resets @translatedFields
    */
   public static void reset() {
      Log.fct(3, "Interpreter.reset");
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
   public static ArrayList<String> translateTreeAndMakeLines(Window window, SplitBehavior splitFragments) {
      Log.fct(2, "Interpreter.translateTreeAndMakeLines");
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

      runTranslation(window, splitFragments);

      return generateLines(window);
   }

   /*
    * @pre :   The input XML file has been successfully read
    *          and the tags tree @Parser.rootTags is initialized
    * Run the translation on that tags, producing a HashMap @translatedFields of the fields
    * and their values in the output file.
    * @return that HashMap.
    */
   public static ArrayList<HashMap<String, String>> translateTree(Window window, SplitBehavior splitFragments) {
      Log.fct(2, "Interpreter.translateTree");
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

      runTranslation(window, splitFragments);

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
      Log.fct(3, "Interpreter.generateLines");
      //============== Get the field names that are not empty ====================
      HashSet<String> presentFieldsNames = new HashSet<String>();//store each field name ONLY once
      for (HashMap<String, String> currentTranslation : allFilesFields) {
         for (String fieldName : currentTranslation.keySet()) {
            if (fieldName == null)
               continue;
            if (currentTranslation.get(fieldName) != null)
               presentFieldsNames.add(fieldName);
         }
      }
      if (presentFieldsNames.size() <= 0) {
         window.addLog("All the fields in all the XML input files seem to be empty.",
            "Tous les champs dans tous les fichiers XML d'entrée semblent être vides.",
            WARNING);
         return new ArrayList<String>();
      }

      ArrayList<String> fieldsOrder = new ArrayList<String>();
      ArrayList<String> lastFields = new ArrayList<String>();//put 'ancienne cote X' at the end
      boolean containsPresentCote = false;
      for (String currentFieldName : presentFieldsNames) {
         if (currentFieldName.equals(fieldNames.get("unitid"))) {//put 'cote actuelle' right before 'ancienne cote' field names
            containsPresentCote = true;
            continue;
         }
         if (currentFieldName.contains("Ancienne cote"))
            lastFields.add(currentFieldName);
         else
            fieldsOrder.add(currentFieldName);
      }
      fieldsOrder.add(fieldNames.get("unitid"));
      fieldsOrder.addAll(lastFields);

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
            if (currentValue != null) {
               //currentValue = checkForSpaces(currentValue);//not used since it isn't very useful and takes too much time
               currentLine += currentValue;
            }
            currentLine += "\t";
         }
         if (currentLine.length() > 0) {
            currentLine = currentLine.substring(0, currentLine.length()-1);//Remove last '\t'
            answer.add(currentLine);
         }
      }

      return answer;
   }

   public static void runTranslation(Window window, SplitBehavior splitFragments) {
      Log.fct(4, "Interpreter.runTranslation");
      for (XMLPart currentRoot : Parser.rootTags) {
         translateTags(currentRoot, 0, window, splitFragments);//Translates tags recursively
      }

      fillMinorMaterial();
   }

   /*
    * Iterates recursively through the tags tree (thanks to argument @tag)
    * and put the interesting translations in the HashMap @translatedFields
    */
   private static void translateTags(XMLPart tag, int currentDescriptionIndex, Window window, SplitBehavior splitFragments) {
      Log.fct(4, "Interpreter.translateTags");
      /*if (tag.getTagName() == null) {//getTagName never returns null
         Log.err("The tag tree seems to be invalid. The input file must have an invalid architecture.");
         return;
      }*/

      currentMapIndex = currentDescriptionIndex;//TODO not certain about that

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
               Log.err("The input file seems to have an invalid architecture. Tag '"+tag.getTagName()+"' is misplaced (content : '"+tag.getWritableContent()+"').");
               window.addLog("The XML file contains a tag named '"+tag.getTagName()+"' in an unexpected place.",
                  "Le fichier XML contient une balise nommée '"+tag.getTagName()+"' à un endroit inattendu.",
                  WARNING);
               invalidArchLogged = true;
            }
            break;
         case CONTAINER:
            if (tag.getTagName().equals("physdesc")) {//'physdesc' can contain both values to translate to a field and containers that translate to fields
               for (XMLPart currentTag : tag.getChildrenElements()) {
                  if (currentTag instanceof XMLString){
                     String fieldValue = tag.getWritableContent();
                     if (fieldValue != null)
                        updateField(fieldNames.get(tag.getTagName()), fieldValue);
                  }
                  else
                     translateTags(currentTag, currentDescriptionIndex, window, splitFragments);
               }
            }
            else {
               for (XMLPart currentTag : tag.getChildrenElements()) {
                  translateTags(currentTag, currentDescriptionIndex, window, splitFragments);
               }
            }
            break;
         case IGNORE://nothing to do
            break;
         case FEEDBACK:
            {
               String fieldValue = tag.getWritableContent();
               if (!fieldValue.equals("") && !fieldValue.equals(" ") && !fieldValue.equals("\t")) {
                  fieldValue = "["+tag.getTagName()+"] "+fieldValue;
                  updateMainMaterialField(feedbackFieldName, fieldValue);
               }
            }
            break;
         case FIELD:
            if (!specialTreatement(tag, window)) {
               String fieldName = fieldNames.get(tag.getTagName());
               String fieldValue = tag.getWritableContent();
               if (fieldValue != null)
                  updateField(fieldName, fieldValue);
            }
            break;
         case FIELD_MAIN:
            if (!specialTreatement(tag, window)) {
               String fieldName = fieldNames.get(tag.getTagName());
               String fieldValue = tag.getWritableContent();
               if (fieldValue != null)
                  updateMainMaterialField(fieldName, fieldValue);
            }
            break;
         case LEVEL:
            if (splitFragments == SplitBehavior.SPLIT_ALL) {
               //----------- One entry for each fragment ------------------------------
               //find <unitid type="foliotation"> or "division"?
               //yes keep it in the same description
               //no keep executing this statement
               if (hasFoliotationTypedChildren(tag)) {//consider it as CONTAINER
                  // Log.log("found foliotation");
                  for (XMLPart currentTag : tag.getChildrenElements())
                     translateTags(currentTag, currentDescriptionIndex, window, splitFragments);
               }
               else {
                  //create a new description : new entry (is it always the use of c?)
                  translatedFields.add(new HashMap<String, String>());
                  parentDescriptionPointers.add(currentDescriptionIndex);
                  currentMapIndex = translatedFields.size()-1;
                  //translate children
                  for (XMLPart currentTag : tag.getChildrenElements())
                     translateTags(currentTag, currentMapIndex, window, splitFragments);
               }
            }
            else if (splitFragments == SplitBehavior.MERGE) {//same behavior as CONTAINER
               //-------------- One single entry for each XML file -----------------------
               if (tag.getTagName().equals("physdesc")) {//'physdesc' can contain both values to translate to a field and containers that translate to fields
                  for (XMLPart currentTag : tag.getChildrenElements()) {
                     if (currentTag instanceof XMLString){
                        String fieldValue = tag.getWritableContent();
                        if (fieldValue != null)
                           updateField(fieldNames.get(tag.getTagName()), fieldValue);
                     }
                     else
                        translateTags(currentTag, currentDescriptionIndex, window, splitFragments);
                  }
               }
               for (XMLPart currentTag : tag.getChildrenElements())
                  translateTags(currentTag, currentDescriptionIndex, window, splitFragments);
            }
            else {
               //--------- One entry for each fragment if the current XML file provides a date for at least one fragment ---------
               if (hasFoliotationTypedChildren(tag)) {
                  // Log.log("found foliotation");
                  for (XMLPart currentTag : tag.getChildrenElements())
                     translateTags(currentTag, currentDescriptionIndex, window, splitFragments);
               }
               else {
                  //make a new entry only if you find "unitdate" in the current fragment
                  //else write it in the parent entry
                  if (hasDateSpecifiedInChildren(tag)) {
                     //make a new entry
                     translatedFields.add(new HashMap<String, String>());
                     parentDescriptionPointers.add(currentDescriptionIndex);
                     currentMapIndex = translatedFields.size()-1;
                     //translate children
                     for (XMLPart currentTag : tag.getChildrenElements())
                        translateTags(currentTag, currentMapIndex, window, splitFragments);
                  }
                  else {//not even one of the children has a date in it
                     //stay on the same level
                     for (XMLPart currentTag : tag.getChildrenElements())
                        translateTags(currentTag, currentDescriptionIndex, window, splitFragments);
                  }
               }
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
      Log.fct(5, "Interpreter.specialTreatement");
      if (currentTag.getTagName().equals("string"))
         return false;

      XMLTag tag = (XMLTag) currentTag;
      switch (tag.getTagName()) {
         case "unitid":
            {
               String attribute = tag.getAttribute("type");
               if (attribute == null)
                  return false;//will get in @translatedFields the regular way

               String fieldValue = tag.getWritableContent();
               if (fieldValue != null) {
                  if (attribute.equals("cote"))
                     updateField("Cote actuelle", fieldValue);
                  else if (attribute.equals("ancienne cote")) {
                     String oldCoteSystem = getOldCoteSystem(fieldValue);
                     String fieldName;
                     if (oldCoteSystem == null)
                        fieldName = "Ancienne cote";
                     else
                        fieldName = "Ancienne cote " + oldCoteSystem;
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

               String fieldValue = tag.getWritableContent();
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
         //       String fieldValue = tag.getWritableContent();
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
            updateField(fieldNames.get("origination"), tag.getWritableContent());
            return true;
      }
      return false;
   }

   /*
    * Add @fieldValue for the field @fieldName of the current piece of material described in the input file
    */
   private static void updateField(String fieldName, String fieldValue) {
      Log.fct(5, "Interpreter.updateField");
      if (isEmpty(fieldValue))
         return;

      if (translatedFields.size() == 0)
         translatedFields.add(new HashMap<String, String>());

      String currentStoredValue = translatedFields.get(currentMapIndex).get(fieldName);
      if (currentStoredValue != null)
         fieldValue = currentStoredValue + " %% " + fieldValue;
      translatedFields.get(currentMapIndex).put(fieldName, fieldValue);
   }

   private static boolean isEmpty(String str) {
      if (str == null || str.length() <= 0)
         return true;

      //remove first spaces
      int i = 0;
      char c = str.charAt(0);
      while (c==' ' || c=='\t') {
         if (++i >= str.length())
            return true;
         c = str.charAt(i);
      }
      if (str.substring(i).length() <= 0)
         return true;//normally never reached

      return false;
   }

   /*
    * Add @fieldValue for the field @fieldName of the main piece of material described in the input file
    */
   private static void updateMainMaterialField(String fieldName, String fieldValue) {
      Log.fct(5, "Interpreter.updateMainMaterialField");
      if (translatedFields.size() == 0)
         translatedFields.add(new HashMap<String, String>());

      String currentStoredValue = translatedFields.get(0).get(fieldName);
      if (currentStoredValue != null)
         fieldValue = currentStoredValue + " %% " + fieldValue;
      translatedFields.get(0).put(fieldName, fieldValue);
   }

   private static String getOldCoteSystem(String coteValue) {
      Log.fct(6, "Interpreter.getOldCoteSystem");
      if (coteValue == null || coteValue.length() <= 0) {
         Log.err("Tried to get the old cote system name from an empty field.");
         return null;
      }

      //remove first spaces
      int i = 0;
      char c = coteValue.charAt(0);
      while (c==' ' || c=='\t') {
         if (++i >= coteValue.length())
            return null;
         c = coteValue.charAt(i);
      }
      coteValue = coteValue.substring(i);
      //
      // //find first actual space
      // while (c!=' ' && c!='\t') {
      //    if (++i >= coteValue.length())
      //       break;
      //    c = coteValue.charAt(i);
      // }
      //
      // String firstWord = coteValue.substring(beginningIndex, i);
      // if (isNumerical(firstWord))
      //    return null;
      //
      // return firstWord;

      String lowercaseValue = coteValue.toLowerCase();
      String currentFieldNameLowercase;
      for (String currentFieldName : oldCoteFieldNames) {
         currentFieldNameLowercase = currentFieldName.toLowerCase();
         if (lowercaseValue.startsWith(currentFieldNameLowercase))
            return currentFieldName;
      }
      //check for specific names
      if (lowercaseValue.startsWith("delamare"))
         return "de la Mare";
      if (lowercaseValue.startsWith("gesvres"))
         return "Gevres";
      if (lowercaseValue.startsWith("résidu saint-germain"))
         return "Saint-Germain";
      if (lowercaseValue.startsWith("suppl.")) {
         if (lowercaseValue.startsWith("suppl. francais") || lowercaseValue.startsWith("suppl. français"))
            return "Supplément français";
         if (lowercaseValue.startsWith("suppl. latin"))
            return "Supplément latin";
         return "Supplément";
      }

      return null;
   }

   /*
    * Fills the minor material with the info of the main material if they haven't been replaced in the minor material description
    */
   private static void fillMinorMaterial() {
      Log.fct(6, "Interpreter.fillMinorMaterial");
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

   /*
    * Calls @tagContainsFoliotation to check if at least one child in the same level as @tag is a 'unitid' tag with type 'foliotation'
    */
   private static boolean hasFoliotationTypedChildren(XMLPart tag) {
      if (tag == null || tag instanceof XMLString)
         return false;

      for (XMLPart currentChild : tag.getChildrenElements()) {
         if (tagContainsFoliotation(currentChild))
            return true;
      }
      return false;
   }

   /*
    * Checks if the tag @part contains a tag 'unitid' with type 'foliotation' at the same level
    * If we find a 'unitid' tag with type 'foliotation', we shouldn't make a new material description but rather stay in the current one
    */
   private static boolean tagContainsFoliotation(XMLPart part) {
      Log.fct(4, "Interpreter.tagContainsFoliotation");

      if (part == null || part instanceof XMLString)
         return false;

      XMLTag tag = (XMLTag) part;

      if (tagTypesMap.get(tag.getTagName()) == TagType.LEVEL)//new level
         return false;

      if (tag.getTagName().equals("unitid")) {
         String typeAttribute = tag.getAttribute("type");
         if (typeAttribute != null && (typeAttribute.equals("foliotation") || typeAttribute.equals("division")))
            return true;
         return false;
      }

      for (XMLPart currentTag : tag.getChildrenElements()) {
         if (tagContainsFoliotation(currentTag))
            return true;
      }

      return false;
   }

   /*
    * Calls @tagContainsDate and returns true if one of the children tags of @tag contains a field 'unitdate'
    */
   private static boolean hasDateSpecifiedInChildren(XMLPart tag) {
      if (tag == null || tag instanceof XMLString)
         return false;

      for (XMLPart currentChild : tag.getChildrenElements()) {
         if (tagContainsDate(currentChild))
            return true;
      }
      return false;
   }

   /*
    * Checks if the tag @part contains a tag 'unitdate' at the same level
    */
   private static boolean tagContainsDate(XMLPart part) {
      Log.fct(4, "Interpreter.tagContainsDate");

      if (part == null || part instanceof XMLString)
         return false;

      XMLTag tag = (XMLTag) part;

      if (tagTypesMap.get(tag.getTagName()) == TagType.LEVEL)//new level
         return false;

      if (tag.getTagName().equals("unitdate"))
         return true;

      for (XMLPart currentTag : tag.getChildrenElements()) {
         if (tagContainsDate(currentTag))
            return true;
      }

      return false;
   }

   /*
    * Returns @true if @str only contains numbers
    */
   private static boolean isNumerical(String str) {
      return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
   }

   /*
    * Checks if there are spaces around "%%" in @str and adds them if there aren't
    * If you can find "siècle" in @str and there is no spacxe in front of it, adds it
    * Not used since I am the only one to add "%%" and I actually add " %% "
    * Plus, it takes too much time to check for "siècle"
    */
   private static String checkForSpaces(String str) {
      if (str == null || str.length() <= 0)
         return "";

      //----------- Check for spaces around "%%" ---------------
      String tmp = "";
      char c, tmpC;
      boolean nextShouldBeASpace = false;
      int i = 0;
      while (i < str.length()-1) {
         c = str.charAt(i);
         if (nextShouldBeASpace && c != ' ' && c != '\t')
            tmp += " ";
         if (c != '%') {
            tmp += c;
            nextShouldBeASpace = false;
         }
         else {
            if (str.charAt(i+1) == '%') {
               if (tmp.length() <= 0) {
                  tmp += "%%";
                  i++;
                  nextShouldBeASpace = true;
               }
               else {
                  tmpC = tmp.charAt(tmp.length()-1);
                  if (tmpC != ' ' && tmpC != '\t')
                     tmp += " ";
                  tmp += "%%";
                  i++;
                  nextShouldBeASpace = true;
               }
            }
            else {
               tmp += c;
               nextShouldBeASpace = false;
            }
         }
         i++;
      }
      tmp += str.charAt(i);

      str = new String(tmp);
      tmp = new String();

      //----------- Check for spaces before "siècle" -------------
      i = 0;
      while (i < str.length()-5) {
         c = str.charAt(i);
         if ((c=='s' || c=='S') && (str.charAt(i+1)=='i'||str.charAt(i+1)=='I') && (str.charAt(i+2)=='è'||str.charAt(i+2)=='È'||str.charAt(i+2)=='e'||str.charAt(i+2)=='E') && (str.charAt(i+3)=='c'||str.charAt(i+3)=='C') && (str.charAt(i+4)=='l'||str.charAt(i+4)=='L') && (str.charAt(i+5)=='e'||str.charAt(i+5)=='E')) {
            if (tmp.length() > 0) {
               tmpC = tmp.charAt(tmp.length()-1);
               if (tmpC != ' ' && tmpC != '\t')
                  tmp += " ";
            }
         }
         tmp += c;
         i++;
      }
      while (i < str.length()) {
         tmp += str.charAt(i);
         i++;
      }

      return tmp;
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
