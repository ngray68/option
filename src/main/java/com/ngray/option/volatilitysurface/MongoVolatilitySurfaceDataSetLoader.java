package com.ngray.option.volatilitysurface;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.ngray.option.Log;
import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.Security;
import com.ngray.option.ig.refdata.OptionReferenceDataMap;
import com.ngray.option.marketdata.MarketData;
import com.ngray.option.marketdata.MarketData.Type;

import static com.mongodb.client.model.Filters.*;

/**
 * Implements VolatilitySurfaceDataSetLoader to load a VolatilitySurfaceData set from a MongoDB instance
 * @author nigelgray
 *
 */
public class MongoVolatilitySurfaceDataSetLoader implements VolatilitySurfaceDataSetLoader {
	
	public static final String OPTION_DB = "optiondb";
	public static final String PRICES = "prices";
	
	private MongoCollection<Document> prices;
	
	public MongoVolatilitySurfaceDataSetLoader(MongoDatabase database) {
		prices = database.getCollection(PRICES);
	}

	@Override
	public Map<Security, MarketData> getUnderlyingPrices(LocalDate valueDate, VolatilitySurfaceDefinition definition) {
		
		Log.getLogger().info("MongoDB optiondb..prices retrieving underlying prices for " + definition.getName());
		List<String> underlyingEpics = definition.getUnderlyingEpics();
		List<Security> underlyings = 
				OptionReferenceDataMap.getUnderlyings()
									  .stream()
									  .filter(security -> underlyingEpics.contains(security.getIdentifier()))
									  .collect(Collectors.toList());
		
		Map<Security, MarketData> underlyingPrices = new HashMap<>();
		underlyings.forEach(
				underlying -> { 
					FindIterable<Document> priceIter = prices.find(and(eq("ValueDate", valueDate), eq("epic", underlying.getIdentifier())));
					if (priceIter != null && priceIter.first() != null) {
						underlyingPrices.put(underlying, new MarketData(underlying.getIdentifier(), priceIter.first().getDouble("Close"), Type.PRICE));
					} else {
						Log.getLogger().warn("MongoDB optiondb..prices has no entry for " + underlying + " on value date " + valueDate);
					}
				});
				                        			
		return underlyingPrices;
	}

	@Override
	public Map<Security, Set<EuropeanOption>> getOptions(LocalDate valueDate, VolatilitySurfaceDefinition definition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<EuropeanOption, MarketData> getOptionPrices(LocalDate valueDate,
			VolatilitySurfaceDefinition definition) {
		// TODO Auto-generated method stub
		return null;
	}

}
