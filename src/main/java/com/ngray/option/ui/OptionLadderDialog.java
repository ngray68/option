package com.ngray.option.ui;

import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import com.ngray.option.Log;
import com.ngray.option.financialinstrument.Security;
import com.ngray.option.ig.refdata.OptionReferenceData;
import com.ngray.option.ig.refdata.OptionReferenceDataMap;

public class OptionLadderDialog {

	private MainUI parentUI;
	private JDialog dialog;
	
	private JComboBox<SecurityAdapter> underlyings;
	private JScrollPane strikes;
	private StrikeSelectionTableModel strikeSelectionTableModel;
	private JComboBox<LocalDate> expiry;
	
	public OptionLadderDialog(MainUI parentUI) {
		this.parentUI = parentUI;
		initialize();
	}
		
	private void initialize() {
		dialog = new JDialog(parentUI.getParentFrame(), "Option Ladder");
		dialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		
		underlyings = new JComboBox<>();
		expiry = new JComboBox<>();
		
		strikeSelectionTableModel = new StrikeSelectionTableModel();
		JTable strikeSelectionTable = new JTable(strikeSelectionTableModel);
		strikes = new JScrollPane(strikeSelectionTable);
		
		underlyings.addItem(new SecurityAdapter());
		OptionReferenceDataMap.getUnderlyings().forEach(
				(underlying) -> underlyings.addItem(new SecurityAdapter(underlying))
			);
		
		underlyings.addActionListener(
				(event) -> { 
					Security selectedUnderlying = underlyings.getItemAt(underlyings.getSelectedIndex()).getSecurity();
					onUnderlyingSelected(selectedUnderlying); 
					
					}
				);
		
		dialog.setLayout(new GridLayout(0,2));
		dialog.add(new JLabel("Unfinished"));
		dialog.add(new JLabel(""));
		dialog.add(new JLabel("Underlying: "));
		dialog.add(underlyings);
		dialog.add(new JLabel("Strikes:"));
		dialog.add(strikes);
		dialog.add(new JLabel("Expiry: "));
		dialog.add(expiry);
		dialog.add(new JButton("OK"));
		dialog.add(new JButton("Cancel"));
		
		dialog.pack();
		dialog.setVisible(true);	
	}
	
	private void onUnderlyingSelected(Security underlying) {
		strikeSelectionTableModel.removeStrikes();
		expiry.removeAllItems();
		
		if (underlying == null) return;
		
		Log.getLogger().debug("OptionLadderDialog: selected underlying " + underlying);
		List<OptionReferenceData> options = OptionReferenceDataMap.getOptionReferenceData(underlying);
		strikeSelectionTableModel.addStrikes(options);
		Set<LocalDate> expiryDates = 	getOptionExpiryDates(options);
		expiryDates.forEach(date -> expiry.addItem(date));	
	}
	
	private Set<LocalDate> getOptionExpiryDates(List<OptionReferenceData> options) {
		Set<LocalDate> expiryDates = new TreeSet<>();
		options.forEach(option -> expiryDates.add(option.getExpiryDate()));
		return expiryDates;
	}

	public class SecurityAdapter {
		
		private final Security security;
		
		public SecurityAdapter() {
			security = null;
		}
		
		public SecurityAdapter(Security security) {
			this.security = security;
		}
		
		public Security getSecurity() {
			return security;
		}
		
		@Override
		public String toString() {
			if (security != null) {
				return security.getName() + " " + security.getIGMarket().getExpiry();
			}
			return "";
		}
	}
}
