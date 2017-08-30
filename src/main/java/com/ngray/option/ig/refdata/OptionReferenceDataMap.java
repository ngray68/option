package com.ngray.option.ig.refdata;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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

public class OptionReferenceDataMap {
	
	/**
	 * Map option instrumentName to static reference data
	 */
	private final static Map<String, OptionReferenceData> referenceData = new HashMap<>();
	
	/**
	 * Map underlying to list of option reference data
	 */
	private final static Map<Security, List<OptionReferenceData>> referenceDataByUnderlying = new HashMap<>();;
	
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
	 * Get all the underlyings for which we have option reference data
	 * @return
	 */
	public static List<Security> getUnderlyings() {
		return new ArrayList<>(referenceDataByUnderlying.keySet());			
	}
	
	/**
	 * Get all option reference data relating to the specified underlying
	 * @param underlying
	 * @return
	 */
	public static List<OptionReferenceData> getOptionReferenceData(Security underlying) {
		if (!referenceDataByUnderlying.containsKey(underlying)) {
			return Collections.emptyList();
		}
		
		return referenceDataByUnderlying.get(underlying);
	}
	
	
	public static OptionReferenceData getOptionReferenceData(String underlyingId, double strike, Type callOrPut) {
		List<OptionReferenceData> refDataList = getOptionReferenceDataForUnderlying(underlyingId);
		Optional<OptionReferenceData> optionRefData = refDataList.stream()
				   .filter(refData -> Double.compare(refData.getStrike(), strike) == 0 && refData.getCallOrPut() == callOrPut)
				   .findFirst();
		
		if (optionRefData.isPresent())  {
			return optionRefData.get();
		}
		
		return null;
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
			// TODO: read these from config
			String[] marketUris = { "Indices/UK/FTSE 100",
									"Forex/Near Quarter/Major FX",
									"Forex/Far Quarter/Major FX",
									"Commodities Metals Energies/Metals/Gold"
								  };
			for (String marketUri : marketUris) {
				List<Market> markets = getUnderlyingMarkets(marketUri, session);
				underlyings.putAll(markets.stream().collect(Collectors.toMap(Market::getEpic, Function.identity())));
			}
			
			List<Map<String, String>> refDataList = null;
			if (fromResource == true) {
				refDataList = OptionReferenceDataLoader.loadFromResources(filename);
			} else {
				refDataList = OptionReferenceDataLoader.loadFromFile(filename);
			}
			refDataList.forEach(
					(entry) -> {
						EuropeanOption.Type callOrPut = entry.get(Attribute.CallOrPut.toString()).equals("PUT") ? Type.PUT : Type.CALL;
						double strike = Double.parseDouble(entry.get(Attribute.Strike.toString()));
						LocalDate expiry = parseReferenceDataExpiryDate(entry.get(Attribute.Expiry.toString()));//LocalDate.parse(entry.get(Attribute.Expiry.toString()));
						double dividendYield = Double.parseDouble(entry.get(Attribute.DividendYield.toString()));
						double riskFreeRate = Double.parseDouble(entry.get(Attribute.RiskFreeRate.toString()));
						Market underlyingMarket = underlyings.get(entry.get(Attribute.UnderlyingEpic.toString()));
						if (underlyingMarket != null) {
							Security underlying = new Security(underlyingMarket);
							OptionReferenceData data = 
									new OptionReferenceData(entry.get(Attribute.OptionEpic.toString()), underlying, strike, expiry, callOrPut, dividendYield, riskFreeRate);
							referenceData.put(entry.get(Attribute.OptionEpic.toString()), data);
							
							if (!referenceDataByUnderlying.containsKey(underlying)) {
								referenceDataByUnderlying.put(underlying, new ArrayList<>());
							}
							
							referenceDataByUnderlying.get(underlying).add(data);
						} else {
							Log.getLogger().info(underlyings.get(entry.get(Attribute.UnderlyingEpic.toString())) + " doesn't exist (eg. expired) - ignoring all options with this underlying"); 
						}
					}
					);
		} catch (IOException | MissingReferenceDataException | URISyntaxException e) {
			Log.getLogger().error(e.getMessage(), e);
			throw new SessionException(e.getMessage());
		}
	}
	
	public static void insert(String key, OptionReferenceData data) {
		referenceData.put(key, data);
	}
	
	private static LocalDate parseReferenceDataExpiryDate(String expiry) {
		switch (expiry) {
		case "DAILY":
			return LocalDate.now().plusDays(1);
		case "WEEKLY":
			int daysToAdd = DayOfWeek.SATURDAY.getValue() - LocalDate.now().getDayOfWeek().getValue();
			return LocalDate.now().plusDays(daysToAdd);
		default:
			return LocalDate.parse(expiry);
		}
	}
	/*
	 * Return a list of IG market objects given a uri of the form eg.
	 * /Indices/UK/FTSE 100
	 * This allows us to specify underlyings in configuration and avoid hardcoding node ids
	 */
	private static List<Market> getUnderlyingMarkets(String uri, Session session) throws SessionException {
		
		if (uri == null) {
			throw new SessionException("Null Market URI");
		}
		
		return getUnderlyingMarkets(uri, MarketNode.getRootNode(session), session);
	}
	
	private static List<Market> getUnderlyingMarkets(String uri, MarketNode node, Session session) {	
		String[] uriTokens = uri.split("/");
		for (String nextToken : uriTokens) {
			if (!node.getSubNodes().isEmpty()) {
				for (MarketNode subNode : node.getSubNodes()) {
					 if (subNode.getName().equals(nextToken)) {
						 subNode.getSubNodesAndMarkets(session);
						 node = subNode;
						 // this is just to make sure we don't breach the request limits
						 // unfortunately it makes initialization quite slow
						 try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						 break;
					 }
				}
			} 
		}
		return node.getMarkets();
	}

	public static List<OptionReferenceData> getOptionReferenceDataForUnderlying(String underlyingEpic) {
		Security security = new Security(underlyingEpic);
		return getOptionReferenceData(security);
	}
}
