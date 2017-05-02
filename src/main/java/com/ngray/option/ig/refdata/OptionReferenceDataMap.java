package com.ngray.option.ig.refdata;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ngray.option.financialinstrument.EuropeanOption.Type;
import com.ngray.option.financialinstrument.Security;
import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionException;
import com.ngray.option.ig.market.Market;
import com.ngray.option.ig.market.MarketNode;
import com.ngray.option.ig.rest.RestAPIGet;
import com.ngray.option.ig.rest.RestAPIResponse;

public class OptionReferenceDataMap {
	
	/**
	 * Map option instrumentName to static reference data
	 */
	private final static Map<String, OptionReferenceData> referenceData = new HashMap<>();
	
	/**
	 * Retrieve the ref data for the specified option. Will return null if not present
	 * @param optionName
	 * @return
	 */
	public static OptionReferenceData getOptionReferenceData(String optionName) throws MissingReferenceDataException {
		if(!referenceData.containsKey(optionName)) {
			throw new MissingReferenceDataException("No reference data for " + optionName);
		}
		return referenceData.get(optionName);
	}
	
	/**
	 * Initialize the reference data. For now this is hardcoded - should load from file
	 * @throws SessionException 
	 */
	public static void init(Session session) throws SessionException {
		
		// Gold underlying
		RestAPIGet get = new RestAPIGet("/marketnavigation/104139");
		RestAPIResponse response = get.execute(session);
		String json = response.getResponseBodyAsJson();
		MarketNode node = MarketNode.fromJson(json);
		Map<String, Market> markets = node.getMarkets().stream().collect(Collectors.toMap(Market::getInstrumentName, Function.identity()));
		Security underlying = new Security(markets.get("Gold"));
		// Gold futures options
		referenceData.put("Gold Futures 1285 PUT", new OptionReferenceData("Gold Futures 1285 PUT", underlying, 1285.0, LocalDate.of(2017, 5, 25), Type.PUT));
		referenceData.put("Gold Futures 1290 CALL", new OptionReferenceData("Gold Futures 1285 PUT", underlying, 1290.0, LocalDate.of(2017, 5, 25), Type.CALL));
	}

}
