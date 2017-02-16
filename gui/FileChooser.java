package gui;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import javax.swing.UIManager;

import util.EnFrString;
import util.Log;

public class FileChooser extends JFileChooser
{
	private static EnFrString title = new EnFrString("Choose a file", "Choisir un fichier");
	private static EnFrString filterName = new EnFrString("XML file", "fichier XML");

	public FileChooser()
	{
		this(title.toString());
		//TODO REMOVE THIS PART
		try {
         UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (Exception e) {
         Log.err("Couldn't set file chooser's window theme.");
      }
		super.updateUI();
		try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
         Log.err("Couldn't get system's window theme.");
      }
		//TODO UNTIL HERE
	}

	public FileChooser(String windowTitle)
	{
		super();
		setCurrentDirectory(new File("."));
		setDialogTitle(windowTitle);
		setFileSelectionMode(JFileChooser.FILES_ONLY);
		setFileFilter(new FileNameExtensionFilter(filterName.toString(), "xml"));
	}

	public String getFileSelected()
	{
		if (showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
		{
			return getSelectedFile().toString();
		}
		else
			return null;
	}
}
