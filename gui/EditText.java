package gui;

import javax.swing.JTextField;
import java.awt.event.FocusListener;
import java.awt.Color;
import java.awt.event.FocusEvent;

import util.Log;

public class EditText extends JTextField implements FocusListener
{
	protected boolean alreadyBeenSelected = false;
	protected String firstText;

	public EditText() {
		super();
		setForeground(Color.LIGHT_GRAY);

		addFocusListener(this);
	}

	public EditText(int columns) {
		super(columns);
		setForeground(Color.LIGHT_GRAY);

		addFocusListener(this);
	}

	public EditText(String defaultText) {
		super(defaultText);
		firstText = defaultText;
		setForeground(Color.LIGHT_GRAY);

		addFocusListener(this);
	}

	public EditText(int columns, String defaultText) {
		super(columns);
		firstText = defaultText;
		setText(defaultText);
		setForeground(Color.LIGHT_GRAY);

		addFocusListener(this);
	}

	@Override
	public String getText() {
		if (super.getText().equals(firstText))
			return null;
		return super.getText();
	}

	@Override
	public void setText(String s) {
		alreadyBeenSelected = true;
		setForeground(Color.BLACK);
		super.setText(s);
	}

	public void setDefaultText(String defaultText) {
		firstText = defaultText;
		setText(defaultText);
	}

	public void focusGained(FocusEvent event) {
		Log.log("EDIT : focused");

		if (!alreadyBeenSelected) {
			setForeground(Color.BLACK);
			setText("");
			alreadyBeenSelected = true;
		}
	}

	public void focusLost(FocusEvent event) {
		Log.log("EDIT : focus lost");
	}
}
