package gui;

import java.util.ArrayList;
import java.io.File;
import java.util.HashMap;

import javax.swing.UIManager;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JTabbedPane;
import javax.swing.Box;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;

import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyleConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import util.Log;
import util.EnFrString;
import util.LogType;
//import static util.LogType.*;
import backend.files.FileNamesInterpreter;
import backend.files.FileOpener;
import backend.parser.Parser;
import backend.transcripter.Interpreter;
import backend.transcripter.Translator;
import backend.transcripter.SplitBehavior;

public class Window extends JFrame {
   protected static final int defaultWidth = 640, defaultHeight = 480;
   protected static final int internalBorderSize = 8, externalBorderSize = 10, externalDescBorderSize = 30,
                              elementsSpacingSize = 10,
                              editTextPadding = 3, editTextMinWidth = 80;
   protected static final EnFrString[] tabTitles = {
      new EnFrString("Translate", "Traduire"),
      new EnFrString("Options", "Options"),
      new EnFrString("About", "À propos")
   };
   protected static final EnFrString[] errorLogsOpenings = {
      new EnFrString("[ERROR]", "[ERREUR]"),
      new EnFrString("[WARNING]", "[ATTENTION]")
   };

   protected EnFrString title;
   protected JTabbedPane tabs = new JTabbedPane();

   protected boolean dirTranslationEnabled = true, singleFileOutput = true;
   protected SplitBehavior splitFragmentsBehavior = SplitBehavior.SPLIT_DATE;
   protected boolean languagesHaveBeenSetup = false;//changed to true when the language drop down menu has been set up to avoid setting it twice because of the ActionListener

   protected Translator backgroundThread = null;//extends Thread

   //============  main tab elements ===================
   protected JPanel mainTab = new JPanel();

   protected JLabel inputFileLabel = new JLabel();
   protected JLabel outputFileLabel = new JLabel();

   protected static final EnFrString inputFileLabelString =
      new EnFrString("Name of the XML file to translate :", "Nom du fichier XML à traduire :");
   protected static final EnFrString outputFileLabelString =
      new EnFrString("Name of the file to create :", "Nom du fichier à créer :");

   protected JButton okButton = new JButton();
   protected JButton browseButton = new JButton();
   protected EditText inputFileField = new EditText("file.xml");
   protected EditText outputFileField = new EditText("file.txt");

   protected static final EnFrString okButtonLabel = new EnFrString("RUN TRANSCRIPTION", "LANCER LA TRANSCRIPTION");
   protected static final EnFrString browseButtonLabel = new EnFrString("Browse", "Parcourir");

   protected static final int okButtonMargin = 10;

   protected Box logArea = Box.createHorizontalBox();
   protected JTextPane logTextPane = new JTextPane();
   protected JScrollPane logAreaScrollPane = new JScrollPane(logTextPane);

   protected static final EnFrString readyLogMsg = new EnFrString("Ready to translate XML file.", "Prêt à traduire un fichier XML.");
   protected GUILogs logs = new GUILogs(this, readyLogMsg, LogType.NORMAL);

   protected static final EnFrString logAreaTitle = new EnFrString("Output", "Sortie");

   protected JButton clearLogsButton = new JButton();
   protected static final EnFrString clearLogsButtonLabel = new EnFrString("Clear output", "Vider la zone de messages de sortie");

   //============ options tab elements ===============
   protected JLabel outputFormatLabel = new JLabel();
   protected JLabel languageChoiceLabel = new JLabel();
   protected static final EnFrString outputFormatLabelString = new EnFrString("Output file format :", "Format du fichier de sortie :");
   protected static final EnFrString languageChoiceLabelString = new EnFrString("Language :", "Langue :");
   protected EnFrString splitFragmentsDropDownMenuLabelString = new EnFrString("Fragments management :", "Gestion des fragments :");

