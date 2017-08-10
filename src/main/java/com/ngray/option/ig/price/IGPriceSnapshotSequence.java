package com.ngray.option.ig.price;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.ngray.option.ig.rest.RestAPIGet;
import com.ngray.option.ig.rest.RestAPIResponse;
import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionException;

/**
 * A sequence of HistoricalPrice objects
 * @author nigelgray
 *
 */
public final class IGPriceSnapshotSequence {

	/**
	 * Prices are keyed by the timestamp for the snapshotTimeUTC
	 */
	private final Map<String, IGPriceSnapshot> historicalPrices;
	
	private final String resolution;
	
	private final String startDate;
	
	private final String endDate;

	private final String id;
	
	public enum Resolution {
		//Price resolution 
		SECOND("SECOND"),
		MINUTE("MINUTE"),
		MINUTE_2("MINUTE_2"),
		MINUTE_3("MINUTE_3"),
		MINUTE_5("MINUTE_5"),
		MINUTE_10("MINUTE_10"),
		MINUTE_15("MINUTE_15"),
		MINUTE_30("MINUTE_30"),
		HOUR("HOUR"),
		HOUR_2("HOUR_2"),
		HOUR_3("HOUR_3"),
		HOUR_4("HOUR_4"),
		DAY("DAY"),
		WEEK("WEEK"),
		MONTH("MONTH");
				
		private final String resolution;
		
		private Resolution(String resolution) {
			this.resolution = resolution;
		}
		
		public String getResolution() {
			return resolution;
		}
		
		@Override
		public String toString() {
			return resolution;
		}
		
	};
	
	public IGPriceSnapshotSequence(String identifier, Resolution resolution, LocalDate startDate, LocalDate endDate) {
		historicalPrices = new TreeMap<>();
		this.id = identifier;
		this.resolution = resolution.getResolution();
		this.startDate = format(startDate, "00:00:00");
		this.endDate = format(endDate, "23:59:59");
	}
	
	private String format(LocalDate date, String timeSuffix) {
		// format at yyyy:mm:dd-HH:MM:ss
		return date.getYear() + ":" + 
			   String.format("%02d", date.getMonth().getValue()) + ":" + 
			   String.format("%02d", date.getDayOfMonth()) + "-" +
			   timeSuffix;
	}
	
	public void getHistoricalPrices(Session session) throws SessionException {		
		String request = "/prices/" + id + "/" + resolution + "?startdate=" + startDate + "&enddate=" + endDate;
		RestAPIGet get = new RestAPIGet(request);
		RestAPIResponse response = get.execute(session);
		HistoricalPricesResponse prices = HistoricalPricesResponse.fromJson(response.getResponseBodyAsJson());
		prices.getPrices().forEach((price) -> historicalPrices.put(price.getSnapshotTime(), price));	
	}
	
	public Set<String> getKeySet() {
		return historicalPrices.keySet();
	}

	public String getId() {
		return id;
	}

	public String getResolution() {
		return resolution;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}
	
	/**
	 * Returns the specified historical price or null if it does not
	 * exist in the map
	 * @param key
	 * @return
	 */
	public IGPriceSnapshot getHistoricalPrice(String key) {
		return historicalPrices.get(key);
	}
	
	public void saveToFile(String path) throws FileNotFoundException, IOException {
		String filename = id + "." + getResolution() + "." + getStartDate().replaceAll(":",  "") + "." + getEndDate().replaceAll(":", "") + ".csv";
		String fullPath = path + "/" + filename;
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fullPath), "utf-8"))) {
			writer.write(IGPriceSnapshot.getAttributeNames() + "\n");
			for (Iterator<IGPriceSnapshot> iter = historicalPrices.values().iterator(); iter.hasNext(); ) {
				writer.write(iter.next().toString() + "\n");
			}
			
		}
	}
	
	
	
	private static class HistoricalPricesResponse {
		
		public List<IGPriceSnapshot> prices;
		public String instrumentType;
		
		public HistoricalPricesResponse() {	
		}
		
		public static HistoricalPricesResponse fromJson(String json) {
			Gson gson = new Gson();
			return gson.fromJson(json, HistoricalPricesResponse.class);
		}
		
		public List<IGPriceSnapshot> getPrices() {
			return Collections.unmodifiableList(prices == null ? new ArrayList<>() : prices);
		}
		
		public String getInstrumentType() {
			return instrumentType;
		}
		
	}
	
	

}
