package com.ngray.option.volatilitysurface;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ngray.option.RiskEngine;
import com.ngray.option.data.HistoricalPriceCache;
import com.ngray.option.financialinstrument.EuropeanOption.Type;
import com.ngray.option.ig.refdata.OptionReferenceData;
import com.ngray.option.ig.refdata.OptionReferenceDataMap;
import com.ngray.option.mongo.Price;
import com.ngray.option.mongo.VolatilitySurfaceDefinition;

public class VolatilitySurfaceDataSetBuilder {
	
	private VolatilitySurfaceDefinition definition;
	
	public VolatilitySurfaceDataSetBuilder(VolatilitySurfaceDefinition definition) {
		this.definition = definition;
	}
	
	public VolatilitySurfaceDataSet build(LocalDate valueDate) throws VolatilitySurfaceException {
		if (definition == null) {
			throw new VolatilitySurfaceException("VolatilitySurfaceDataSetBuilder has null VolatilitySurfaceDefinition");
		}
		if (definition.getValidFrom().compareTo(valueDate) > 0 ||
			(definition.getValidTo() != null && definition.getValidTo().compareTo(valueDate) < 0)) {
			throw new VolatilitySurfaceException("Value date for vol surface incompatible with vol surface definition");
		}
		
		return build(valueDate, definition);
	}
	
	private VolatilitySurfaceDataSet build(LocalDate valueDate, VolatilitySurfaceDefinition definition) {
		HistoricalPriceCache priceCache = RiskEngine.getHistoricalPriceCache();
		boolean readThrough = true;
		
		Map<String, Price> underlyingPrices = 
				definition.getUnderlyingEpics().stream()
									   		   .map(epic -> priceCache.getPrice(valueDate, epic, readThrough))
									   		   .collect(Collectors.toMap(price -> price.getId(), Function.<Price>identity()));
		
		Map<String, Set<String>> optionEpics = new HashMap<>();	
		List<Double> strikeOffsets = definition.getStrikeOffsets();
	
		definition.getUnderlyingEpics().forEach(
				underlyingEpic -> optionEpics.put(
						underlyingEpic,  
						getOptionEpics(
								getATMStrike(underlyingEpic, underlyingPrices.get(underlyingEpic).getClose()), 
								strikeOffsets, 
								definition.getOptionEpicForm(underlyingEpic))
						)
				);
		
		
		
		Map<String, Price> optionPrices = new HashMap<>();
		optionEpics.forEach(		
				(underlyingEpic, optionEpicSet) -> optionEpicSet.forEach(
					optionEpic ->  { optionPrices.put(
							optionEpic, RiskEngine.getHistoricalPriceCache().getPrice(valueDate, optionEpic, readThrough)
							); }
					)
				);
		
		return new VolatilitySurfaceDataSet(valueDate, underlyingPrices, optionEpics, optionPrices, definition);		
	}
				
	private Set<String> getOptionEpics(double atm, List<Double> strikeOffsets, String optionEpicForm) {
		
		 Set<Double> strikes = strikeOffsets.stream().map(offset -> offset + atm).collect(Collectors.toSet());
		 Set<String> optionEpics = strikes.stream()
										  .map(strike -> optionEpicForm.replace("{STRIKE}", Integer.toString(strike.intValue())))
										  .collect(Collectors.toSet());
		 return optionEpics;
	}
	
	private double getATMStrike(String underlyingEpic, double underlyingPrice) {
		List<OptionReferenceData> optRefDataList = OptionReferenceDataMap.getOptionReferenceDataForUnderlying(underlyingEpic);
		NavigableSet<Double> optRefDataSet = optRefDataList.stream()
														   .map(optRefData -> optRefData.getStrike())
														   .collect(Collectors.toCollection(()->new TreeSet<Double>()));
		
		double lowATM = optRefDataSet.floor(underlyingPrice);
		double highATM = optRefDataSet.ceiling(underlyingPrice);
		double atmStrike = 0.0;
		
		if (Double.compare(lowATM, highATM) == 0) {
			atmStrike = lowATM;
		}
		else if (Double.compare(underlyingPrice - lowATM, highATM - underlyingPrice) < 0) {
			atmStrike = lowATM;
		} else if (Double.compare(underlyingPrice - lowATM, highATM - underlyingPrice) > 0) {
			atmStrike = highATM;
		} else if (Double.compare(underlyingPrice - lowATM, highATM - underlyingPrice) == 0) {
			if (definition.getCallOrPut() == Type.CALL) {
				atmStrike = lowATM;
			} else {
				atmStrike = highATM;
			}
		}
		return atmStrike;
	}

}
