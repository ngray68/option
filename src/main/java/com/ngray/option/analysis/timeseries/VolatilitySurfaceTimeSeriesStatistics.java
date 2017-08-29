package com.ngray.option.analysis.timeseries;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ngray.option.Log;
import com.ngray.option.mongo.Price.SnapshotType;

public class VolatilitySurfaceTimeSeriesStatistics {
	
	// constants for moving averages
	public static int FIVE_DAY = 5;
	public static int THIRTY_DAY = 30;
	public static int NINETY_DAY = 90;

	private final String volatilitySurfaceName;
	private final SnapshotType snapshotType;
	private final LocalDate fromDate;
	private final LocalDate toDate;

	private final boolean calcMin;
	private final boolean calcMax;
	private final boolean calcMean;
	
	private final boolean calcFiveDay;
	private final boolean calcThirtyDay;
	private final boolean calcNinetyDay;

	// the maturity and strike (expressed as offset from atm) to
	// calculate stats for
	private final Set<Double> atmOffsets;
	private final double daysToExpiry;
	
	// cache the time series generated in evaluate
	VolatilitySurfaceTimeSeries volSurfaceTimeSeries;
	
	// cache the results generated in evaluate
	// map atmOffset -> result
	private Map<Double, Double> minImpliedVol;
	private Map<Double, Double> maxImpliedVol;
	private Map<Double, Double> meanImpliedVol;
	
	// map atmOffset -> movingAvgDays -> moving average time series
	private Map<Double, Map<Integer, TimeSeries<LocalDate>>> movingAverages;
	
	
	public VolatilitySurfaceTimeSeriesStatistics(
			String volatilitySurfaceName, SnapshotType snapshotType, LocalDate fromDate, LocalDate toDate,
			boolean calcMin, boolean calcMax, boolean calcMean,
			boolean calcFiveDay, boolean calcThirtyDay, boolean calcNinetyDay,
			Set<Double> atmOffsets, double daysToExpiry) {
		this.volatilitySurfaceName = volatilitySurfaceName;
		this.snapshotType = snapshotType;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.calcMin = calcMin;
		this.calcMax = calcMax;
		this.calcMean = calcMean;
		this.calcFiveDay = calcFiveDay;
		this.calcThirtyDay = calcThirtyDay;
		this.calcNinetyDay = calcNinetyDay;
		this.atmOffsets = atmOffsets;
		this.daysToExpiry = daysToExpiry;
		
		this.minImpliedVol = new HashMap<>();
		this.maxImpliedVol = new HashMap<>();
		this.meanImpliedVol = new HashMap<>();
		this.movingAverages = new HashMap<>();
	}
	
