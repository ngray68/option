package com.ngray.option.risk;

import com.ngray.option.financialinstrument.FinancialInstrument;

public interface RiskListener {
	
	public void onRiskUpdate(FinancialInstrument instrument, Risk risk);

}
