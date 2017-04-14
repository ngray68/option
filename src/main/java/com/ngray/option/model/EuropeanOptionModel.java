package com.ngray.option.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.marketdata.MarketData;
import com.ngray.option.marketdata.MarketDataCollection;
import com.ngray.option.marketdata.MarketDataException;
import com.ngray.option.marketdata.MarketData.Type;

public class EuropeanOptionModel implements Model {

	public EuropeanOptionModel() {
	}

	@Override
	public Risk calculateRisk(FinancialInstrument instrument, MarketDataCollection marketData, LocalDate valueDate) throws ModelException {
		if(instrument == null) {
			throw new ModelException("EuropeanOptionModel::calculateRisk called with null FinancialInstrument");
		}
		
		if(marketData == null) {
			throw new ModelException("EuropeanOptionModel::calculateRisk called with null MarketData");
		}
		
		if (!(instrument instanceof EuropeanOption)) {
			throw new ModelException("EuropeanOptionModel can't value security " + instrument.getIdentifier() +
										" of type " + instrument.getClass());
		}
		
		EuropeanOption option = (EuropeanOption)instrument;		
		switch (option.getType()) {
		case CALL:
			return calculateRiskForCall(option, marketData, valueDate);
		case PUT:
			return calculateRiskForPut(option, marketData, valueDate);
		default:
			// will never get here because we can't create an option without a type
			return null;
		}
	}

	private Risk calculateRiskForPut(EuropeanOption option, MarketDataCollection marketData, LocalDate valueDate) throws ModelException {
		try {
			double strike = option.getStrike();
			long daysToExpiry = ChronoUnit.DAYS.between(valueDate, option.getExpiryDate());
			double timeToExpiry = ((double)daysToExpiry)/365.0;
			
			// TODO
			double dividendYield = 0.0; 
			double riskFreeRate = 0.0025;
			
			MarketData spotMarketData = marketData.getMarketData(option.getUnderlying());
			MarketData optionPriceOrVol = marketData.getMarketData(option);
			
			if (spotMarketData.getType() != Type.PRICE) {
				throw new ModelException("");
			}
			double spot = spotMarketData.getValue();
			
			double volatility = 0.0;
			double optionPrice = 0.0;
			if (optionPriceOrVol.getType() == Type.VOLATILITY) {
				volatility = optionPriceOrVol.getValue();
				optionPrice = BlackScholesModel.calcPutOptionPrice(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
			} else {
				optionPrice = optionPriceOrVol.getValue();
				 volatility = BlackScholesModel.calcPutOptionImpliedVol(spot, strike, optionPrice, timeToExpiry, riskFreeRate, dividendYield);
			}
			
			double delta = BlackScholesModel.calcPutOptionDelta(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
			double gamma = BlackScholesModel.calcOptionGamma(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
			double vega = BlackScholesModel.calcOptionVega(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
			double theta = BlackScholesModel.calcPutOptionTheta(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
			double rho = BlackScholesModel.calcPutOptionRho(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
			return new Risk(optionPrice, delta, gamma, vega, theta, rho, volatility);
		} catch (MarketDataException e) {
			throw new ModelException(e.getMessage());
		}
	}

	private Risk calculateRiskForCall(EuropeanOption option, MarketDataCollection marketData, LocalDate valueDate) throws ModelException {
		try {
			double strike = option.getStrike();
			long daysToExpiry = ChronoUnit.DAYS.between(valueDate, option.getExpiryDate());
			double timeToExpiry = ((double)daysToExpiry)/365.0;
			
			// TODO
			double dividendYield = 0.0; 
			double riskFreeRate = 0.0025;
			
			MarketData spotMarketData = marketData.getMarketData(option.getUnderlying());
			MarketData optionPriceOrVol = marketData.getMarketData(option);
			
			if (spotMarketData.getType() != Type.PRICE) {
				throw new ModelException("");
			}
			double spot = spotMarketData.getValue();
			
			double volatility = 0.0;
			double optionPrice = 0.0;
			if (optionPriceOrVol.getType() == Type.VOLATILITY) {
				volatility = optionPriceOrVol.getValue();
				optionPrice = BlackScholesModel.calcCallOptionPrice(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
			} else {
				optionPrice = optionPriceOrVol.getValue();
				 volatility = BlackScholesModel.calcCallOptionImpliedVol(spot, strike, optionPrice, timeToExpiry, riskFreeRate, dividendYield);
			}
			
			double delta = BlackScholesModel.calcCallOptionDelta(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
			double gamma = BlackScholesModel.calcOptionGamma(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
			double vega = BlackScholesModel.calcOptionVega(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
			double theta = BlackScholesModel.calcCallOptionTheta(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
			double rho = BlackScholesModel.calcCallOptionRho(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		return new Risk(optionPrice, delta, gamma, vega, theta, rho, volatility);
		} catch (MarketDataException e) {
			throw new ModelException(e.getMessage());
		}
	}

}
