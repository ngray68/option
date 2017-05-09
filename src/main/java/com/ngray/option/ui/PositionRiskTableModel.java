package com.ngray.option.ui;


import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.swing.table.AbstractTableModel;

import com.ngray.option.position.Position;
import com.ngray.option.position.PositionListener;
import com.ngray.option.risk.Risk;

@SuppressWarnings("serial")
public class PositionRiskTableModel extends AbstractTableModel implements PositionListener {

	private String[] columns = {
			"Id",
			"Security",
			"Size",
			"Open",
			"Latest",
			"P&L",
			"Imp Vol",
			"Delta",
			"Gamma",
			"Vega",
			"Theta",
			"Rho"
	};
	
	private static final int ID_COL = 0;
	private static final int SEC_COL = 1;
	private static final int SIZE_COL = 2;
	private static final int OPEN_COL = 3;
	private static final int LATEST_COL = 4;
	private static final int PNL_COL = 5;
	private static final int IV_COL = 6;
	private static final int DELTA_COL = 7;
	private static final int GAMMA_COL = 8;
	private static final int VEGA_COL = 9;
	private static final int THETA_COL = 10;
	private static final int RHO_COL = 11;
	
	private Object lock = new Object();
	private volatile Object[][] data;
	
	private Map<Position, Integer> positions;
	
	public PositionRiskTableModel(List<Position> positions) {
		this.positions = new HashMap<>();
		this.data = new Object[positions.size()+1][columns.length];
		double sumPnL = 0;
		double sumDelta = 0;
		double sumGamma = 0;
		double sumVega = 0;
		double sumTheta = 0;
		double sumRho = 0;
		
		for (int i = 0; i < data.length-1; ++i) {
			Position pos = positions.get(i);
			this.positions.put(pos, i);
			Risk risk = pos.getPositionRisk();
			data[i][ID_COL] = pos.getId();
			data[i][SEC_COL] = pos.getInstrument().getIdentifier();
			data[i][SIZE_COL] = pos.getPositionSize();
			data[i][OPEN_COL] = pos.getOpen();
			data[i][LATEST_COL] = pos.getLatest();
			data[i][PNL_COL] = pos.getPositionPnL();
			data[i][IV_COL] = risk.getImpliedVolatility();
			data[i][DELTA_COL] = risk.getDelta();
			data[i][GAMMA_COL] = risk.getGamma();
			data[i][VEGA_COL] = risk.getVega();
			data[i][THETA_COL] = risk.getTheta();
			data[i][RHO_COL] = risk.getRho();
			
			sumPnL += pos.getPositionPnL();
			sumDelta += risk.getDelta();
			sumGamma += risk.getGamma();
			sumVega += risk.getVega();
			sumTheta += risk.getTheta();
			sumRho += risk.getRho();
		}
		
		data[data.length -1][ID_COL] = "Total";
		data[data.length - 1][PNL_COL] = sumPnL;
		data[data.length - 1][DELTA_COL] = sumDelta;
		data[data.length - 1][GAMMA_COL] = sumGamma;
		data[data.length - 1][VEGA_COL] = sumVega;
		data[data.length - 1][THETA_COL] = sumTheta;
		data[data.length - 1][RHO_COL] = sumRho;
	}
	
	// PositionListener overrides
	@Override
	public void onPositionPnLUpdate(Position position) {
		synchronized(lock) {
			int rowIndex = positions.get(position);
			
			double oldPnL = (double)data[rowIndex][PNL_COL];
			double newPnL = position.getPositionPnL();
			
			data[rowIndex][LATEST_COL] = position.getLatest();
			data[rowIndex][PNL_COL] = position.getPositionPnL();
			double updatedPnL = (double)data[data.length - 1][PNL_COL] - oldPnL + newPnL;
			if (Double.isNaN(updatedPnL)) {
				updatedPnL = calculateTotalPnL();
			}
		
			data[data.length - 1][PNL_COL] = updatedPnL;	
	
			fireTableCellUpdated(rowIndex, LATEST_COL);
			fireTableCellUpdated(rowIndex, PNL_COL);
			fireTableCellUpdated(data.length - 1, PNL_COL);
		}
	}
	