   protected JComboBox outputFormatDropDownMenu = new JComboBox();
   protected JComboBox languageChoiceDropDownMenu = new JComboBox();
   protected static final EnFrString[] outputFormatsAvailable = {
      new EnFrString("TXT file (.txt)", "Fichier TXT (.txt)"),
      new EnFrString("TAB file (.tab)", "Fichier TAB (.tab)"),
   };
   protected static final EnFrString[] languagesAvailable = {
      new EnFrString("English", "Anglais"),
      new EnFrString("French", "Français")
   };

   protected JLabel enableDirCheckBoxLabel = new JLabel();
   protected JCheckBox enableDirCheckBox = new JCheckBox();
   protected static EnFrString enableDirCheckBoxString = new EnFrString(
      "Enable the translation of all files in the specified directory.",
      "Autoriser la traduction de tous les fichier se trouvant dans le dossier spécifié."
   );

   protected JLabel singleFileOutputCheckBoxLabel = new JLabel();
   protected JCheckBox singleFileOutputCheckBox = new JCheckBox();
   protected static final EnFrString singleFileOutputCheckBoxString = new EnFrString(
      "Generate one output file for each XML file translated.",
      "Générer un fichier de sortie par fichier XML traduit."
   );

   protected JLabel splitFragmentsDropDownMenuLabel = new JLabel();
   protected JComboBox splitFragmentsDropDownMenu = new JComboBox();
   protected static final EnFrString[] splitFragmentsDropDownMenuStrings = {
      new EnFrString(
         "One entry for each fragment",
         "Une entrée par fragment"
      ),
      new EnFrString(//default
         "One entry for each fragment with a date specified",
         "Une entrée par fragment avec une date spécifiée"
      ),
      new EnFrString(
         "One entry for each XML file",
         "Une entrée par fichier XML"
      )
   };

   //============ about tab elements =================
   protected JTextPane descriptionPane = new JTextPane();//TODO change to JLabel?
   protected static final EnFrString description = new EnFrString(
      "This program is meant to translate XML files that describe archival materials,"
      +" into TAB or TXT files importable easily into databases.",
      "Ce programme permet de traduire des fichiers XML qui décrivent de la documentation archivistique,"
      +" en fichiers TAB ou TXT facilement importables dans des bases de données."
   );
   protected static final EnFrString usageTitle = new EnFrString("Usage", "Utilisation");
   protected static final EnFrString usage = new EnFrString(
      "Specify the path to the XML file you want to translate (you can do so using the 'Browse' button);\n\n"
      +"Optionaly specify the name of the TAB or TXT file;\n\n"
      +"Click the button 'Run transcription';\n\n"
      +"A TAB or TXT file containing the translation of the contents of the XML file will be created.",
      "Spécifiez le chemin vers le fichier XML (par exemple en utilisant le bouton 'Parcourir');\n\n"
      +"Éventuellement, spécifiez le nom du fichier TAB ou TXT à créer;\n\n"
      +"Cliquez sur 'Lancer la transcription';\n\n"
      +"Un fichier TAB ou TXT contenant la traduction du contenu du fichier XML sera créé."
   );
   protected static final EnFrString precision = new EnFrString(
      "You can set the format of the output file (TAB or TXT) in the option tab.\n"
      +"If the option 'translate directories' is enabled, the program will translate all the XML files in the specified directory.\n",
      "Vous pouvez régler le format du fichier de sortie (TAB or TXT) dans l'onglet 'Options'.\n"
      +"Si l'option 'traduire les dossiers' est activée, le programme traduira tous les fichiers XML dans le dossier spécifié."
   );
   protected static final EnFrString credits = new EnFrString(
      "\u00A9 2017 S. Gustin",
      "\u00A9 2017 S. Gustin"
   );

   public Window() {
      this(new EnFrString("XML to TAB transcriptor", "Transcripteur XML vers TAB"));
   }

   public Window(String enTitle, String frTitle) {
      this(new EnFrString(enTitle, frTitle));
   }

   public Window(EnFrString title) {
      Log.fct(0, "Window.constructor");
      this.title = title;
      this.setSize(defaultWidth, defaultHeight);
      this.setLocationRelativeTo(null);//center window
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      //---------- Set program to system theme --------------------
      //*
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
         Log.err("Couldn't get the system's window theme.");
      }//*/

