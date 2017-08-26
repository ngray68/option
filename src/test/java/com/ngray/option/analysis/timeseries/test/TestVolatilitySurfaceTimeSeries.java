package com.ngray.option.analysis.timeseries.test;

import static org.junit.Assert.*;

import java.time.LocalDate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ngray.option.analysis.timeseries.TimeSeries;
import com.ngray.option.analysis.timeseries.TimeSeriesException;
import com.ngray.option.analysis.timeseries.VolatilitySurfaceTimeSeries;
import com.ngray.option.mongo.Mongo;
import com.ngray.option.mongo.MongoCacheRegistry;
import com.ngray.option.mongo.MongoCacheRegistryException;
import com.ngray.option.mongo.Price.SnapshotType;
import com.ngray.option.volatilitysurface.VolatilitySurface;
import com.ngray.option.volatilitysurface.VolatilitySurfaceException;

public class TestVolatilitySurfaceTimeSeries {

	private VolatilitySurfaceTimeSeries volSurfaceTimeSeries;
	private final String volatilitySurfaceName = "GBPUSD-PUT";
	private final LocalDate fromDate = LocalDate.of(2017, 8, 13);
	private final LocalDate toDate = LocalDate.of(2017, 8, 24);
	
	@Before
	public void setUp() throws Exception {
		Mongo.initialize();
		MongoCacheRegistry.get(VolatilitySurface.class).getAll(true);
		volSurfaceTimeSeries = VolatilitySurfaceTimeSeries.create(volatilitySurfaceName, SnapshotType.CLOSE, fromDate, toDate);
	}
	
	@After
	public void tearDown() {
		Mongo.getMongoClient().close();
	}

	@Test
	public void testGetImpliedVolatilityMaxValue() throws TimeSeriesException {
		double maxIV = volSurfaceTimeSeries.getImpliedVolatilityMaxValue(100.0, 0.0);
		System.out.println("Max IV: " + maxIV);
	}

	@Test
	public void testGetImpliedVolatilityMinValue() throws TimeSeriesException {
		double minIV = volSurfaceTimeSeries.getImpliedVolatilityMinValue(100.0, 0.0);
		System.out.println("Min IV: " + minIV);
	}

	@Test
	public void testGetImpliedVolatilityMovingAverage() throws TimeSeriesException {
		TimeSeries<LocalDate> fiveDayMovingAverage = volSurfaceTimeSeries.getImpliedVolatilityMovingAverage(100.0, 0.0, 5);
		for (LocalDate date : fiveDayMovingAverage.getTimePoints()) {
			System.out.println("ValueDate: " + date + ", Moving Average: " + fiveDayMovingAverage.getValue(date));
		}
	}
	
	@Test(expected=TimeSeriesException.class)
	public void testGetImpliedVolatilityMovingAverageThrows() throws TimeSeriesException {
		// not enough data points - should throw
		volSurfaceTimeSeries.getImpliedVolatilityMovingAverage(100.0, 0.0, 30);
	}
	
	@Test
	public void testGetImpliedVolatilityMovingAverageOneDay() throws TimeSeriesException, VolatilitySurfaceException, MongoCacheRegistryException {
		TimeSeries<LocalDate> oneDayMovingAverage = volSurfaceTimeSeries.getImpliedVolatilityMovingAverage(100.0, 0.0, 1);
		for (LocalDate date : oneDayMovingAverage.getTimePoints()) {
			System.out.println("ValueDate: " + date + ", Moving Average: " + oneDayMovingAverage.getValue(date));
			String volSurfaceID = volSurfaceTimeSeries.getVolatilitySurfaceName() + ":" + date + ":" + volSurfaceTimeSeries.getSnapshotType();
			VolatilitySurface volSurface = MongoCacheRegistry.get(VolatilitySurface.class).get(volSurfaceID);
			if (volSurface != null) {
				// we may have missing vol surfaces for weekends
				assertTrue(Double.compare(oneDayMovingAverage.getValue(date), volSurface.getImpliedVolatility(100.0, 0.0)) == 0);
			}
		}
	}
	

}