	public void evaluate() {	
		Log.getLogger().info("Evaluating time series statistics for volatility surface " + volatilitySurfaceName +
				             " " + snapshotType + " between " + fromDate + " and " + toDate);
		try {
			volSurfaceTimeSeries = VolatilitySurfaceTimeSeries.create(
																		volatilitySurfaceName, 
																		snapshotType,
																		fromDate,
																		toDate
																	);
		} catch (TimeSeriesException e) {
			Log.getLogger().error(e.getMessage(), e);
			return;
		}

		for (double atmOffset : atmOffsets) {
			if (calcMin) {
				try {
					double min = volSurfaceTimeSeries.getImpliedVolatilityMinValue(daysToExpiry, atmOffset);
					minImpliedVol.put(atmOffset, min);
					Log.getLogger().debug("Min=" + min);
				} catch (TimeSeriesException e) {
					Log.getLogger().error(e.getMessage(), e);
				}	
			}
			if (calcMax) {
				try {
					double max = volSurfaceTimeSeries.getImpliedVolatilityMaxValue(daysToExpiry, atmOffset);
					maxImpliedVol.put(atmOffset, max);
					Log.getLogger().debug("Max=" + max);
				} catch (TimeSeriesException e) {
					Log.getLogger().error(e.getMessage(), e);
				}
			}
			if (calcMean) {
				try {
					double mean = volSurfaceTimeSeries.getMeanImpliedVolatility(daysToExpiry, atmOffset);
					meanImpliedVol.put(atmOffset, mean);
					Log.getLogger().debug("Mean=" + mean);
				} catch (TimeSeriesException e) {
					Log.getLogger().error(e.getMessage(), e);
				}
			}
			
			try {
				TimeSeries<LocalDate> oneDay = volSurfaceTimeSeries.getImpliedVolatilityMovingAverage(daysToExpiry, atmOffset, 1);
				cacheMovingAverage(atmOffset, 1, oneDay);
			} catch (TimeSeriesException e) {
				Log.getLogger().error(e.getMessage(), e);
			}
			
			if (calcFiveDay) {
				try {
					TimeSeries<LocalDate> fiveDay = volSurfaceTimeSeries.getImpliedVolatilityMovingAverage(daysToExpiry, atmOffset, FIVE_DAY);
					cacheMovingAverage(atmOffset, FIVE_DAY, fiveDay);
				} catch (TimeSeriesException e) {
					Log.getLogger().error(e.getMessage(), e);
				}
			}
			if (calcThirtyDay) {
				try {
					TimeSeries<LocalDate> thirtyDay = volSurfaceTimeSeries.getImpliedVolatilityMovingAverage(daysToExpiry, atmOffset, THIRTY_DAY);
					cacheMovingAverage(atmOffset, THIRTY_DAY, thirtyDay);
				} catch (TimeSeriesException e) {
					Log.getLogger().error(e.getMessage(), e);
				}
			}
			if (calcNinetyDay) {
				try {
					TimeSeries<LocalDate> ninetyDay = volSurfaceTimeSeries.getImpliedVolatilityMovingAverage(daysToExpiry, atmOffset, NINETY_DAY);
					cacheMovingAverage(atmOffset, NINETY_DAY, ninetyDay);
				} catch (TimeSeriesException e) {
					Log.getLogger().error(e.getMessage(), e);
				}
			}	
		}
	}

	public String getVolatilitySurfaceName() {
		return volatilitySurfaceName;
	}

	public SnapshotType getSnapshotType() {
		return snapshotType;
	}

	public LocalDate getFromDate() {
		return fromDate;
	}

	public LocalDate getToDate() {
		return toDate;
	}

	public boolean getCalcMin() {
		return calcMin;
	}

	public boolean getCalcMax() {
		return calcMax;
	}

	public boolean getCalcMean() {
		return calcMean;
	}

	public boolean getCalcFiveDay() {
		return calcFiveDay;
	}

	public boolean getCalcThirtyDay() {
		return calcThirtyDay;
	}

	public boolean getCalcNinetyDay() {
		return calcNinetyDay;
	}

	public Set<Double> getAtmOffsets() {
		return Collections.unmodifiableSet(atmOffsets);
	}

	public double getDaysToExpiry() {
		return daysToExpiry;
	}
	
	public double getMinImpliedVol(double atmOffset) {
		return minImpliedVol.getOrDefault(atmOffset, Double.NaN);
	}
	
	public double getMaxImpliedVol(double atmOffset) {
		return maxImpliedVol.getOrDefault(atmOffset, Double.NaN);
	}
	
	public double getMeanImpliedVol(double atmOffset) {
		return meanImpliedVol.getOrDefault(atmOffset, Double.NaN);
	}
	
	public TimeSeries<LocalDate> getMovingAverage(double atmOffset, int movingAvgDays) {
		return  movingAverages.getOrDefault(atmOffset, new HashMap<>()).getOrDefault(movingAvgDays, new TimeSeries<>());
	}
	
	private void cacheMovingAverage(double atmOffset, int movingAvgDays, TimeSeries<LocalDate> movingAvg) {
		if (!movingAverages.containsKey(atmOffset)) {
			movingAverages.put(atmOffset, new HashMap<>());
		}
		movingAverages.get(atmOffset).put(movingAvgDays, movingAvg);
	}
}
	
	
	


