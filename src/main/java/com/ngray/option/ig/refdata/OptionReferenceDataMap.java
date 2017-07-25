package com.ngray.option.ig.refdata;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import com.ngray.option.financialinstrument.EuropeanOption.Type;
import com.ngray.option.Log;
import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.Security;
import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionException;
import com.ngray.option.ig.market.Market;
import com.ngray.option.ig.market.MarketNode;
import com.ngray.option.ig.refdata.OptionReferenceData.Attribute;
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
	 * Initialise reference data from file
	 * Underlyings still hard-coded to some extent for now
	 * @param filename
	 * @param session
	 * @param fromResource 
	 * @throws SessionException
	 * @throws URISyntaxException 
	 */
	public static void init(String filename, Session session, boolean fromResource) throws SessionException {
		try {
			Map<String, Market> underlyings = new HashMap<>();
			if (session.getIsLive()) {
				// Gold
				RestAPIGet get = new RestAPIGet("/marketnavigation/93613");
				RestAPIResponse response = get.execute(session);
				String json = response.getResponseBodyAsJson();
				MarketNode node = MarketNode.fromJson(json);
				Map<String, Market> markets = node.getMarkets().stream().collect(Collectors.toMap(Market::getEpic, Function.identity()));
				underlyings.putAll(markets);
				
				// FTSE
				RestAPIGet get2 = new RestAPIGet("/marketnavigation/93334");
				RestAPIResponse response2 = get2.execute(session);
				String json2 = response2.getResponseBodyAsJson();
				MarketNode node2 = MarketNode.fromJson(json2);
				Map<String, Market> markets2 = node2.getMarkets().stream().collect(Collectors.toMap(Market::getEpic, Function.identity()));
				underlyings.putAll(markets2);
				
				// Major FX 
				RestAPIGet get3 = new RestAPIGet("/marketnavigation/191731");
				RestAPIResponse response3 = get3.execute(session);
				String json3 = response3.getResponseBodyAsJson();
				MarketNode node3 = MarketNode.fromJson(json3);
				Map<String, Market> markets3 = node3.getMarkets().stream().collect(Collectors.toMap(Market::getEpic, Function.identity()));
				underlyings.putAll(markets3);	
	
			} else {
				//Gold
				RestAPIGet get = new RestAPIGet("/marketnavigation/104139");
				RestAPIResponse response = get.execute(session);
				String json = response.getResponseBodyAsJson();
				MarketNode node = MarketNode.fromJson(json);
				Map<String, Market> markets = node.getMarkets().stream().collect(Collectors.toMap(Market::getEpic, Function.identity()));
				underlyings.putAll(markets);
				
				// FTSE
				RestAPIGet get2 = new RestAPIGet("/marketnavigation/97605");
				RestAPIResponse response2 = get2.execute(session);
				String json2 = response2.getResponseBodyAsJson();
				MarketNode node2 = MarketNode.fromJson(json2);
				Map<String, Market> markets2 = node2.getMarkets().stream().collect(Collectors.toMap(Market::getEpic, Function.identity()));
				underlyings.putAll(markets2);
				
				// Major FX
				RestAPIGet get3 = new RestAPIGet("/marketnavigation/425881");
				RestAPIResponse response3 = get3.execute(session);
				String json3 = response3.getResponseBodyAsJson();
				MarketNode node3 = MarketNode.fromJson(json3);
				Map<String, Market> markets3 = node3.getMarkets().stream().collect(Collectors.toMap(Market::getEpic, Function.identity()));
				underlyings.putAll(markets3);		
			}
			
			List<Map<String, String>> refDataList = null;
			if (fromResource == true) {
				refDataList = OptionReferenceDataLoader.loadFromResources(filename);
				//refDataList = OptionReferenceDataLoader.loadFromResource(filename);
			} else {
				refDataList = OptionReferenceDataLoader.loadFromFile(filename);
			}
			refDataList.forEach(
					(entry) -> {
						EuropeanOption.Type callOrPut = entry.get(Attribute.CallOrPut.toString()).equals("PUT") ? Type.PUT : Type.CALL;
						double strike = Double.parseDouble(entry.get(Attribute.Strike.toString()));
						LocalDate expiry = LocalDate.parse(entry.get(Attribute.Expiry.toString()));
						double dividendYield = Double.parseDouble(entry.get(Attribute.DividendYield.toString()));
						double riskFreeRate = Double.parseDouble(entry.get(Attribute.RiskFreeRate.toString()));
						Market underlyingMarket = underlyings.get(entry.get(Attribute.UnderlyingEpic.toString()));
						if (underlyingMarket != null) {
							Security underlying = new Security(underlyingMarket);
							OptionReferenceData data = 
									new OptionReferenceData(entry.get(Attribute.OptionEpic.toString()), underlying, strike, expiry, callOrPut, dividendYield, riskFreeRate);
							referenceData.put(entry.get(Attribute.OptionEpic.toString()), data);
						} else {
							Log.getLogger().info(underlyings.get(entry.get(Attribute.UnderlyingEpic.toString())) + " doesn't exist (eg. expired) - ignoring all options with this underlying"); 
						}
					}
					);
		} catch (IOException | MissingReferenceDataException | URISyntaxException e) {
			throw new SessionException(e.getMessage());
		}
	}
	
	public static void insert(String key, OptionReferenceData data) {
		referenceData.put(key, data);
	}
