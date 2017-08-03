package com.ngray.option.ui;

import java.text.NumberFormat;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.ngray.option.Log;
import com.ngray.option.analysis.scenario.Scenario;
import com.ngray.option.analysis.scenario.ScenarioListener;
import com.ngray.option.analysis.scenario.ScenarioService;
import com.ngray.option.position.Position;
import com.ngray.option.ui.ScenarioView.RiskMeasure;

@SuppressWarnings("serial")
public class ScenarioTableModel extends AbstractTableModel implements ScenarioListener {

	private final RiskMeasure riskMeasure;
	
	private final Scenario scenario;
	
	private Object[] columnNames;
	
	private Object[][] data;
	
	
	public ScenarioTableModel(Scenario scenario, RiskMeasure riskMeasure) {
		this.scenario = scenario;
		this.riskMeasure = riskMeasure;
		initialize();
	}
	
	private void initialize() {
		int rows = scenario.getBasePositions().size() + 1;  // +1 for total
		int cols = scenario.getDefinition().getValues().length + 2;
		columnNames = new Object[scenario.getDefinition().getValues().length + 2];
		columnNames[0] = "Last Update";
		columnNames[1] = "Id";
		for (int i = 2; i < columnNames.length; ++i) {
			columnNames[i] = scenario.getDefinition().getValues()[i-2];
		}
		data = new Object[rows][cols];
		Object[] total = new Object[cols];
		total[0] = "Total";
		total[1] = "";
		int i = 0;
		for (Position basePosition : scenario.getBasePositions()) {
			int j = 2;
			data[i][0] = basePosition.getTimestamp();
			data[i][1] = basePosition.getInstrument().getIGMarket().getInstrumentName() + " " + basePosition.getInstrument().getIGMarket().getExpiry();
 			List<Position> results = scenario.getScenarioResult().getPerturbedPosition(basePosition);
			for (Position perturbedPosition : results) {
				data[i][j] = getRiskMeasure(perturbedPosition);
				if (total[j] == null) {
					total[j] = (double)data[i][j];
				} else {
					total[j] = (double)total[j] + (double)data[i][j];
				}
				++j;
			}
			++i;
		}
		
		data[i] = total;
	}
	
	private Double getRiskMeasure(Position perturbedPosition) {
		switch (riskMeasure) {
		case PNL:
			return perturbedPosition.getPositionPnL();
		case DELTA:
			return perturbedPosition.getPositionRisk().getDelta();
		case GAMMA:
			return perturbedPosition.getPositionRisk().getGamma();
		case VEGA:
			return perturbedPosition.getPositionRisk().getVega();
		case THETA:
			return perturbedPosition.getPositionRisk().getTheta();
		case RHO:
			return perturbedPosition.getPositionRisk().getRho();
		default:
			return Double.NaN;
		}
	}

	@Override
	public int getRowCount() {
		return data.length;
	}

	@Override
	public int getColumnCount() {
		return scenario.getDefinition().getValues().length + 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex < 2) {
			return data[rowIndex][columnIndex];
		}
		
		NumberFormat formatter = null;
		switch (riskMeasure) {
		case PNL:
			formatter = NumberFormat.getNumberInstance();
			formatter.setMinimumFractionDigits(1);
			formatter.setMaximumFractionDigits(2);
			return formatter.format(data[rowIndex][columnIndex]);
		case DELTA:
		case VEGA:
		case THETA:
		case RHO:
			formatter = NumberFormat.getNumberInstance();
			formatter.setMinimumFractionDigits(2);
			formatter.setMaximumFractionDigits(2);
			return formatter.format(data[rowIndex][columnIndex]);		
		case GAMMA:
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
		return columnNames[col].toString();
    }

	@Override
	public void onUpdate(String key, Scenario scenario) {
		Log.getLogger().debug("ScenarioTableModel::onUpdate called for scenario " + key);
		if (scenario.getName() == this.scenario.getName()) {
			// we reconstruct the whole table
			Log.getLogger().debug("Updating ScenarioTableModel for scenario " + key);
			initialize();
			fireTableDataChanged();
		}
	}

}