	private double calculateTotalPnL() {
		double totalPnL = 0;
		for (int i = 0; i < data.length - 1; ++i) {
			totalPnL += (double)data[i][PNL_COL];
		}
		return totalPnL;
	}
	
	private double[] calculateTotalRisk() {
		double[] result = new double[] {0,0,0,0,0};
		for (int i = 0; i < data.length - 1; ++i) {		
			result[0] += (double)data[i][DELTA_COL];
			result[1] += (double)data[i][GAMMA_COL];
			result[2] += (double)data[i][VEGA_COL];
			result[3] += (double)data[i][THETA_COL];
			result[4] += (double)data[i][RHO_COL];
		}
		return result;
	}
	

	@Override
	public void onPositionRiskUpdate(Position position) {
		synchronized(lock) {
			int rowIndex = positions.get(position);
			Risk risk = position.getPositionRisk();
			data[rowIndex][IV_COL] = risk.getImpliedVolatility();
			data[rowIndex][DELTA_COL] = risk.getDelta();
			data[rowIndex][GAMMA_COL] = risk.getGamma();
			data[rowIndex][VEGA_COL] = risk.getVega();
			data[rowIndex][THETA_COL] = risk.getTheta();
			data[rowIndex][RHO_COL] = risk.getRho();
			
			double[] newRisk = calculateTotalRisk();
			data[data.length - 1][DELTA_COL] = newRisk[0];
			data[data.length - 1][GAMMA_COL] = newRisk[1];
			data[data.length - 1][VEGA_COL] = newRisk[2];
			data[data.length - 1][THETA_COL] = newRisk[3];
			data[data.length - 1][RHO_COL] = newRisk[4];
			
			//fireTableCellUpdated(rowIndex, 6);
			//fireTableCellUpdated(rowIndex, 7);
			//fireTableCellUpdated(rowIndex, 8);
			//fireTableCellUpdated(rowIndex, 10);
			//fireTableCellUpdated(rowIndex, 11);
			fireTableRowsUpdated(rowIndex, rowIndex);
			fireTableRowsUpdated(data.length -1, data.length -1);
			
		}
	}

	// AbstractTableModel overrides
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
		synchronized(lock) {
			NumberFormat formatter = null;
			if (rowIndex != data.length - 1) {
				switch (columnIndex) {
					case ID_COL:
					case SEC_COL:
						return data[rowIndex][columnIndex];
					case SIZE_COL:
					case OPEN_COL:
					case LATEST_COL:
						formatter = NumberFormat.getNumberInstance();
						formatter.setMinimumFractionDigits(1);
						formatter.setMaximumFractionDigits(2);
						return formatter.format(data[rowIndex][columnIndex]);
					case IV_COL:
						formatter = NumberFormat.getPercentInstance();
						formatter.setMinimumFractionDigits(1);
						formatter.setMaximumFractionDigits(2);
						return formatter.format(data[rowIndex][columnIndex]);
					default:
						formatter = NumberFormat.getNumberInstance();
						formatter.setMinimumFractionDigits(2);
						formatter.setMaximumFractionDigits(2);
						return formatter.format(data[rowIndex][columnIndex]);
				}	
			} else {
				switch (columnIndex) {		
					case PNL_COL:
					case DELTA_COL:
					case GAMMA_COL:
					case VEGA_COL:
					case THETA_COL:
					case RHO_COL:
						formatter = NumberFormat.getNumberInstance();
						formatter.setMinimumFractionDigits(2);
						formatter.setMaximumFractionDigits(2);
						return formatter.format(data[rowIndex][columnIndex]);
					default:
						return data[rowIndex][columnIndex];
				
				}	
			}
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
