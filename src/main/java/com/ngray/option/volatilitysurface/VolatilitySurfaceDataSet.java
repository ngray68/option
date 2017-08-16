package com.ngray.option.volatilitysurface;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.ngray.option.Log;
import com.ngray.option.ig.refdata.MissingReferenceDataException;
import com.ngray.option.ig.refdata.OptionReferenceData;
import com.ngray.option.ig.refdata.OptionReferenceDataMap;
import com.ngray.option.mongo.Price;
import com.ngray.option.mongo.VolatilitySurfaceDefinition;

public class VolatilitySurfaceDataSet {

	public static class OptionData implements Comparable<OptionData> {
		private final String optionId;
		private final double atmOffset;
		
		public OptionData(String optionId, double atmOffset) {
			this.optionId = optionId;
			this.atmOffset = atmOffset;
		}
		
		@Override
		public String toString() {
			return "[ID: " + optionId + ", ATM Offset: " + atmOffset + "]";
		}

		public String getOptionId() {
			return optionId;
		}

		public double getAtmOffset() {
			return atmOffset;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof OptionData)) return false;
			if (this == o) return true;
			
			return ((OptionData)o).getOptionId() == this.getOptionId();
		}

		@Override
		public int compareTo(OptionData o) {
			return Double.compare(this.getAtmOffset(), o.getAtmOffset());
		}		
	}
	
	private final LocalDate valueDate;
	private final Map<String, Price> underlyingPrices;
	private final Map<String, Set<OptionData>> options;
	private final Map<String, Price> optionPrices;
	private final VolatilitySurfaceDefinition definition;
	
	public VolatilitySurfaceDataSet(LocalDate valueDate, Map<String, Price> underlyingPrices,
			Map<String, Set<OptionData>> optionData, Map<String, Price> optionPrices,
			VolatilitySurfaceDefinition definition) {
		this.valueDate = valueDate;
		this.definition = definition;
		this.underlyingPrices = new TreeMap<>(underlyingPrices);
		this.options = new TreeMap<>(optionData);
		this.optionPrices = new TreeMap<>(optionPrices);
	}

	public LocalDate getValueDate() {
		return valueDate;
	}

	public VolatilitySurfaceDefinition getDefinition() {
		return definition;
	}
	
	public Set<String> getUnderlyingIdentifiers() {
		return underlyingPrices.keySet();
	}
	
	public Set<LocalDate> getOptionExpiries() {
		Set<LocalDate> expiries = new TreeSet<>();
		options.forEach(
				(underlying, optionDataSet) -> {
					optionDataSet.forEach(
						optionData -> {
							try {
								OptionReferenceData refData;
								refData = OptionReferenceDataMap.getOptionReferenceData(optionData.getOptionId());
								LocalDate expiryDate = refData.getExpiryDate();
								expiries.add(expiryDate);
							} catch (MissingReferenceDataException e) {
								Log.getLogger().error(e.getMessage(), e);
							}
						}
				);
			}
		);
		return expiries;
	}
	
	public Set<OptionData> getOptionData(String underlyingId) {
		return options.get(underlyingId);
	}
	
	public Price getUnderlyingPrice(String underlyingId) {
		return underlyingPrices.get(underlyingId);
	}
	
	public Price getOptionPrice(String optionId) {
		return optionPrices.get(optionId);
	}
}
