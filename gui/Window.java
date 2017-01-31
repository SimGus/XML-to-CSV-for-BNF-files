package gui;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.JTabbedPane;
import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import util.Log;
import util.EnFrString;

public class Window extends JFrame {
   protected static final EnFrString[] tabTitles = {new EnFrString("Translate", "Traduire"), new EnFrString("Options", "Options")};

   protected EnFrString title;
   protected JTabbedPane tab = new JTabbedPane();

   //--------- main tab elements ----------------
   protected JPanel mainTab = new JPanel();

   protected JButton okButton = new JButton();
   protected JButton browseButton = new JButton();
   protected EditText inputFileField = new EditText();
   protected EditText outputFileField = new EditText();

   protected static final EnFrString okButtonLabel = new EnFrString("Run transcription", "Lancer la transcription");
   protected static final EnFrString browseButtonLabel = new EnFrString("Browse", "Parcourir");
   protected static final EnFrString defaultInputFieldName = new EnFrString("file.xml", "fichier.xml");
   protected static final EnFrString defaultOutputFieldName = new EnFrString("file.txt", "fichier.txt");
   protected String inputFileName;
   protected String outputFileName;

   //----------- options tab elements ------------

   public Window() {
      this(new EnFrString("XML to TSV transcriptor", "Transcripteur XML vers TSV"));
   }

   public Window(String enTitle, String frTitle) {
      this(new EnFrString(enTitle, frTitle));
   }

   public Window(EnFrString title) {
      this.title = title;
      this.setSize(640, 480);
      this.setLocationRelativeTo(null);
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      //get system theme
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
         Log.err("Couldn't get system's window theme.");
      }//*/

      inputFileName = defaultInputFieldName.toString();
      outputFileName = defaultOutputFieldName.toString();

      mainTab.setLayout(new GridBagLayout());
      //mainTab.setPreferredSize(new Dimension(640, 480));

      regenerateElements();

      this.setVisible(true);
   }

   public void regenerateElements() {
      this.setTitle(title.toString());

      //--------- make the main tab -----------
      okButton.setText(okButtonLabel.toString());
      browseButton.setText(browseButtonLabel.toString());
      inputFileField.setText(inputFileName);
      outputFileField.setText(outputFileName);

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(2, 2, 2, 2);
      gbc.weightx = gbc.weighty = 1.0;

      gbc.gridx = gbc.gridy = 0;
      gbc.gridheight = 1;
      gbc.gridwidth = 3;
      mainTab.add(inputFileField, gbc);

      gbc.gridx = 3;
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      mainTab.add(browseButton, gbc);

      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.gridheight = 1;
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      mainTab.add(outputFileField, gbc);

      //---------- make the option tab ---------------

      //---------- put contents in the tabs -------------
      tab.add(tabTitles[0].toString(), mainTab);
      tab.add(tabTitles[1].toString(), null);

      this.getContentPane().add(tab);
   }
}
