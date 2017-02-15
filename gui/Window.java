package gui;

import java.util.ArrayList;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.JTabbedPane;
import javax.swing.Box;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;

import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyleConstants;

import util.Log;
import util.EnFrString;
import util.LogType;
//import static util.LogType.*;

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

   protected static final EnFrString okButtonLabel = new EnFrString("Run transcription", "Lancer la transcription");
   protected static final EnFrString browseButtonLabel = new EnFrString("Browse", "Parcourir");
   protected static final EnFrString defaultInputFieldName = new EnFrString("file.xml", "fichier.xml");
   protected static final EnFrString defaultOutputFieldName = new EnFrString("file.txt", "fichier.txt");
   protected String inputFileName;
   protected String outputFileName;

   protected Box logArea = Box.createHorizontalBox();
   protected JTextPane logTextPane = new JTextPane();
   protected JScrollPane logAreaScrollPane = new JScrollPane(logTextPane);

   protected GUILogs logs = new GUILogs();

   protected static final EnFrString logAreaTitle = new EnFrString("Output", "Sortie");

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
      new EnFrString("TSV file (.tsv)", "Fichier TSV (.tsv)")
   };
   protected static EnFrString[] languagesAvailable = {
      new EnFrString("English", "Anglais"),
      new EnFrString("French", "Français")
   };

   //============ about tab elements =================
   protected JTextPane descriptionPane = new JTextPane();//TODO change to JLabel?
   protected static EnFrString description = new EnFrString(
      "This program is meant to translate XML files that describe archival materials,"
      +" into TAB or TXT files importable easily into databases.",
      "Ce programme permet de traduire des fichiers XML qui décrivent de la documentation archivistique,"
      +"en fichiers TAB ou TXT facilement importables dans des bases de données."
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
      "You can set the format of the output file (TAB, TXT or TSV) in the option tab.",
      "Vous pouvez régler le format du fichier de sortie (TAB, TXT ou TSV) dans l'onglet 'Options'."
   );
   protected static EnFrString credits = new EnFrString(
      "\u00A9 2017 S. Gustin",
      "\u00A9 2017 S. Gustin"
   );

   public Window() {
      this(new EnFrString("XML to TSV transcriptor", "Transcripteur XML vers TSV"));
   }

   public Window(String enTitle, String frTitle) {
      this(new EnFrString(enTitle, frTitle));
   }

   public Window(EnFrString title) {
      this.title = title;
      this.setSize(defaultWidth, defaultHeight);
      this.setLocationRelativeTo(null);//center window
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      //get system theme
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
         Log.err("Couldn't get system's window theme.");
      }//*/

      //--------- Initialize file names and styles --------------
      inputFileName = defaultInputFieldName.toString();
      outputFileName = defaultOutputFieldName.toString();

      initLogsStyles(logTextPane.getStyledDocument());
      initDescStyles(descriptionPane.getStyledDocument());

      placeElements();
      setLabels();

      this.setVisible(true);
   }

   public void placeElements() {
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

      Box mainTab = Box.createVerticalBox();
      mainTab.add(line1);
      mainTab.add(line2);
      mainTab.add(line3);
      mainTab.add(logArea);
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

      Box optionsTab = Box.createVerticalBox();
      optionsTab.add(outputFormatLine);
      optionsTab.add(languageLine);
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

      writeLogs();

      //=========== options tab =============
      outputFormatLabel.setText(outputFormatLabelString.toString());
      languageChoiceLabel.setText(languageChoiceLabelString.toString());

      setDropDownMenusItems();

      //=========== about tab ==============
      writeDesc();
   }

   protected void writeDesc() {
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
      outputFormatDropDownMenu.removeAllItems();
      for (EnFrString currentItem : outputFormatsAvailable)
         outputFormatDropDownMenu.addItem(currentItem.toString());

      languageChoiceDropDownMenu.removeAllItems();
      for (EnFrString currentItem : languagesAvailable)
         languageChoiceDropDownMenu.addItem(currentItem.toString());
   }

   private void setElementBorder(JComponent element, EmptyBorder border) {
      Border currentBorder = element.getBorder();
      if (currentBorder == null)
         element.setBorder(border);
      else
         element.setBorder(new CompoundBorder(border, currentBorder));
   }

   //============== LOGS =================
   /*
    * Writes the logs in the log area
    */
   public void writeLogs() {
      logTextPane.setText("");//Rewrite everytime TODO optimize (maybe not because of languages)
      StyledDocument logDoc = logTextPane.getStyledDocument();
      for (GUILogs.LogMsg currentLogMsg : logs) {
         try {
            switch (currentLogMsg.getType()) {
               case ERROR:
                  logDoc.insertString(logDoc.getLength(), errorLogsOpenings[0].toString()+" "+currentLogMsg.getString()+"\n", logDoc.getStyle("big"));
                  break;
               case WARNING:
                  logDoc.insertString(logDoc.getLength(), errorLogsOpenings[1].toString()+" "+currentLogMsg.getString()+"\n", logDoc.getStyle("bold"));
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
   }

   /*
    * Add @enFrLine to the array of lines to be displayed in the log area
    */
   public void addLog(EnFrString enFrLine, LogType type) {
      logs.add(enFrLine, type);
      writeLogs();
   }
   public void addLog(String enLine, String frLine, LogType type) {
      addLog(new EnFrString(enLine, frLine), type);
   }

   /*
    * Removes all the lines displayed in the log area
    */
   public void clearLogs() {
      logs.clear();
      writeLogs();
   }
}
