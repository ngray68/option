package com.ngray.option.ui;

import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextPane;

import com.ngray.option.financialinstrument.Security;
import com.ngray.option.ig.refdata.OptionReferenceDataMap;

public class OptionLadderDialog {

	private MainUI parentUI;
	private JDialog dialog;
	
	public OptionLadderDialog(MainUI parentUI) {
		this.parentUI = parentUI;
		initialize();
	}
		
	private void initialize() {
		dialog = new JDialog(parentUI.getParentFrame(), "Option Ladder");
		
		JComboBox<Security> underlyings = new JComboBox<>();
		OptionReferenceDataMap.getUnderlyings().forEach(
				(underlying) -> underlyings.addItem(underlying)
			);
		dialog.setLayout(new GridLayout(0,2));
		dialog.add(new JLabel("Not yet implemented"));
		dialog.add(new JLabel(""));
		dialog.add(new JLabel("Underlying: "));
		dialog.add(underlyings);
		dialog.add(new JLabel("Strikes:"));
		dialog.add(new JTable());
		dialog.add(new JLabel("Expiry: "));
		dialog.add(new JTextPane());
		dialog.add(new JButton("OK"));
		dialog.add(new JButton("Cancel"));
		
		dialog.pack();
		dialog.setVisible(true);	
	}
	
	
}
