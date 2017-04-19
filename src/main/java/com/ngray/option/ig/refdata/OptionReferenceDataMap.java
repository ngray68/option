package com.ngray.option.ig.refdata;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.ngray.option.financialinstrument.EuropeanOption.Type;

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
	 */
	public static void init() {
		
		// Gold futures options
		referenceData.put("Gold Futures 1285 PUT", new OptionReferenceData("Gold Futures 1285 PUT", "Gold", 1285.0, LocalDate.of(2017, 5, 25), Type.PUT));
		referenceData.put("Gold Futures 1290 CALL", new OptionReferenceData("Gold Futures 1285 PUT", "Gold", 1290.0, LocalDate.of(2017, 5, 25), Type.CALL));
	}

}
