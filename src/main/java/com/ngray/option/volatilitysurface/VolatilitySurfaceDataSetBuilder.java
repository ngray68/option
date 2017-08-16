package com.ngray.option.volatilitysurface;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.ngray.option.Log;
import com.ngray.option.RiskEngine;
import com.ngray.option.data.HistoricalPriceCache;
import com.ngray.option.financialinstrument.EuropeanOption.Type;
import com.ngray.option.ig.refdata.OptionReferenceData;
import com.ngray.option.ig.refdata.OptionReferenceDataMap;
import com.ngray.option.mongo.Price;
import com.ngray.option.mongo.Price.SnapshotType;
import com.ngray.option.mongo.VolatilitySurfaceDefinition;
import com.ngray.option.volatilitysurface.VolatilitySurfaceDataSet.OptionData;

/**
 * Utility class which loads volatility surface data given a
 * VolatilitySurfaceDefinition and a value date.
 * @author nigelgray
 *
 */
public class VolatilitySurfaceDataSetBuilder {
	
	private final VolatilitySurfaceDefinition definition;
	
	public VolatilitySurfaceDataSetBuilder(VolatilitySurfaceDefinition definition) {
		this.definition = definition;
	}
	
	/**
	 * Build the data set. Will throw if the definition is inconsistent with the value date (assumes a
	 * valid definition). Will also throw if:
	 * 		i.  we cannot find a price for one or more of the underlying on the given value date
	 * 	    ii. we cannot find options for one or more of the underlyings
	 * 		iii.there missing option prices we will return an incomplete data set. 
	 * @param valueDate
	 * @return
	 * @throws VolatilitySurfaceException
	 */
	public VolatilitySurfaceDataSet build(LocalDate valueDate, SnapshotType snapshotType) throws VolatilitySurfaceException {
		Log.getLogger().info("Building VolatilitySurfaceDataSet: definition " + definition + ", value date " + valueDate);
		if (definition == null) {
			throw new VolatilitySurfaceException("VolatilitySurfaceDataSetBuilder has null VolatilitySurfaceDefinition");
		}
		if (definition.getValidFrom().compareTo(valueDate) > 0 ||
			(definition.getValidTo() != null && definition.getValidTo().compareTo(valueDate) < 0)) {
			throw new VolatilitySurfaceException("Value date for vol surface incompatible with vol surface definition");
		}
		
		return build(valueDate, snapshotType, definition);
	}
	
	private VolatilitySurfaceDataSet build(LocalDate valueDate, SnapshotType snapshotType, VolatilitySurfaceDefinition definition) throws VolatilitySurfaceException {
		HistoricalPriceCache priceCache = RiskEngine.getHistoricalPriceCache();
		boolean readThrough = true;
		
		// use a tree map to order by underlying epic value
		Map<String, Price> underlyingPrices = new TreeMap<>();
		definition.getUnderlyingEpics().forEach(
					epic -> underlyingPrices.put(epic, priceCache.getPrice(valueDate, epic, readThrough))
				);
		
		// validate that we have no null entries in the underlyingPrices map
		if (underlyingPrices.containsValue(null)) {
			throw new VolatilitySurfaceException("VolatilitySurfaceDataSetLoader: missing underlying price for " + definition.getName() + " on value date " + valueDate);
		}
		
		Map<String, Set<OptionData>> optionData = new TreeMap<>();	
		List<Double> strikeOffsets = definition.getStrikeOffsets();
	
		// underlyingPrices.get guaranteed to return an object
		definition.getUnderlyingEpics().forEach(
				underlyingEpic -> {
					try {
						optionData.put(
							underlyingEpic,  
							getOptionData(
									getATMStrike(underlyingEpic, underlyingPrices.get(underlyingEpic).getPrice(snapshotType)), 
									strikeOffsets, 
									definition.getOptionEpicForm(underlyingEpic))
							);
					} catch (VolatilitySurfaceException e) {
						Log.getLogger().error(e.getMessage(), e);
						optionData.put(underlyingEpic, null);
					} }
				);
		
		// validate the option epics
		if (optionData.containsValue(null)) {
			throw new VolatilitySurfaceException("VolatilitySurfaceDataSetLoader: could not find options for all underlyings in the definition");
		}
		
		// guaranteed here that optionEpics will have no null values
		Map<String, Price> optionPrices = new TreeMap<>();
		optionData.forEach(		
				(underlyingEpic, optionDataSet) -> optionDataSet.forEach(
					optionInfo->  { optionPrices.put(
							optionInfo.getOptionId(), RiskEngine.getHistoricalPriceCache().getPrice(valueDate, optionInfo.getOptionId(), readThrough)
							); }
					)
				);
		
		// if we are missing prices we throw
		if (optionPrices.containsValue(null)) {
			throw new VolatilitySurfaceException("VolatilitySurfaceDataSetLoader: could not find option prices for all options in the definition");
		}
		
		return new VolatilitySurfaceDataSet(valueDate, underlyingPrices, optionData, optionPrices, definition);		
	}
				
	private Set<OptionData> getOptionData(double atm, List<Double> strikeOffsets, String optionEpicForm) {
		
		Set<OptionData> optionData = new TreeSet<>();
		strikeOffsets.forEach(
				offset -> {
					Double strike = atm + offset;
					String id = optionEpicForm.replace("{STRIKE}", Integer.toString(strike.intValue()));
					optionData.add(new OptionData(id, offset));
				}
			);
		
		 return optionData;
	}
	
	private double getATMStrike(String underlyingEpic, double underlyingPrice) throws VolatilitySurfaceException {
		List<OptionReferenceData> optRefDataList = OptionReferenceDataMap.getOptionReferenceDataForUnderlying(underlyingEpic);
		// will have an empty list if no options for this underlying
		if (optRefDataList.isEmpty()) {
			throw new VolatilitySurfaceException("No options defined in Option Reference Data for " + underlyingEpic);
		}
		
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
