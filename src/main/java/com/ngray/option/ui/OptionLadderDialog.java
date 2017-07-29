package com.ngray.option.ui;

import java.awt.EventQueue;
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
import net.miginfocom.swing.MigLayout;

/**
 * Dialog box where the user chooses a set of options to view risk and volatility.
 * Choose an underlying, an option expiry date and a set of strikes. On OK, the user
 * will see two tables (one for calls and one for puts) showing risk on the options
 * selected.
 * @author nigelgray
 *
 */
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
	
	/**
	 * Constructor
	 * @param parentUI
	 */
	public OptionLadderDialog(MainUI parentUI) {
		this.parentUI = parentUI;
		initialize();
	}

	/**
	 * Show this dialog box
	 */
	public void show() {
		EventQueue.invokeLater(()-> {
			try {
				dialog.setVisible(true);
			} catch (HeadlessException e) {
				Log.getLogger().error(e.getMessage(), e);
			}
		});
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
		dialog.setResizable(false);
		
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
		
		JLabel expiryLabel = new JLabel("Option Expiry: ");
		expiry = new JComboBox<>();
		ok = new JButton("OK");
		cancel = new JButton("Cancel");
		ok.setEnabled(false);
		
		dialog.getContentPane().setLayout(new MigLayout("", "[grow][grow][grow][grow][grow][grow]", "[][][][][][][]"));		
		dialog.getContentPane().add(underlyingLabel, "cell 0 0,span,grow");
		dialog.getContentPane().add(underlyings, "cell 0 1,span,grow");
		dialog.getContentPane().add(expiryLabel, "cell 0 2,span,grow");
		dialog.getContentPane().add(expiry, "cell 0 3,span,grow");
		dialog.getContentPane().add(strikesLabel, "cell 0 4,span,grow");
		dialog.getContentPane().add(strikes, "cell 0 5,span,grow");
		dialog.getContentPane().add(ok, "cell 4 6");
		dialog.getContentPane().add(cancel, "cell 5 6");
		dialog.pack();
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
	
	// Event handler for the underlying selection
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
		
		if (validate()) {
			ok.setEnabled(true);
		} else {
			ok.setEnabled(false);
		}
	}

	// Event handler for the strike selection table
	private void onStrikeSelected(double strike, boolean checked) {
		if (checked) {
            strikesSelected.add(strike);
        } else {
            strikesSelected.remove(strike);
        }
		
		if (validate()) {
			ok.setEnabled(true);
		} else {
			ok.setEnabled(false);
		}
	}
	
	// event handler for the expiry date selection
	private void onExpiryDateSelected(LocalDate expiryDate) {
		expirySelected = expiryDate;
		if (validate()) {
			ok.setEnabled(true);
		} else {
			ok.setEnabled(false);
		}
	}

	// Event handler for OK button
	private void onOK() {	
		List<EuropeanOption> options = createOptions();
		new OptionRiskLadderView(parentUI, underlyingSelected, expirySelected, options);
		dialog.dispose();
	}
		
	// Event handler for cancel button
	private void onCancel() {
		dialog.dispose();
	}
	
	// Create option securities from the selections made
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
	
	// Get the option epxiry dates from the supplied reference data
	private Set<LocalDate> getOptionExpiryDates(List<OptionReferenceData> options) {
		Set<LocalDate> expiryDates = new TreeSet<>();
		options.forEach(option -> expiryDates.add(option.getExpiryDate()));
		return expiryDates;
	}
	
	// Validate the selection - 
	private boolean validate() {
		return (underlyingSelected != null) && (expirySelected != null) && !strikesSelected.isEmpty();
	}

	// Adapter to show a friendly name for securities in the underlyings dialog box
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
