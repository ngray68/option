package com.ngray.option.ig.refdata;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ngray.option.Log;

public class OptionReferenceDataLoader {
		
	private static String[] ATTRIBUTES = {
			"OptionEpic",
			"UnderlyingEpic",
			"Strike",
			"Expiry",
			"CallOrPut",
			"DividendYield",
			"RiskFreeRate"
		};
	
	public static List<Map<String, String>> loadFromFile(String filename) throws FileNotFoundException, IOException, MissingReferenceDataException {
		
		Log.getLogger().info("Loading option reference data from file " + filename);
		List<Map<String, String>> results = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			String line = null;
			// read the header and discard
			reader.readLine();
			while ((line = reader.readLine()) != null) {
				Log.getLogger().info(line);
				String[] data = line.split(",");
				if (data.length != ATTRIBUTES.length) {
					throw new MissingReferenceDataException("Missing option reference data in file");
				}
				Map<String, String> thisEntry = new HashMap<>();
				for (int i = 0; i < data.length; ++i) {
					thisEntry.put(ATTRIBUTES[i], data[i]);
				}
				results.add(thisEntry);
			}
		}
		
		return results;
		
	}

}
