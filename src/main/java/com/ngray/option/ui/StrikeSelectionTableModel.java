package com.ngray.option.ui;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.table.DefaultTableModel;

import com.ngray.option.ig.refdata.OptionReferenceData;

@SuppressWarnings("serial")
public class StrikeSelectionTableModel extends DefaultTableModel {

	private final static String[] columns = { "Select", "Strike" };

	public StrikeSelectionTableModel() {
		super(columns, 0);
	}
	
	@Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
        case 0:
            return Boolean.class;
        default:
            return String.class;
        }
    }
	
	public void addStrikes(List<OptionReferenceData> options) {
		Set<Double> uniqueStrikes = new TreeSet<>();
		for (OptionReferenceData option : options) {
			uniqueStrikes.add(option.getStrike());
		}
		uniqueStrikes.forEach(strike -> { this.addRow(new Object[] { false, strike }); });
		fireTableDataChanged();
	}

	public void removeStrikes() { 
		setRowCount(0);
	}
}
