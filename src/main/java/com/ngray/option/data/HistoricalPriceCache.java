package com.ngray.option.data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;

import com.ngray.option.Log;
import com.ngray.option.RiskEngine;
import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionException;
import com.ngray.option.ig.price.IGPriceSnapshot;
import com.ngray.option.ig.price.IGPriceSnapshotSequence;
import com.ngray.option.ig.price.IGPriceSnapshotSequence.Resolution;
import com.ngray.option.mongo.MongoConstants;
import com.ngray.option.mongo.Price;

public class HistoricalPriceCache {

	private final String name;
	private final Map<LocalDate, Map<String, Price>> cache;
	
	public HistoricalPriceCache(String name) {
		this.name = name;
		this.cache = new TreeMap<>();
	}
	
	/**
	 * Get the name of this cache
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Return the price for the given security identifier on the given value date.
	 * Returns null if no price is present.
	 * Equivalent to getPrice(valueDate, identifier, false) ie. no read-through
	 * @param valueDate
	 * @param identifier
	 * @return
	 */
	public Price getPrice(LocalDate valueDate, String identifier) {
		return getPrice(valueDate, identifier, false);
	}
	
	/**
	 * Return the price for the given security identifier on the given value date.
	 * Returns null if no price is present  in the cache, and read through fails. Read through to Mongo (and IG)
	 * if not cached, cache the value and return it.
	 * @param valueDate
	 * @param identifier
	 * @param readThrough
	 * @return
	 */
	public Price getPrice(LocalDate valueDate, String identifier, boolean readThrough) {
		Map<String, Price> pricesOnValueDate = cache.getOrDefault(valueDate, new TreeMap<>());
		if (pricesOnValueDate.containsKey(identifier)) {
			return pricesOnValueDate.get(identifier);
		}
		
		// read through
		if (readThrough) {
			Set<String> ids = new TreeSet<>();
			ids.add(identifier);
			load(valueDate, valueDate, ids);
			return getPrice(valueDate, identifier, false);
		}
		
		return null;
	}
	
	public void load(LocalDate startDate, LocalDate endDate, Set<String> identifiers) {
		loadFromMongo(startDate, endDate, identifiers);
		getMissingEntries(startDate, endDate, identifiers).forEach(
				(date, set) -> loadFromIG(date, set, true)
				);
	}

	private void loadFromMongo(LocalDate startDate, LocalDate endDate, Set<String> identifiers) {
		Log.getLogger().info("Loading historical prices from Mongo database: " + identifiers);
		Log.getLogger().info("Date range: " + startDate + " to " + endDate);
		MongoDatabase db = getMongoClient().getDatabase(MongoConstants.DATABASE_NAME);
		MongoCollection<Price> pricesCollection = db.getCollection(MongoConstants.PRICE_COLLECTION, Price.class);
		MongoCursor<Price> prices = pricesCollection.find(
				and(in(Price.ID_COL, identifiers), 
					gte(Price.VALUE_DATE_COL, startDate.toString()), 
					lte(Price.VALUE_DATE_COL, endDate.toString())
					)
				).iterator();
		
		while (prices.hasNext()) {
			Price price = prices.next();
			if (!cache.containsKey(price.getValueDate())) {
				cache.put(price.getValueDate(), new TreeMap<>());
			}
			cache.get(price.getValueDate()).put(price.getId(), price);
		}
	}
	
	private void loadFromIG(LocalDate valueDate, Set<String> identifiers, boolean insertInMongo) {
		if (identifiers.isEmpty()) return;
		
		Log.getLogger().info("Loading prices from IG for value date: " + valueDate + ": " + identifiers);
		Session session = getSession();
		List<Price> prices = new ArrayList<>();
		identifiers.forEach(
				id -> {
					IGPriceSnapshotSequence seq = new IGPriceSnapshotSequence(id, Resolution.DAY, valueDate, valueDate);
					try {
						seq.getHistoricalPrices(session);
						TreeSet<String> timestamps = new TreeSet<>(seq.getKeySet());
						
						// we cache the latest update for the given value date	
						IGPriceSnapshot historicalPrice = seq.getHistoricalPrice(timestamps.last());
						 if (!cache.containsKey(valueDate)) {
								cache.put(valueDate, new TreeMap<>());
						}
					    Price price = new Price(id, valueDate, historicalPrice);
					    prices.add(price);
						cache.get(valueDate).put(id, price);					   		    
					} catch (SessionException e) {
						Log.getLogger().warn(e.getMessage(), e);
					}			
				}
			);
		
		if (insertInMongo && !prices.isEmpty()) {
			insertInMongo(prices);
		}	
	}
	
	private void insertInMongo(List<Price> prices) {
		Log.getLogger().info("Inserting historical prices from IG into Mongo database...");
		MongoDatabase db = RiskEngine.getMongoClient().getDatabase(MongoConstants.DATABASE_NAME);
		MongoCollection<Price> pricesCollection = db.getCollection(MongoConstants.PRICE_COLLECTION, Price.class);
		pricesCollection.insertMany(prices);
	}

	private Map<LocalDate, Set<String>> getMissingEntries(LocalDate startDate, LocalDate endDate, Set<String> identifiers) {
		// return a set of identifiers for each date between startDate and endDate which do not have entries in the cache
		Map<LocalDate, Set<String>> missingEntries = new TreeMap<>();
		LocalDate thisDate = startDate;
		while (!thisDate.isAfter(endDate)) {
			Map<String, Price> pricesOnThisDate = cache.getOrDefault(thisDate, new TreeMap<>());
			Set<String> missingIds = new TreeSet<>(identifiers);
			missingIds.removeAll(pricesOnThisDate.keySet());
			missingEntries.put(thisDate, missingIds);
			// won't bother with holidays
			if (thisDate.getDayOfWeek() == DayOfWeek.FRIDAY) {
				thisDate = thisDate.plusDays(3);
			} else {
				thisDate = thisDate.plusDays(1);
			}
		}
		return missingEntries;
	}
	
	private Session getSession() {
		return RiskEngine.getSession();
	}
	
	private MongoClient getMongoClient() {
		return RiskEngine.getMongoClient();
	}
}
	