      //--------- Initialize styles --------------
      initLogsStyles(logTextPane.getStyledDocument());
      initDescStyles(descriptionPane.getStyledDocument());

      //-------- Make window ------------
      descriptionPane.setEditable(false);
      logTextPane.setEditable(false);
      okButton.setBackground(new Color(0x0277BD));
      okButton.setForeground(Color.WHITE);
      //default values (check boxes)
      enableDirCheckBox.setSelected(dirTranslationEnabled);
      singleFileOutputCheckBox.setSelected(!singleFileOutput);

      placeElements();
      setLabels();

      //default values (drop down menus)
      try {
         splitFragmentsDropDownMenu.setSelectedIndex(1);
      } catch (IllegalArgumentException e) {
         Log.err("Tried to set the default value of the drop down menu for fragments management to a too big value (2)");
      }

      Thread logsUpdater = new Thread(logs);
      logsUpdater.start();

      //-------- Add listeners ------------
      okButton.addActionListener(new ButtonListener());
      browseButton.addActionListener(new ButtonListener());
      clearLogsButton.addActionListener(new ButtonListener());

      outputFormatDropDownMenu.addActionListener(new DropDownMenuListener());
      languageChoiceDropDownMenu.addActionListener(new DropDownMenuListener());

      enableDirCheckBox.addActionListener(new CheckBoxListener());
      singleFileOutputCheckBox.addActionListener(new CheckBoxListener());

      splitFragmentsDropDownMenu.addActionListener(new DropDownMenuListener());

