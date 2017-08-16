package com.ngray.option.ui;

import java.text.NumberFormat;

import javax.swing.table.AbstractTableModel;
import com.ngray.option.volatilitysurface.VolatilitySurface;

@SuppressWarnings("serial")
public class VolatilitySurfaceTableModel extends AbstractTableModel {

	private final double[] columns;
	private final double[][] data;
	private double[] rows;
	
	public VolatilitySurfaceTableModel(VolatilitySurface volatilitySurface) {
		data = volatilitySurface.getImpliedVolatilities();
		columns = volatilitySurface.getStrikeOffsets();
		rows = volatilitySurface.getDaysToExpiry();
	}

	@Override
	public int getRowCount() {
		return data.length;
	}

	@Override
	public int getColumnCount() {
		return data[0].length + 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		if (columnIndex == 0) {
			NumberFormat formatter = NumberFormat.getNumberInstance();
			formatter.setMaximumFractionDigits(0);
			formatter.setMinimumFractionDigits(0);
			return rows[rowIndex];
		}
		NumberFormat formatter = NumberFormat.getPercentInstance();
		formatter.setMaximumFractionDigits(2);
		formatter.setMinimumFractionDigits(2);
		return formatter.format(data[rowIndex][columnIndex - 1]);
	}
	
	@Override
	public String getColumnName(int col) {
		if (col == 0) {
			return "Expiry/Strike";
		}
		return String.valueOf(columns[col - 1]);
	}
}
