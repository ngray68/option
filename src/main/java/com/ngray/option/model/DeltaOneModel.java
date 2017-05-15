package com.ngray.option.model;

import java.time.LocalDate;

import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.financialinstrument.Security;
import com.ngray.option.marketdata.MarketData;
import com.ngray.option.marketdata.MarketData.Type;
import com.ngray.option.risk.Risk;
import com.ngray.option.marketdata.MarketDataCollection;
import com.ngray.option.marketdata.MarketDataException;

public class DeltaOneModel implements Model {

	@Override
	public Risk calculateRisk(FinancialInstrument instrument, MarketDataCollection marketData, LocalDate valueDate)
			throws ModelException {
		if(instrument == null) {
			throw new ModelException("DeltaOneModel::calculateRisk called with null FinancialInstrument");
		}
		
		if(marketData == null) {
			throw new ModelException("DeltaOneModel::calculateRisk called with null MarketData");
		}
		
		if (!(instrument instanceof Security)) {
			throw new ModelException("DeltaOneModel can't value security " + instrument.getIdentifier() +
										" of type " + instrument.getClass());
		}
		
		try {
			MarketData price = marketData.getMarketData(instrument);
			if (!(price.getType() == Type.PRICE)) {
				throw new ModelException("Market data must be a price for delta one security");
			}
			return new Risk(price.getMid(), 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, price.getMid(), price.getMid());
		} catch (MarketDataException e) {
			throw new ModelException(e.getMessage());
		}
	}

}
