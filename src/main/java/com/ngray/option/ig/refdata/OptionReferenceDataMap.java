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
}