/*	
	/**
	 * Initialize the reference data. For now this is hardcoded - should load from file
	 * @throws SessionException 
	 
	public static void init(Session session) throws SessionException {
		
		// Gold underlying
		if (!session.getIsLive()) {
			RestAPIGet get = new RestAPIGet("/marketnavigation/104139");
			RestAPIResponse response = get.execute(session);
			String json = response.getResponseBodyAsJson();
			MarketNode node = MarketNode.fromJson(json);
			Map<String, Market> markets = node.getMarkets().stream().collect(Collectors.toMap(Market::getEpic, Function.identity()));
			Security underlying = new Security(markets.get("MT.D.GC.MONTH1.IP"));
			// Gold futures options
			referenceData.put("OP.D.GC1.1285P.IP", new OptionReferenceData("OP.D.GC1.1285P.IP", underlying, 1285.0, LocalDate.of(2017, 5, 25), Type.PUT));
			referenceData.put("OP.D.GC1.1290C.IP", new OptionReferenceData("OP.D.GC1.1290C.IP", underlying, 1290.0, LocalDate.of(2017, 5, 25), Type.CALL));
					
			RestAPIGet get2 = new RestAPIGet("/marketnavigation/97605");
			RestAPIResponse response2 = get2.execute(session);
			String json2 = response2.getResponseBodyAsJson();
			MarketNode node2 = MarketNode.fromJson(json2);
			Map<String, Market> markets2 = node2.getMarkets().stream().collect(Collectors.toMap(Market::getEpic, Function.identity()));
			Security underlying2 = new Security(markets2.get("IX.D.FTSE.MONTH1.IP"));
			// FTSE options
			referenceData.put("OP.D.FTSE6.7325P.IP", new OptionReferenceData("OP.D.FTSE6.7325P.IP", underlying2, 7325.0, LocalDate.of(2017, 6, 16), Type.PUT));
			referenceData.put("OP.D.FTSE6.7350C.IP", new OptionReferenceData("OP.D.FTSE6.7350C.IP", underlying2, 7350.0, LocalDate.of(2017, 6, 16), Type.CALL));
		} else {
			RestAPIGet get = new RestAPIGet("/marketnavigation/93613");
			RestAPIResponse response = get.execute(session);
			String json = response.getResponseBodyAsJson();
			MarketNode node = MarketNode.fromJson(json);
			Map<String, Market> markets = node.getMarkets().stream().collect(Collectors.toMap(Market::getEpic, Function.identity()));
			Security underlying = new Security(markets.get("MT.D.GC.MONTH1.IP"));
			// Gold futures options
			referenceData.put("OP.D.GC1.1285P.IP", new OptionReferenceData("OP.D.GC1.1285P.IP", underlying, 1285.0, LocalDate.of(2017, 5, 25), Type.PUT));
			referenceData.put("OP.D.GC1.1290C.IP", new OptionReferenceData("OP.D.GC1.1290C.IP", underlying, 1290.0, LocalDate.of(2017, 5, 25), Type.CALL));
		
			RestAPIGet get2 = new RestAPIGet("/marketnavigation/93334");
			RestAPIResponse response2 = get2.execute(session);
			String json2 = response2.getResponseBodyAsJson();
			MarketNode node2 = MarketNode.fromJson(json2);
			Map<String, Market> markets2 = node2.getMarkets().stream().collect(Collectors.toMap(Market::getEpic, Function.identity()));
			Security underlying2 = new Security(markets2.get("IX.D.FTSE.MONTH1.IP"));
			// FTSE options
			referenceData.put("OP.D.FTSE6.7325P.IP", new OptionReferenceData("OP.D.FTSE6.7325P.IP", underlying2, 7325.0, LocalDate.of(2017, 6, 16), Type.PUT));
			referenceData.put("OP.D.FTSE6.7350C.IP", new OptionReferenceData("OP.D.FTSE6.7350C.IP", underlying2, 7350.0, LocalDate.of(2017, 6, 16), Type.CALL));
		}

	}
	*/

}
