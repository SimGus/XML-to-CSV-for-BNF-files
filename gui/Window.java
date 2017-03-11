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

import javax.swing.SwingUtilities;

import util.Log;
import util.EnFrString;
import util.LogType;
//import static util.LogType.*;
import backend.files.FileNamesInterpreter;
import backend.files.FileOpener;
import backend.parser.Parser;
import backend.transcripter.Interpreter;

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
   protected boolean languagesHaveBeenSetup = false;//changed to true when the language drop down menu has been set up to avoid setting it twice because of the ActionListener

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
   protected GUILogs logs = new GUILogs(readyLogMsg, LogType.NORMAL);

   protected static final EnFrString logAreaTitle = new EnFrString("Output", "Sortie");

   protected JButton clearLogsButton = new JButton();
   protected static final EnFrString clearLogsButtonLabel = new EnFrString("Clear output", "Vider la zone de messages de sortie");

   //============ options tab elements ===============
   protected JLabel outputFormatLabel = new JLabel();
   protected JLabel languageChoiceLabel = new JLabel();
   protected static EnFrString outputFormatLabelString = new EnFrString("Output file format :", "Format du fichier de sortie :");
   protected static EnFrString languageChoiceLabelString = new EnFrString("Language :", "Langue :");

   protected JComboBox outputFormatDropDownMenu = new JComboBox();
   protected JComboBox languageChoiceDropDownMenu = new JComboBox();
   protected static EnFrString[] outputFormatsAvailable = {
      new EnFrString("TXT file (.txt)", "Fichier TXT (.txt)"),
      new EnFrString("TAB file (.tab)", "Fichier TAB (.tab)"),
   };
   protected static EnFrString[] languagesAvailable = {
      new EnFrString("English", "Anglais"),
      new EnFrString("French", "Français")
   };

   protected JLabel enableDirCheckBoxLabel = new JLabel();
   protected JCheckBox enableDirCheckBox = new JCheckBox();
   protected static EnFrString enableDirCheckBoxString = new EnFrString(
      "Enable the translation of all files in the specified directory",
      "Autoriser la traduction de tous les fichier se trouvant dans le dossier spécifié"
   );

   protected JLabel singleFileOutputCheckBoxLabel = new JLabel();
   protected JCheckBox singleFileOutputCheckBox = new JCheckBox();
   protected static EnFrString singleFileOutputCheckBoxString = new EnFrString(
      "Generate one output file for each XML file translated.",
      "Générer un fichier de sortie par fichier XML traduit."
   );

   //============ about tab elements =================
   protected JTextPane descriptionPane = new JTextPane();//TODO change to JLabel?
   protected static EnFrString description = new EnFrString(
      "This program is meant to translate XML files that describe archival materials,"
      +" into TAB or TXT files importable easily into databases.",
      "Ce programme permet de traduire des fichiers XML qui décrivent de la documentation archivistique,"
      +" en fichiers TAB ou TXT facilement importables dans des bases de données."
   );
   protected static EnFrString usageTitle = new EnFrString("Usage", "Utilisation");
   protected static EnFrString usage = new EnFrString(
      "Specify the path to the XML file you want to translate (you can do so using the 'Browse' button);\n\n"
      +"Optionaly specify the name of the TAB or TXT file;\n\n"
      +"Click the button 'Run transcription';\n\n"
      +"A TAB or TXT file containing the translation of the contents of the XML file will be created.",
      "Spécifiez le chemin vers le fichier XML (par exemple en utilisant le bouton 'Parcourir');\n\n"
      +"Éventuellement, spécifiez le nom du fichier TAB ou TXT à créer;\n\n"
      +"Cliquez sur 'Lancer la transcription';\n\n"
      +"Un fichier TAB ou TXT contenant la traduction du contenu du fichier XML sera créé."
   );
   protected static EnFrString precision = new EnFrString(
      "You can set the format of the output file (TAB or TXT) in the option tab.\n"
      +"If the option 'translate directories' is enabled, the program will translate all the XML files in the specified directory.\n",
      "Vous pouvez régler le format du fichier de sortie (TAB or TXT) dans l'onglet 'Options'.\n"
      +"Si l'option 'traduire les dossiers' est activée, le programme traduira tous les fichiers XML dans le dossier spécifié."
   );
   protected static EnFrString credits = new EnFrString(
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

      //-------- Add listeners ------------
      okButton.addActionListener(new ButtonListener());
      browseButton.addActionListener(new ButtonListener());
      clearLogsButton.addActionListener(new ButtonListener());

      outputFormatDropDownMenu.addActionListener(new DropDownMenuListener());
      languageChoiceDropDownMenu.addActionListener(new DropDownMenuListener());

      enableDirCheckBox.addActionListener(new CheckBoxListener());
      singleFileOutputCheckBox.addActionListener(new CheckBoxListener());

      //-------- Make window ------------
      descriptionPane.setEditable(false);
      logTextPane.setEditable(false);
      okButton.setBackground(new Color(0x0277BD));
      okButton.setForeground(Color.WHITE);
      enableDirCheckBox.setSelected(true);
      placeElements();
      setLabels();

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

      Box optionsTab = Box.createVerticalBox();
      optionsTab.add(outputFormatLine);
      optionsTab.add(languageLine);
      optionsTab.add(dircheckBoxLine);
      optionsTab.add(singleFileCheckBoxLine);
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

      writeLogs();

      //=========== options tab =============
      outputFormatLabel.setText(outputFormatLabelString.toString());
      languageChoiceLabel.setText(languageChoiceLabelString.toString());

      enableDirCheckBoxLabel.setText(enableDirCheckBoxString.toString());
      singleFileOutputCheckBoxLabel.setText(singleFileOutputCheckBoxString.toString());

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
   public void writeLogs() {
      Log.fct(4, "Window.writeLogs");
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
      writeLogs();
   }
   public void addLog(String enLine, String frLine, LogType type) {
      addLog(new EnFrString(enLine, frLine), type);
   }

   /* Removes all the lines displayed in the log area (and put again the default line) */
   public void clearLogs() {
      Log.fct(5, "Window.clearLogs");
      logs.clear();
      addLog(readyLogMsg, LogType.NORMAL);
      writeLogs();
   }

   //============ Listeners ==============
   public class ButtonListener implements ActionListener {
      public void actionPerformed(ActionEvent event) {
         Log.fct(3, "Window.ButtonListener.actionPerformed");
         if (event.getSource() == okButton) {
            Log.log("Ok button : "+Thread.currentThread().getName());
            SwingUtilities.invokeLater(new Runnable() {
               @Override
               public void run() {
                  runTranslation();
               }
            });
            //runTranslation();
         }
         else if (event.getSource() == browseButton) {
            openFileChooser();
         }
         else if (event.getSource() == clearLogsButton) {
            clearLogs();
         }
      }
   }

   /*
    * MAIN FUNCTION - Translates the file whose name is given in the EditText
    */
   public void runTranslation() {
      //========= DEBUG ===========
      /*Log.log("Translation : "+Thread.currentThread().getName());
      for (int i=0; i<20; i++) {
         try {
            Thread.sleep(1000);
         } catch (InterruptedException e) {
            Log.err("Thread interrupted : "+e.getMessage());
         }
         addLog("Test "+i, "Test "+i, LogType.NORMAL);
         Log.log("Test "+i);
      }*/

      //========== Translation ===============
      Log.fct(1, "Window.runTranslation");
      String inputFilePath = inputFileField.getText();
      String outputFilePath = outputFileField.getText();

      if (inputFilePath != null && !inputFilePath.equals("")) {
         inputFilePath = FileNamesInterpreter.interpretInputFileName(inputFilePath);
         outputFilePath = FileNamesInterpreter.interpretOutputFileName(inputFilePath, outputFilePath);

         //==================== Translate one file ==============================
         if (!dirTranslationEnabled || FileOpener.representsAFile(inputFilePath)) {
            Log.log("Translate one file : '"+inputFilePath+"'");
            try {
               if (!FileOpener.isValidFileName(inputFilePath)) {
                  addLog("The path '"+inputFilePath+"' is not a valid file path.",
                     "Le chemin '"+inputFilePath+"' n'est pas un chemin vers un fichier valide.", LogType.ERROR);
                  return;
               }

               translate(inputFilePath, outputFilePath);

            } catch (IllegalArgumentException e) {
               addLog("There was an error while translating the file : 'Illegal argument exception - "+e.getMessage()+"'.",
                  "Une erreur s'est produite lors de la traduction du fichier : 'Illegal argument exception - "+e.getMessage()+"'.",
                  LogType.ERROR);
            } catch (UnsupportedOperationException e) {
               addLog("There was an error while translating the file 'Unsupported operation exception - "+e.getMessage()+"'.",
               "Une erreur s'est produite lors de la traduction du fichier : 'Unsuppported operation exception - "+e.getMessage()+"'.",
               LogType.ERROR);
            } catch (Exception e) {
               addLog("There was an error while translating the file : '"+e.getMessage()+"'.",
                  "Une erreur s'est produite lors de la traduction du fichier : '"+e.getMessage()+"'.",
                  LogType.ERROR);
            }

         }
         //======================= Translate one directory ==============================
         else {//dirTranslationEnabled
            Log.log("Translate whole directory : '"+inputFilePath+"'");
            try {
               addLog("\n=====================================\n"
                  +"Starting the translation of the XML files in the directory '"+inputFilePath+"'.\n",
                  "\n=====================================\n"
                  +"Lancement de la traduction des fichiers XML se trouvant dans le dossier '"+inputFilePath+"'.\n",
                  LogType.NORMAL
               );

               File[] filesInDir = FileOpener.getFilesInDirectory(inputFilePath);
               if (filesInDir.length <= 0)
                  addLog("The directory specified is empty.", "Le dossier spécifié est vide.", LogType.WARNING);

               ArrayList<HashMap<String, String>> allFilesFields = new ArrayList<HashMap<String, String>>();//only for single output

               int i=0;
               Log.log("Beginning the translation of all files ("+filesInDir.length+" files)");
               for (File inputFile : filesInDir) {
                  i++;
                  //--------- Check if current file is an XML file --------------
                  String currentInputFilePath = inputFile.getAbsolutePath();
                  if (FileNamesInterpreter.isAnXMLFile(currentInputFilePath)) {
                     Log.log("Translation of file '"+inputFile.getName()+"' ("+i+"/"+filesInDir.length+")");
                     if (!FileOpener.isValidFileName(currentInputFilePath)) {
                        addLog("The path '"+currentInputFilePath+"' is not a valid file path. Moving on to the next file in the directory.",
                           "Le chemin '"+currentInputFilePath+"' n'est pas valide. Passage au fichier suivant dans le dossier.",
                           LogType.ERROR);
                        continue;
                     }

                     if (!singleFileOutput) {
                        String currentOutputFilePath = FileNamesInterpreter.generateOutputFileName(currentInputFilePath, outputFilePath);
                        translate(currentInputFilePath, currentOutputFilePath);//translates and write the current file
                     }
                     else {//single output for all the files in the directory
                        ArrayList<HashMap<String, String>> currentTranslation = translate(currentInputFilePath);
                        if (currentTranslation != null)
                           allFilesFields.addAll(currentTranslation);
                     }
                  }
                  else//not an XML file
                     continue;

                  Log.log("Translation of '"+inputFile.getName()+"' over");
               }

               //========== Make and write the single file (if directory is translating to only one single file) ===========
               if (singleFileOutput) {
                  Log.log("Creating single file output '"+outputFilePath+"'");
                  addLog("Writing translations in file '"+outputFilePath+"'.", "Lancement de l'écriture des traductions dans le fichier '"+outputFilePath+"'.", LogType.NORMAL);
                  ArrayList<String> linesToWrite = Interpreter.generateLines(allFilesFields, this);
                  //---------- Writing ---------------
                  if (!FileNamesInterpreter.checkExtensionsCoherence(outputFilePath))
                     addLog("The name of the output file provided does not have the same extension as what has been set in the options ('."+FileNamesInterpreter.getOutputExtension()+"'). The name provided will be used.",
                        "Le nom du fichier de sortie fourni n'a pas la même extension que ce qui a été réglé dans les options ('."+FileNamesInterpreter.getOutputExtension()+"'). Le nom fourni sera utilisé.",
                        LogType.WARNING);

                  FileOpener.writeFile(outputFilePath, linesToWrite, this);
               }

               addLog("\nTranslation of the XML files in the directory '"+inputFilePath+"' over.\n"
                  +"=====================================\n",
                  "\nTraduction des fichiers XML se trouvant dans le dossier '"+inputFilePath+"' terminée.\n"
                  +"=====================================\n",
                  LogType.NORMAL
               );

            } catch (IllegalArgumentException e) {
               addLog("There was an error while translating the file : 'Illegal argument exception - "+e.getMessage()+"'.",
                  "Une erreur s'est produite lors de la traduction du fichier : 'Illegal argument exception - "+e.getMessage()+"'.",
                  LogType.ERROR);
            } catch (UnsupportedOperationException e) {
               addLog("There was an error while translating the file 'Unsupported operation exception - "+e.getMessage()+"'.",
               "Une erreur s'est produite lors de la traduction du fichier : 'Unsuppported operation exception - "+e.getMessage()+"'.",
               LogType.ERROR);
            } catch (Exception e) {
               addLog("There was an error while translating the file : '"+e.getMessage()+"'.",
                  "Une erreur s'est produite lors de la traduction du fichier : '"+e.getMessage()+"'.",
                  LogType.ERROR);
            }
         }
      }
      else {
         addLog("No name for the XML file provided.", "Pas de nom pour le fichier XML fourni.", LogType.ERROR);
      }

      addLog(readyLogMsg, LogType.NORMAL);
   }

   protected void translate(String inputFilePath, String outputFilePath) {
      Log.fct(2, "Window.translate");
      String inputFileName = FileNamesInterpreter.getFileOrDirName(inputFilePath);
      String outputFileName = FileNamesInterpreter.getFileOrDirName(outputFilePath);
      if (!FileOpener.fileExists(inputFilePath)) {
         addLog("The specified file named '"+inputFileName+"' does not exist.",
            "Le fichier spécifié '"+inputFileName+"' n'existe pas.", LogType.WARNING);
         return;
      }

      addLog("----------------------------------------------------------------\n"
         +"Starting translation of the file '"+inputFileName+"' to the file '"+outputFileName+"'.",
         "----------------------------------------------------------------\n"
         +"Lancement de la traduction du fichier '"+inputFileName+"' vers le fichier '"+outputFileName+"'.",
         LogType.NORMAL
      );

      //============ Reset parser and interpreter =============
      Parser.reset();
      Interpreter.reset();

      //--------- Parsing -----------
      boolean somethingToTranslate = Parser.parse(inputFilePath, this);

      if (somethingToTranslate) {
         //----------- Translation -------------
         ArrayList<String> linesToWrite = Interpreter.translateTreeAndMakeLines(this);

         //---------- Writing ---------------
         if (!FileNamesInterpreter.checkExtensionsCoherence(outputFilePath))
            addLog("The name of the output file provided does not have the same extension as what has been set in the options ('."+FileNamesInterpreter.getOutputExtension()+"'). The name provided will be used.",
               "Le nom du fichier de sortie fourni n'a pas la même extension que ce qui a été réglé dans les options ('."+FileNamesInterpreter.getOutputExtension()+"'). Le nom fourni sera utilisé.",
               LogType.WARNING);

         FileOpener.writeFile(outputFilePath, linesToWrite, this);

         addLog("... Translation of the file '"+inputFileName+"' to the file '"+outputFileName+"' done.\n"
            +"----------------------------------------------------------------",
            "... Traduction du fichier '"+inputFileName+"' vers le fichier '"+outputFileName+"' terminée.\n"
            +"----------------------------------------------------------------",
            LogType.NORMAL
         );
      }
      else {
         addLog("----------------------------------------------------------------",
            "----------------------------------------------------------------",
            LogType.NORMAL
         );
      }
   }

   protected ArrayList<HashMap<String, String>> translate(String inputFilePath) {
      Log.fct(3, "Window.translate(ArrayList)");
      String inputFileName = FileNamesInterpreter.getFileOrDirName(inputFilePath);
      if (!FileOpener.fileExists(inputFilePath)) {
         addLog("The specified file name '"+inputFileName+"' does not exist.",
            "Le fichier spécifié '"+inputFileName+"' n'existe pas.", LogType.WARNING);
         return null;
      }

      addLog("----------------------------------------------------------------\n"
         +"Starting translation of the file '"+inputFileName+"'.",
         "----------------------------------------------------------------\n"
         +"Lancement de la traduction du fichier '"+inputFileName+"'.",
         LogType.NORMAL
      );

      //============ Reset parser and interpreter ===================
      Parser.reset();
      Interpreter.reset();

      //----------- Parsing ----------------
      boolean somethingToTranslate = Parser.parse(inputFilePath, this);

      if (somethingToTranslate) {
         ArrayList<HashMap<String, String>> answer = Interpreter.translateTree(this);

         addLog("... Translation of the file '"+inputFileName+"' done.\n"
            +"----------------------------------------------------------------",
            "... Traduction du fichier '"+inputFileName+"' terminée.\n"
            +"----------------------------------------------------------------",
            LogType.NORMAL
         );
         return answer;
      }
      else {
         addLog("----------------------------------------------------------------",
            "----------------------------------------------------------------",
            LogType.NORMAL
         );
         return null;
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
         }
         else if (event.getSource() == languageChoiceDropDownMenu) {
            String selectedItem = (String) languageChoiceDropDownMenu.getSelectedItem();
            if (selectedItem == null)
               return;//nothing to do
            else if (selectedItem.equals(languagesAvailable[0].toString()))
               EnFrString.setCurrentLanguage("English");
            else if (selectedItem.equals(languagesAvailable[1].toString()))
               EnFrString.setCurrentLanguage("French");
            if (languagesHaveBeenSetup)//don't call it if the listener is being called because of the first drop down menu setup
               setLabels();
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
