package com.ngray.option.volatilitysurface;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.ngray.option.Log;
import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.marketdata.MarketData;
import com.ngray.option.marketdata.MarketData.Type;
import com.ngray.option.mongo.Price;
import com.ngray.option.mongo.VolatilitySurfaceDefinition;

import static com.mongodb.client.model.Filters.*;

/**
 * Implements VolatilitySurfaceDataSetLoader to load a VolatilitySurfaceData set from a MongoDB instance
 * @author nigelgray
 *
 */
public class MongoVolatilitySurfaceDataSetLoader implements VolatilitySurfaceDataSetLoader {
	
	public static final String OPTION_DB = "optiondb";
	public static final String PRICES = "prices";
	
	private MongoCollection<Price> prices;
	
	public MongoVolatilitySurfaceDataSetLoader(MongoDatabase database) {
		prices = database.getCollection(PRICES, Price.class);
	}

	@Override
	public Map<String, MarketData> getUnderlyingPrices(LocalDate valueDate, VolatilitySurfaceDefinition definition) {
		
		Log.getLogger().info("MongoDB optiondb..prices retrieving underlying prices for " + definition.getName());
		return getPrices(valueDate, definition.getUnderlyingEpics());
	}

	@Override
	public Map<String, Set<String>> getOptions(LocalDate valueDate, VolatilitySurfaceDefinition definition) {
		Map<String, Set<String>> options = new HashMap<>();
		Map<String, MarketData> underlyingPrices = getUnderlyingPrices(valueDate, definition);
		underlyingPrices.forEach((underlyingId, marketData) -> { options.put(underlyingId, getOptionIds(underlyingId, marketData.getMid(), definition)); });	
		return options;
	}

	@Override
	public Map<String, MarketData> getOptionPrices(LocalDate valueDate, VolatilitySurfaceDefinition definition) {
		Log.getLogger().info("MongoDB optiondb..prices retrieving option prices for " + definition.getName());
		Map<String, Set<String>> options = getOptions(valueDate, definition);
		Map<String, MarketData> optionPrices = new HashMap<>();
				options.forEach((underlyingId, optionIdSet) -> optionPrices.putAll(getPrices(valueDate, optionIdSet)));
		return optionPrices;
	}

	private Map<String, MarketData> getPrices(LocalDate valueDate, Iterable<String> identifiers) {
		Map<String, MarketData> priceMap = new HashMap<>();
		FindIterable<Price> priceIter = prices.find(and(eq("ValueDate", valueDate), in("Id", identifiers)));
		MongoCursor<Price> iter = priceIter.iterator();
		while (iter.hasNext()) {
			Price price = iter.next();
			priceMap.put(price.getId(), new MarketData(price.getId(), price.getClose(), Type.PRICE));
		}
		
		identifiers.forEach(
			 epic -> { if (!priceMap.containsKey(epic)) {
				 			Log.getLogger().warn("VolatilitySurfaceDataSetLoader: missing price for " + epic + " on value date " + valueDate);
			 		   }
			 } );
		return priceMap;
		
	}
	private Set<String> getOptionIds(String underlyingId, double underlyingPrice, VolatilitySurfaceDefinition definition) {
		double atmStrike = getAtmStrike(underlyingPrice, definition);
		List<Double> strikeOffsets = definition.getStrikeOffsets();
		String optionEpicForm = definition.getOptionEpicForm(underlyingId);
		Set<String> optionEpics = new TreeSet<>(); // we want it to be ordered
		String type = definition.getCallOrPut() == EuropeanOption.Type.CALL ? "C" : "P";
		strikeOffsets.forEach(
			strikeOffset -> optionEpics.add(
					optionEpicForm.replace("{STRIKE}", Double.toString(atmStrike + strikeOffset).replace("{CALLORPUT}", type))
					)
				);
		
		return optionEpics;
	}
	
	private double getAtmStrike(double underlyingPrice, VolatilitySurfaceDefinition definition) {
	// TODO Auto-generated method stub
	return 0;
}
}
