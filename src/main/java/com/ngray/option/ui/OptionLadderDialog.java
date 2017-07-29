package com.ngray.option.ui;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import com.ngray.option.Log;
import com.ngray.option.financialinstrument.EuropeanOption;
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
	private JButton ok;
	private JButton cancel;
	
	private Security underlyingSelected;
	private Set<Double> strikesSelected;
	private LocalDate expirySelected;
	
	public OptionLadderDialog(MainUI parentUI) {
		this.parentUI = parentUI;
		initialize();
	}
		
	private void initialize() {
		underlyingSelected = null;
		strikesSelected = new TreeSet<>();
		expirySelected = null;
		
		createComponents();
		addListeners();	
	}
	
	private void createComponents() {
		
		dialog = new JDialog(parentUI.getParentFrame(), "Option Ladder");
		dialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		dialog.setLayout(new GridLayout(0,2));
		
		JLabel underlyingLabel = new JLabel("Underlying: ");	
		underlyings = new JComboBox<>();
		underlyings.addItem(new SecurityAdapter());
		OptionReferenceDataMap.getUnderlyings().forEach(
				(underlying) -> underlyings.addItem(new SecurityAdapter(underlying))
			);
		
		
		JLabel strikesLabel = new JLabel("Strikes: ");
		strikeSelectionTableModel = new StrikeSelectionTableModel();
		JTable strikeSelectionTable = new JTable(strikeSelectionTableModel);
		strikes = new JScrollPane(strikeSelectionTable);
		
		JLabel expiryLabel = new JLabel("Expiry: ");
		expiry = new JComboBox<>();
		ok = new JButton("OK");
		cancel = new JButton("Cancel");
		
		dialog.add(underlyingLabel);
		dialog.add(underlyings);
		dialog.add(strikesLabel);
		dialog.add(strikes);
		dialog.add(expiryLabel);
		dialog.add(expiry);
		dialog.add(ok);
		dialog.add(cancel);
		dialog.pack();
		show(dialog);
		
	}
	
	private void show(JDialog dialog) {
		EventQueue.invokeLater(()-> {
			try {
				dialog.setVisible(true);
			} catch (HeadlessException e) {
				Log.getLogger().error(e.getMessage(), e);
			}
		});
	}

	private void addListeners() {
		underlyings.addActionListener(
				(event) -> { 
					Security selectedUnderlying = underlyings.getItemAt(underlyings.getSelectedIndex()).getSecurity();
					onUnderlyingSelected(selectedUnderlying); 				
					}
				);
		
		strikeSelectionTableModel.addTableModelListener(
				(event) -> { 
						int row = event.getFirstRow();
					    int column = event.getColumn();
					    if (strikeSelectionTableModel.getColumnClass(column) == Boolean.class) {
					        TableModel model = (TableModel) event.getSource();
					        boolean checked = (boolean) model.getValueAt(row, column);
					        double strike = (double)model.getValueAt(row, 1);
					        onStrikeSelected(strike, checked);
					    }
					}
				);
		
		expiry.addActionListener(
				(event) -> { 
						LocalDate selectedDate = expiry.getItemAt(expiry.getSelectedIndex());
						onExpiryDateSelected(selectedDate); 				
					}
				);
		
		ok.addActionListener(event -> onOK());
		cancel.addActionListener(event -> onCancel());		
	}
	
	private void onUnderlyingSelected(Security underlying) {
		strikeSelectionTableModel.removeStrikes();
		expiry.removeAllItems();
		underlyingSelected = underlying;		
		if (underlying == null) return;
		
		Log.getLogger().debug("OptionLadderDialog: selected underlying " + underlying);
		List<OptionReferenceData> options = OptionReferenceDataMap.getOptionReferenceData(underlying);
		strikeSelectionTableModel.addStrikes(options);
		Set<LocalDate> expiryDates = 	getOptionExpiryDates(options);
		expiryDates.forEach(date -> expiry.addItem(date));	
	}

	private void onStrikeSelected(double strike, boolean checked) {
		if (checked) {
            strikesSelected.add(strike);
        } else {
            strikesSelected.remove(strike);
        }	
	}
	
	private void onExpiryDateSelected(LocalDate expiryDate) {
		expirySelected = expiryDate;
	}

	private void onOK() {
		if (validate()) {
			List<EuropeanOption> options = createOptions();
			new OptionRiskLadderView(parentUI, underlyingSelected, expirySelected, options);
			dialog.dispose();
		}
		
	}

	private List<EuropeanOption> createOptions() {
		List<EuropeanOption> options = new ArrayList<EuropeanOption>();
		List<OptionReferenceData> optionRefData = OptionReferenceDataMap.getOptionReferenceData(underlyingSelected);
		List<OptionReferenceData> filteredRefData 
									= optionRefData.stream()
									.filter(refData -> expirySelected.equals(refData.getExpiryDate()) && strikesSelected.contains(refData.getStrike()))
									.collect(Collectors.toList());
		
		filteredRefData.forEach(refData -> {
			options.add(new EuropeanOption(refData.getOptionName(), underlyingSelected, refData.getStrike(), expirySelected, refData.getCallOrPut()));
		});
		
		return options;
	}

	private void onCancel() {
		dialog.dispose();
	}
	
	private Set<LocalDate> getOptionExpiryDates(List<OptionReferenceData> options) {
		Set<LocalDate> expiryDates = new TreeSet<>();
		options.forEach(option -> expiryDates.add(option.getExpiryDate()));
		return expiryDates;
	}
	
	private boolean validate() {
		// TODO Auto-generated method stub
		return true;
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
