package com.ngray.option.analysis.timeseries;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.ngray.option.Log;
import com.ngray.option.mongo.MongoCache;
import com.ngray.option.mongo.MongoCacheRegistry;
import com.ngray.option.mongo.MongoCacheRegistryException;
import com.ngray.option.mongo.Price.SnapshotType;
import com.ngray.option.volatilitysurface.VolatilitySurface;
import com.ngray.option.volatilitysurface.VolatilitySurfaceException;

public class VolatilitySurfaceTimeSeries {

	private final String volatilitySurfaceName;
	private final SnapshotType snapshotType;
	private final LocalDate fromDate;
	private final LocalDate toDate;
	
	private final Map<Double, Map<Double, TimeSeries<LocalDate>>> localCache;
	
	/**
	 * Construct a volatility surface time series from saved vol surfaces with the given name
	 * using all surfaces between and including the given from and to dates
	 * @param volatilitySurfaceName
	 * @param fromDate
	 * @param toDate
	 */
	private VolatilitySurfaceTimeSeries(String volatilitySurfaceName, SnapshotType snapshotType, LocalDate fromDate, LocalDate toDate) {
		this.volatilitySurfaceName = volatilitySurfaceName;
		this.snapshotType = snapshotType;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.localCache = new HashMap<>();
	}
	
	public static VolatilitySurfaceTimeSeries create(String volatilitySurfaceName, SnapshotType snapshotType, LocalDate fromDate, LocalDate toDate) throws TimeSeriesException {
		if (fromDate.compareTo(toDate) > 0) {
			throw new TimeSeriesException("Time series must have fromDate <= toDate");
		}
		return new VolatilitySurfaceTimeSeries(volatilitySurfaceName, snapshotType, fromDate, toDate);
	}
	
	/**
	 * Get the maximum IV for the given (daysToExpiry, atmOffset) over the time period of this series
	 * as specified by fromDate and toDate when constructed
	 * @param daysToExpiry
	 * @param atmOffset
	 * @return
	 * @throws TimeSeriesException 
	 */
	public double getImpliedVolatilityMaxValue(double daysToExpiry, double atmOffset) throws TimeSeriesException {
		TimeSeries<LocalDate> timeSeries = getImpliedVolatilityTimeSeries(daysToExpiry, atmOffset);
		return timeSeries.getMaxValue();
	}
	
	/**
	 * Get the minimum IV for the given (daysToExpiry, atmOffset) over the time period of this series
	 * as specified by fromDate and toDate when constructed
	 * @param daysToExpiry
	 * @param atmOffset
	 * @return
	 * @throws TimeSeriesException 
	 */
	public double getImpliedVolatilityMinValue(double daysToExpiry, double atmOffset) throws TimeSeriesException {
		TimeSeries<LocalDate> timeSeries = getImpliedVolatilityTimeSeries(daysToExpiry, atmOffset);
		return timeSeries.getMinValue();
	}
	
	/**
	 * Get the N-day moving average of IV at (daysToExpiry, atmOffset) 
	 * @param daysToExpiry
	 * @param atmOffset
	 * @param N
	 * @return
	 * @throws TimeSeriesException 
	 */
	public TimeSeries<LocalDate> getImpliedVolatilityMovingAverage(double daysToExpiry, double atmOffset, int N) throws TimeSeriesException {
		TimeSeries<LocalDate> timeSeries = getImpliedVolatilityTimeSeries(daysToExpiry, atmOffset);
		return timeSeries.getMovingAverage(N);
	}

	public String getVolatilitySurfaceName() {
		return volatilitySurfaceName;
	}

	public LocalDate getFromDate() {
		return fromDate;
	}

	public LocalDate getToDate() {
		return toDate;
	}
	
	public SnapshotType getSnapshotType() {
		return snapshotType;
	}

	private TimeSeries<LocalDate> buildImpliedVolatilityTimeSeries(double daysToExpiry, double atmOffset) throws TimeSeriesException  {
		Log.getLogger().info("Building time series for " + volatilitySurfaceName + " between " + fromDate + " and " + toDate);
		TimeSeries<LocalDate> timeSeries = new TimeSeries<>();
 		try {
			MongoCache<VolatilitySurface> volSurfaceCache = MongoCacheRegistry.get(VolatilitySurface.class);
			LocalDate thisDate = fromDate;
			VolatilitySurface lastSurface = null;
			while (thisDate.compareTo(toDate) <= 0) {
				Log.getLogger().debug("Fetching volatility surface " + volatilitySurfaceName + ":" + thisDate + ":" + snapshotType);
				VolatilitySurface thisSurface = volSurfaceCache.get(volatilitySurfaceName + ":" + thisDate + ":" + snapshotType);
				if (thisSurface != null) {
					lastSurface = thisSurface;
					timeSeries.insert(thisDate, thisSurface.getImpliedVolatility(daysToExpiry, atmOffset));
				} else {
					Log.getLogger().warn("No volatility surface " + volatilitySurfaceName + ":" + thisDate + ":" + snapshotType);
					if (thisDate.getDayOfWeek().equals(DayOfWeek.FRIDAY) || thisDate.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
						Log.getLogger().debug("Weekend - rolling forward previous volatility surface");
						timeSeries.insert(thisDate, lastSurface.getImpliedVolatility(daysToExpiry, atmOffset));
					}
				}
				thisDate = thisDate.plusDays(1);
			}
			if (timeSeries.getSize() == 0) {
				throw new TimeSeriesException("Empty volatility surface time series - try loading some volatility surfaces into the cache");
			}
			return timeSeries;
		} catch (MongoCacheRegistryException | VolatilitySurfaceException e) {
			throw new TimeSeriesException(e.getMessage());
		}	
	}
	
	private TimeSeries<LocalDate> getCachedImpliedVolatilityTimeSeries(double daysToExpiry, double atmOffset) {
		if (localCache.get(daysToExpiry) != null) {
			return localCache.get(daysToExpiry).get(atmOffset);
		}
		return null;
	}
	
	private void cacheImpliedVolatilityTimeSeries(double daysToExpiry, double atmOffset, TimeSeries<LocalDate> timeSeries) {
		if (localCache.get(daysToExpiry) == null) {
			localCache.put(daysToExpiry, new HashMap<>());
		}
		localCache.get(daysToExpiry).put(atmOffset, timeSeries);
	}
	
	private TimeSeries<LocalDate> getImpliedVolatilityTimeSeries(double daysToExpiry, double atmOffset) throws TimeSeriesException {
		Log.getLogger().info("Getting implied volatitility time series");
		TimeSeries<LocalDate> timeSeries = getCachedImpliedVolatilityTimeSeries(daysToExpiry, atmOffset);
		if (timeSeries == null) {
			timeSeries = buildImpliedVolatilityTimeSeries(daysToExpiry, atmOffset);
			cacheImpliedVolatilityTimeSeries(daysToExpiry, atmOffset, timeSeries);
		}	
		return timeSeries;
	}

	public double getMeanImpliedVolatility(double daysToExpiry, double atmOffset) throws TimeSeriesException {
		TimeSeries<LocalDate> timeSeries = getImpliedVolatilityTimeSeries(daysToExpiry, atmOffset);
		return timeSeries.getMovingAverage(timeSeries.getSize()).getValue(toDate);
		
	}
	
}
