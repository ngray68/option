package com.ngray.option.ui;

import java.text.NumberFormat;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.ngray.option.Log;
import com.ngray.option.analysis.OptionRiskLadder;
import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.risk.Risk;
import com.ngray.option.risk.RiskListener;

@SuppressWarnings("serial")
public class OptionRiskLadderTableModel extends AbstractTableModel implements RiskListener {
	
	private static final String[] columns = {
			"LastUpdate",
			"Strike",
			"Spot",
			"Price",
			"ImpVol",
			"Delta",
			"Gamma",
			"Vega",
			"Theta",
			"Rho"		
	};
	
	private enum ColumnIndex {
		LASTUPDATE(0),
		STRIKE(1),
		SPOT(2),
		PRICE(3),
		IMPVOL(4),
		DELTA(5),
		GAMMA(6),
		VEGA(7),
		THETA(8),
		RHO(9);
		
		private int value;
		
		private ColumnIndex(int value) {
			this.value = value;
		}
		
		public int toInt() {
			return value;
		}
		
	};
	
	private final Object[][] data;
	private Map<EuropeanOption, Integer> rowIndices;
	
	OptionRiskLadderTableModel(OptionRiskLadder optionRiskLadder) {
		Map<EuropeanOption, Risk> riskMap = optionRiskLadder.getRiskMap();
		data = new Object[riskMap.size()][columns.length];
		rowIndices = new HashMap<>();
		int i = 0;
		for (EuropeanOption option : riskMap.keySet()) {
			rowIndices.put(option, i);
			Risk risk = riskMap.get(option);
			data[i][ColumnIndex.LASTUPDATE.toInt()] = LocalTime.now();
			data[i][ColumnIndex.STRIKE.toInt()] = option.getStrike();
			data[i][ColumnIndex.SPOT.toInt()] = risk.getUnderlyingPrice();
			data[i][ColumnIndex.PRICE.toInt()] = risk.getPrice();
			data[i][ColumnIndex.IMPVOL.toInt()] = risk.getImpliedVolatility();
			data[i][ColumnIndex.DELTA.toInt()] = risk.getDelta();
			data[i][ColumnIndex.GAMMA.toInt()] = risk.getGamma();
			data[i][ColumnIndex.VEGA.toInt()] = risk.getVega();
			data[i][ColumnIndex.THETA.toInt()] = risk.getTheta();
			data[i][ColumnIndex.RHO.toInt()] = risk.getRho();
			++i;
		}
		optionRiskLadder.addListener(this);
	}

	@Override
	public void onRiskUpdate(FinancialInstrument instrument, Risk risk) {
		if (instrument == null) {
			Log.getLogger().warn("OptionRiskLadderTableModel::onRiskUpdate called for null financial instrument - ignoring");			
		}
		if (!(instrument instanceof EuropeanOption)) {
			Log.getLogger().warn("OptionRiskLadderTableModel::onRiskUpdate called for non-option financial instrument - ignoring");
		}
		EuropeanOption option = (EuropeanOption)instrument;
		
		Integer i = rowIndices.get(option);
		if (i == null) {
			Log.getLogger().warn("OptionRiskLadderTableModel::onRiskUpdate called for financial instrument not present in the table - ignoring");			
		}
		
		data[i][ColumnIndex.LASTUPDATE.toInt()] = LocalTime.now();
		data[i][ColumnIndex.SPOT.toInt()] = risk.getUnderlyingPrice();
		data[i][ColumnIndex.PRICE.toInt()] = risk.getPrice();
		data[i][ColumnIndex.IMPVOL.toInt()] = risk.getImpliedVolatility();
		data[i][ColumnIndex.DELTA.toInt()] = risk.getDelta();
		data[i][ColumnIndex.GAMMA.toInt()] = risk.getGamma();
		data[i][ColumnIndex.VEGA.toInt()] = risk.getVega();
		data[i][ColumnIndex.THETA.toInt()] = risk.getTheta();
		data[i][ColumnIndex.RHO.toInt()] = risk.getRho();
		this.fireTableRowsUpdated(i, i);
	}

	@Override
	public int getRowCount() {
		return data.length;
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		NumberFormat formatter = null;
		switch (ColumnIndex.values()[columnIndex]) {
			case SPOT:
			case PRICE:
			case DELTA:	
			case VEGA:
			case THETA:
			case RHO:
				formatter = NumberFormat.getNumberInstance();
				formatter.setMinimumFractionDigits(2);
				formatter.setMaximumFractionDigits(2);
				return formatter.format(data[rowIndex][columnIndex]);
			case GAMMA:
			case IMPVOL:
				formatter = NumberFormat.getPercentInstance();
				formatter.setMinimumFractionDigits(2);
				formatter.setMaximumFractionDigits(2);
				return formatter.format(data[rowIndex][columnIndex]);
			default:
				return data[rowIndex][columnIndex];
		}
	
	}	
	
	@Override
	public String getColumnName(int col) {
        return columns[col];
    }
	
	@Override
	public boolean isCellEditable(int row, int col) {     
	    return false;	
	}

}