      this.setVisible(true);
   }

   public void placeElements() {
      Log.fct(3, "Window.placeElements");
      this.setTitle(title.toString());
      EmptyBorder linesBorder = new EmptyBorder(internalBorderSize, internalBorderSize, internalBorderSize, internalBorderSize);

      //=========== make the contents of the main tab ============
      inputFileLabel.setLabelFor(inputFileField);
      outputFileLabel.setLabelFor(outputFileField);

      Box line1 = Box.createHorizontalBox();
      line1.add(inputFileLabel);
      line1.add(Box.createHorizontalGlue());
      line1.add(Box.createHorizontalStrut(elementsSpacingSize));
      line1.add(inputFileField);
      line1.add(Box.createHorizontalStrut(elementsSpacingSize));
      line1.add(browseButton);
      setElementBorder(line1, linesBorder);
      //sizes
      int inputFileFieldHeight = inputFileField.getPreferredSize().height+editTextPadding;
      inputFileField.setMaximumSize(new Dimension(Integer.MAX_VALUE, inputFileFieldHeight));
      inputFileField.setMinimumSize(new Dimension(editTextMinWidth, 0));
      line1.setMaximumSize(new Dimension(Integer.MAX_VALUE, inputFileFieldHeight+2*internalBorderSize));

      Box line2 = Box.createHorizontalBox();
      line2.add(outputFileLabel);
      line2.add(Box.createHorizontalGlue());
      line2.add(Box.createHorizontalStrut(elementsSpacingSize));
      line2.add(outputFileField);
      setElementBorder(line2, linesBorder);
      //sizes
      int outputFileFieldHeight = outputFileField.getPreferredSize().height+editTextPadding;
      outputFileField.setMaximumSize(new Dimension(Integer.MAX_VALUE, outputFileFieldHeight));
      outputFileField.setMinimumSize(new Dimension(editTextMinWidth, 0));
      line2.setMaximumSize(new Dimension(Integer.MAX_VALUE, outputFileFieldHeight+2*internalBorderSize));

      Box line3 = Box.createHorizontalBox();
      line3.add(Box.createHorizontalGlue());
      line3.add(okButton);
      setElementBorder(line3, linesBorder);
      //sizes
      line3.setMaximumSize(new Dimension(Integer.MAX_VALUE, okButton.getPreferredSize().height+2*internalBorderSize));

      //logArea needs to have its title changed, hence it is a class attribute
      logArea.add(logAreaScrollPane);

      Box line4 = Box.createHorizontalBox();
      line4.add(Box.createHorizontalGlue());
      line4.add(clearLogsButton);
      setElementBorder(line4, linesBorder);
      //sizes
      line4.setMaximumSize(new Dimension(Integer.MAX_VALUE, clearLogsButton.getPreferredSize().height+2*internalBorderSize));

      Box mainTab = Box.createVerticalBox();
      mainTab.add(line1);
      mainTab.add(line2);
      mainTab.add(line3);
      mainTab.add(logArea);
      mainTab.add(line4);
      mainTab.setFocusable(true);
      mainTab.setBorder(new EmptyBorder(externalBorderSize, externalBorderSize, externalBorderSize, externalBorderSize));

      //========== make the contents of the option tab =================
      Box outputFormatLine = Box.createHorizontalBox();
      outputFormatLine.add(outputFormatLabel);
      outputFormatLine.add(Box.createHorizontalStrut(elementsSpacingSize));
      outputFormatLine.add(outputFormatDropDownMenu);
      //sizes
      setElementBorder(outputFormatLine, linesBorder);
      int formatDropDownMenuHeight = outputFormatDropDownMenu.getPreferredSize().height;
      outputFormatDropDownMenu.setMaximumSize(new Dimension(Integer.MAX_VALUE, formatDropDownMenuHeight));
      outputFormatLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, formatDropDownMenuHeight+2*internalBorderSize));

      Box languageLine = Box.createHorizontalBox();
      languageLine.add(languageChoiceLabel);
      languageLine.add(Box.createHorizontalStrut(elementsSpacingSize));
      languageLine.add(languageChoiceDropDownMenu);
      //sizes
      int languagesDropDownMenuHeight = languageChoiceDropDownMenu.getPreferredSize().height;
      setElementBorder(languageLine, linesBorder);
      languageChoiceDropDownMenu.setMaximumSize(new Dimension(Integer.MAX_VALUE, languagesDropDownMenuHeight));
      languageLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, languagesDropDownMenuHeight+2*internalBorderSize));

      Box dircheckBoxLine = Box.createHorizontalBox();
      dircheckBoxLine.add(enableDirCheckBox);
      dircheckBoxLine.add(Box.createHorizontalStrut(elementsSpacingSize));
      dircheckBoxLine.add(enableDirCheckBoxLabel);
      dircheckBoxLine.add(Box.createHorizontalGlue());
      //sizes
      setElementBorder(dircheckBoxLine, linesBorder);
      dircheckBoxLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, enableDirCheckBoxLabel.getPreferredSize().height+2*internalBorderSize));

      Box singleFileCheckBoxLine = Box.createHorizontalBox();
      singleFileCheckBoxLine.add(Box.createHorizontalStrut(2*elementsSpacingSize));
      singleFileCheckBoxLine.add(singleFileOutputCheckBox);
      singleFileCheckBoxLine.add(Box.createHorizontalStrut(elementsSpacingSize));
      singleFileCheckBoxLine.add(singleFileOutputCheckBoxLabel);
      singleFileCheckBoxLine.add(Box.createHorizontalGlue());
      singleFileOutputCheckBox.setEnabled(enableDirCheckBox.isSelected());
      //sizes
      setElementBorder(singleFileCheckBoxLine, linesBorder);
      singleFileCheckBoxLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, singleFileOutputCheckBoxLabel.getPreferredSize().height+2*internalBorderSize));

      Box splitFragmentsDropDownMenuLine = Box.createHorizontalBox();
      splitFragmentsDropDownMenuLine.add(splitFragmentsDropDownMenuLabel);
      splitFragmentsDropDownMenuLine.add(Box.createHorizontalStrut(elementsSpacingSize));
      splitFragmentsDropDownMenuLine.add(splitFragmentsDropDownMenu);
      splitFragmentsDropDownMenuLine.add(Box.createHorizontalGlue());
      //sizes
      setElementBorder(splitFragmentsDropDownMenuLine, linesBorder);
      splitFragmentsDropDownMenuLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, splitFragmentsDropDownMenuLabel.getPreferredSize().height+2*internalBorderSize));

      Box optionsTab = Box.createVerticalBox();
      optionsTab.add(outputFormatLine);
      optionsTab.add(languageLine);
      optionsTab.add(dircheckBoxLine);
      optionsTab.add(singleFileCheckBoxLine);
      optionsTab.add(splitFragmentsDropDownMenuLine);
      optionsTab.add(Box.createVerticalGlue());
      optionsTab.setBorder(new EmptyBorder(externalBorderSize, externalBorderSize, externalBorderSize, externalBorderSize));

      //========== make the contents of the about tab ==================
      Box descriptionBox = Box.createVerticalBox();
      descriptionBox.add(descriptionPane);
      descriptionBox.setFocusable(true);
      descriptionBox.setBorder(new EmptyBorder(externalDescBorderSize, externalDescBorderSize, externalDescBorderSize, externalDescBorderSize));

      //============ put the tabs in the panel =======================
      tabs.add(tabTitles[0].toString(), mainTab);
      tabs.add(tabTitles[1].toString(), optionsTab);
      tabs.add(tabTitles[2].toString(), descriptionBox);

      this.getContentPane().add(tabs);
   }

   /*
    * Sets the texts of all the elements that display something
    */
   public void setLabels() {
      Log.fct(3, "Window.setLabels");
      //=========== tabs titles =============
      tabs.setTitleAt(0, tabTitles[0].toString());
      tabs.setTitleAt(1, tabTitles[1].toString());
      tabs.setTitleAt(2, tabTitles[2].toString());

      //=========== main tab ==============
      inputFileLabel.setText(inputFileLabelString.toString());
      outputFileLabel.setText(outputFileLabelString.toString());

      browseButton.setText(browseButtonLabel.toString());
      okButton.setText(okButtonLabel.toString());

      logArea.setBorder(BorderFactory.createTitledBorder(logAreaTitle.toString()));

      clearLogsButton.setText(clearLogsButtonLabel.toString());

      updateLogs();

      //=========== options tab =============
      outputFormatLabel.setText(outputFormatLabelString.toString());
      languageChoiceLabel.setText(languageChoiceLabelString.toString());

      enableDirCheckBoxLabel.setText(enableDirCheckBoxString.toString());
      singleFileOutputCheckBoxLabel.setText(singleFileOutputCheckBoxString.toString());

      splitFragmentsDropDownMenuLabel.setText(splitFragmentsDropDownMenuLabelString.toString());

      setDropDownMenusItems();

      //=========== about tab ==============
      writeDesc();
   }

   protected void writeDesc() {
      Log.fct(4, "Window.writeDesc");
      descriptionPane.setText("");
      StyledDocument doc = descriptionPane.getStyledDocument();
      try {
         doc.insertString(doc.getLength(), description.toString()+"\n\n\n", doc.getStyle("normal"));
         doc.insertString(doc.getLength(), usageTitle.toString()+"\n\n", doc.getStyle("title"));
         doc.insertString(doc.getLength(), usage.toString()+"\n\n", doc.getStyle("normal"));
         doc.insertString(doc.getLength(), precision.toString()+"\n\n\n\n", doc.getStyle("italic"));
         doc.insertString(doc.getLength(), credits.toString()+"\n", doc.getStyle("legal"));
      } catch (BadLocationException e) {
         Log.err("Couldn't insert text in the description text pane.");
      }
   }

   /*
    * Define font styles for the log area
    */
   protected void initLogsStyles(StyledDocument doc) {
      Log.fct(4, "Window.initLogsStyles");
      Style defaultStyle = StyleContext
                              .getDefaultStyleContext()
                              .getStyle(StyleContext.DEFAULT_STYLE);

      Style normal = doc.addStyle("normal", defaultStyle);
      StyleConstants.setFontFamily(defaultStyle, "SansSerif");

      Style currentStyle = doc.addStyle("minor", normal);
      StyleConstants.setItalic(currentStyle, true);
      StyleConstants.setFontSize(currentStyle, 11);

      currentStyle = doc.addStyle("bold", normal);
      StyleConstants.setBold(currentStyle, true);

      currentStyle = doc.addStyle("big", normal);
      StyleConstants.setFontSize(currentStyle, 15);
      StyleConstants.setBold(currentStyle, true);
   }

   /*
    * Define font styles for the description area
    */
   protected void initDescStyles(StyledDocument doc) {
      Log.fct(4, "Window.initDescStyles");
      Style defaultStyle = StyleContext
                              .getDefaultStyleContext()
                              .getStyle(StyleContext.DEFAULT_STYLE);

      Style normal = doc.addStyle("normal", defaultStyle);
      StyleConstants.setFontFamily(defaultStyle, "SansSerif");

      Style currentStyle = doc.addStyle("title", normal);
      StyleConstants.setBold(currentStyle, true);
      StyleConstants.setFontSize(currentStyle, 16);

      currentStyle = doc.addStyle("italic", normal);
      StyleConstants.setItalic(currentStyle, true);

      currentStyle = doc.addStyle("legal", normal);
      StyleConstants.setFontSize(currentStyle, 10);
   }

   protected void setDropDownMenusItems() {
      Log.fct(4, "Window.setDropDownMenusItems");
      int indexToSelect = -1;

      //----------- Set the output format menu -------------------------------
      String selectedItem = (String) outputFormatDropDownMenu.getSelectedItem();
      if (selectedItem == null)
         indexToSelect = -1;//nothing to do
      else if (selectedItem.equals(outputFormatsAvailable[0].getStrings("English")) || selectedItem.equals(outputFormatsAvailable[0].getStrings("French")))
         indexToSelect = 0;
      else if (selectedItem.equals(outputFormatsAvailable[1].getStrings("English")) || selectedItem.equals(outputFormatsAvailable[1].getStrings("French")))
         indexToSelect = 1;
      else if (selectedItem.equals(outputFormatsAvailable[2].getStrings("English")) || selectedItem.equals(outputFormatsAvailable[2].getStrings("French")))
         indexToSelect = 2;

      outputFormatDropDownMenu.removeAllItems();
      for (EnFrString currentItem : outputFormatsAvailable)
         outputFormatDropDownMenu.addItem(currentItem.toString());
      if (indexToSelect >= 0)
         outputFormatDropDownMenu.setSelectedIndex(indexToSelect);

      //-------------- Set the language menu ---------------------------------
      selectedItem = (String) languageChoiceDropDownMenu.getSelectedItem();
      if (selectedItem == null)
         indexToSelect = -1;//nothing to do
      else if (selectedItem.equals(languagesAvailable[0].getStrings("English")) || selectedItem.equals(languagesAvailable[0].getStrings("French")))
         indexToSelect = 0;
      else if (selectedItem.equals(languagesAvailable[1].getStrings("English")) || selectedItem.equals(languagesAvailable[1].getStrings("French")))
         indexToSelect = 1;

      languageChoiceDropDownMenu.removeAllItems();
      for (EnFrString currentItem : languagesAvailable)
         languageChoiceDropDownMenu.addItem(currentItem.toString());
      if (indexToSelect >= 0)
         languageChoiceDropDownMenu.setSelectedIndex(indexToSelect);

      languagesHaveBeenSetup = true;

      //-------------- Set the fragment management menu -----------------------
      selectedItem = (String) splitFragmentsDropDownMenu.getSelectedItem();
      if (selectedItem == null)
         indexToSelect = -1;//nothing to do
      else if (selectedItem.equals(splitFragmentsDropDownMenuStrings[0].getStrings("English")) || selectedItem.equals(splitFragmentsDropDownMenuStrings[0].getStrings("French")))
         indexToSelect = 0;
      else if (selectedItem.equals(splitFragmentsDropDownMenuStrings[1].getStrings("English")) || selectedItem.equals(splitFragmentsDropDownMenuStrings[1].getStrings("French")))
         indexToSelect = 1;
      else if (selectedItem.equals(splitFragmentsDropDownMenuStrings[2].getStrings("English")) || selectedItem.equals(splitFragmentsDropDownMenuStrings[2].getStrings("French")))
         indexToSelect = 2;

      splitFragmentsDropDownMenu.removeAllItems();
      for (EnFrString currentItem : splitFragmentsDropDownMenuStrings)
         splitFragmentsDropDownMenu.addItem(currentItem.toString());
      if (indexToSelect >= 0)
         splitFragmentsDropDownMenu.setSelectedIndex(indexToSelect);
   }

   private void setElementBorder(JComponent element, EmptyBorder border) {
      Log.fct(5, "Window.setElementBorder");
      Border currentBorder = element.getBorder();
      if (currentBorder == null)
         element.setBorder(border);
      else
         element.setBorder(new CompoundBorder(border, currentBorder));
   }

   //============== LOGS =================
   /* Writes the logs in the log area */
   public void updateLogs() {
      Log.fct(4, "Window.updateLogs");
      //logTextPane = new JTextPane();
      logTextPane.setText("");//Rewrite everytime TODO optimize (maybe not because of languages)
      StyledDocument logDoc = logTextPane.getStyledDocument();
      for (GUILogs.LogMsg currentLogMsg : logs) {
         try {
            switch (currentLogMsg.getType()) {
               case ERROR:
                  logDoc.insertString(logDoc.getLength(), "\n"+errorLogsOpenings[0].toString()+" "+currentLogMsg.getString()+"\n\n", logDoc.getStyle("big"));
                  break;
               case WARNING:
                  logDoc.insertString(logDoc.getLength(), errorLogsOpenings[1].toString()+" "+currentLogMsg.getString()+"\n", logDoc.getStyle("bold"));
                  break;
               case IMPORTANT:
                  logDoc.insertString(logDoc.getLength(), currentLogMsg.getString()+"\n", logDoc.getStyle("bold"));
                  break;
               case NORMAL:
                  logDoc.insertString(logDoc.getLength(), currentLogMsg.getString()+"\n", logDoc.getStyle("normal"));
                  break;
               case MINOR:
                  logDoc.insertString(logDoc.getLength(), currentLogMsg.getString()+"\n", logDoc.getStyle("minor"));
                  break;
               default://nothing to do (should never happen)
                  break;
            }
         } catch (BadLocationException e) {
            Log.err("Couldn't insert text in the log text pane.");
         }
      }
      //--------- Scroll down automatically ------------------
      JScrollBar bar = logAreaScrollPane.getVerticalScrollBar();
      bar.setValue(bar.getMaximum());

      // logTextPane.updateUI();
      // logTextPane.revalidate();
      // logTextPane.validate();
      // logTextPane.repaint();
      // logTextPane.paintImmediately(logTextPane.getVisibleRect());//dangerous
      //
      // logAreaScrollPane.updateUI();
      // logAreaScrollPane.revalidate();
      // logAreaScrollPane.validate();
      // logAreaScrollPane.repaint();
   }

   /* Add @enFrLine to the array of lines to be displayed in the log area */
   public void addLog(EnFrString enFrLine, LogType type) {
      Log.fct(4, "Window.addLog");
      Log.displayed(enFrLine.toString());
      logs.add(enFrLine, type);
   }
   public void addLog(String enLine, String frLine, LogType type) {
      addLog(new EnFrString(enLine, frLine), type);
   }

   public void displayReadyMsg() {
      addLog(readyLogMsg, LogType.NORMAL);
   }

   /* Removes all the lines displayed in the log area (and put again the default line) */
   public void clearLogs() {
      Log.fct(5, "Window.clearLogs");
      logs.clear();
      displayReadyMsg();
      updateLogs();
   }

   //============ Listeners ==============
   public class ButtonListener implements ActionListener {
      public void actionPerformed(ActionEvent event) {
         Log.fct(3, "Window.ButtonListener.actionPerformed");
         if (event.getSource() == okButton) {
            Log.log("Ok button : "+Thread.currentThread().getName());
            runBackgroundThread();
         }
         else if (event.getSource() == browseButton) {
            openFileChooser();
         }
         else if (event.getSource() == clearLogsButton) {
            clearLogs();
         }
      }
   }

   public void runBackgroundThread() {
      if (backgroundThread == null || backgroundThread.getState() == Thread.State.TERMINATED) {
         backgroundThread = new Translator(this, inputFileField.getText(), outputFileField.getText(), singleFileOutput, splitFragmentsBehavior);//Thread starts in its constructor
      }
      else {
         addLog("A transcription is already being computed.", "Un processus de transcription est déjà lancé.", LogType.WARNING);
         Log.warn("Tried to run 2 background thread at the same time. Not created the second one.");
      }
   }

   public void openFileChooser() {
      Log.fct(2, "Window.openFileChooser");
      FileChooser chooser = new FileChooser(dirTranslationEnabled);
      String filenameSelected = chooser.getFileSelected();
      if (filenameSelected != null) {
         inputFileField.setText(filenameSelected);
         outputFileField.setText(FileNamesInterpreter.generateOutputFileName(filenameSelected));
      }
   }

   public class DropDownMenuListener implements ActionListener {
      public void actionPerformed(ActionEvent event) {
         Log.fct(2, "Window.DropDownMenuListener.actionPerformed");
         if (event.getSource() == outputFormatDropDownMenu) {
            String selectedItem = (String) outputFormatDropDownMenu.getSelectedItem();
            if (selectedItem == null)
               return;//nothing to do
            else if (selectedItem.equals(outputFormatsAvailable[0].toString()))
               FileNamesInterpreter.changeOutputExtension("txt");
            else if (selectedItem.equals(outputFormatsAvailable[1].toString()))
               FileNamesInterpreter.changeOutputExtension("tab");
            else
               Log.err("The output format drop down menu is badly set up : the selected item's index is out of range");
         }
         else if (event.getSource() == languageChoiceDropDownMenu) {
            String selectedItem = (String) languageChoiceDropDownMenu.getSelectedItem();
            if (selectedItem == null)
               return;//nothing to do
            else if (selectedItem.equals(languagesAvailable[0].toString()))
               EnFrString.setCurrentLanguage("English");
            else if (selectedItem.equals(languagesAvailable[1].toString()))
               EnFrString.setCurrentLanguage("French");
            else
               Log.err("The language drop down menu is badly set up : the selected item's index is out of range");
            if (languagesHaveBeenSetup)//don't call it if the listener is being called because of the first drop down menu setup
               setLabels();
         }
         else if (event.getSource() == splitFragmentsDropDownMenu) {
            String selectedItem = (String) splitFragmentsDropDownMenu.getSelectedItem();
            if (selectedItem == null)
               return;//nothing to do
            else if (selectedItem.equals(splitFragmentsDropDownMenuStrings[0].toString()))
               splitFragmentsBehavior = SplitBehavior.SPLIT_ALL;
            else if (selectedItem.equals(splitFragmentsDropDownMenuStrings[1].toString()))
               splitFragmentsBehavior = SplitBehavior.SPLIT_DATE;
            else if (selectedItem.equals(splitFragmentsDropDownMenuStrings[2].toString()))
               splitFragmentsBehavior = SplitBehavior.MERGE;
            else
               Log.err("The fragment management drop down menu is badly set up : the selected item's index is out of range");
         }
      }
   }

   public class CheckBoxListener implements ActionListener {
      public void actionPerformed(ActionEvent event) {
         Log.fct(2, "Window.CheckBoxListener.actionPerformed");
         if (event.getSource() == enableDirCheckBox) {
            dirTranslationEnabled = enableDirCheckBox.isSelected();
            singleFileOutputCheckBox.setEnabled(enableDirCheckBox.isSelected());
         }
         else if (event.getSource() == singleFileOutputCheckBox) {
            singleFileOutput = !singleFileOutputCheckBox.isSelected();
         }
      }
   }
}
