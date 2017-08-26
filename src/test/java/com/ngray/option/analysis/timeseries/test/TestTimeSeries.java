package com.ngray.option.analysis.timeseries.test;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ngray.option.analysis.timeseries.TimeSeries;
import com.ngray.option.analysis.timeseries.TimeSeriesException;

public class TestTimeSeries {

	TimeSeries<LocalDate> testSeries;
	double minValue = 3.0;
	double maxValue = 12.0;
	double arithmeticMean = 158.0/25.0;
	double[] movingAvgs = { 
								 5.400
								,5.600
								,5.600
								,5.200
								,6.400
								,7.600
								,7.000
								,6.600
								,6.600
								,5.400
								,3.800
								,4.200
								,4.400
								,4.800
								,5.600
								,6.400
								,7.000
								,8.000
								,8.600
								,8.600
								,8.400
						  };
	
	@Before
	public void setUp() throws Exception {
		int year = 2017;
		int month = 8;
		testSeries = new TimeSeries<LocalDate>();
		testSeries.insert(LocalDate.of(year, month, 1), 5.0);
		testSeries.insert(LocalDate.of(year, month, 2), 6.0);
		testSeries.insert(LocalDate.of(year, month, 3), 6.0);
		testSeries.insert(LocalDate.of(year, month, 4), 4.0);
		testSeries.insert(LocalDate.of(year, month, 5), 6.0);
		testSeries.insert(LocalDate.of(year, month, 6), 6.0);
		testSeries.insert(LocalDate.of(year, month, 7), 6.0);
		testSeries.insert(LocalDate.of(year, month, 8), 4.0);
		testSeries.insert(LocalDate.of(year, month, 9), 10.0);
		testSeries.insert(LocalDate.of(year, month, 10), 12.0);
		testSeries.insert(LocalDate.of(year, month, 11), 3.0);
		testSeries.insert(LocalDate.of(year, month, 12), 4.0);
		testSeries.insert(LocalDate.of(year, month, 13), 4.0);
		testSeries.insert(LocalDate.of(year, month, 14), 4.0);
		testSeries.insert(LocalDate.of(year, month, 15), 4.0);
		testSeries.insert(LocalDate.of(year, month, 16), 5.0);
		testSeries.insert(LocalDate.of(year, month, 17), 5.0);
		testSeries.insert(LocalDate.of(year, month, 18), 6.0);
		testSeries.insert(LocalDate.of(year, month, 19), 8.0);
		testSeries.insert(LocalDate.of(year, month, 20), 8.0);
		testSeries.insert(LocalDate.of(year, month, 21), 8.0);
		testSeries.insert(LocalDate.of(year, month, 22), 10.0);
		testSeries.insert(LocalDate.of(year, month, 23), 9.0);
		testSeries.insert(LocalDate.of(year, month, 24), 8.0);
		testSeries.insert(LocalDate.of(year, month, 25), 7.0);
		
		
	}

	@Test
	public void testInsert() {
		int size = testSeries.getSize();
		testSeries.insert(LocalDate.of(2017, 8, 29), 50.0);
		assertTrue(testSeries.getSize() == size + 1);
	}

	@Test
	public void testGetSize() {
		assertTrue(testSeries.getSize() == 25);
	}

	@Test
	public void testGetMaxValue() throws TimeSeriesException {
		assertTrue(testSeries.getMaxValue() == maxValue);
	}

	@Test
	public void testGetMinValue() throws TimeSeriesException {
		assertTrue(testSeries.getMinValue() == minValue);
	}

	@Test
	public void testGetMovingAverage() throws TimeSeriesException {
		TimeSeries<LocalDate> movingAverages = testSeries.getMovingAverage(5);
		assertTrue(movingAverages.getSize() == testSeries.getSize() - 5 + 1);
		int i = 0;
		List<LocalDate> timePoints = movingAverages.getTimePoints();
		for (LocalDate timePoint : timePoints) {
			assertTrue(Double.compare(movingAverages.getValue(timePoint), movingAvgs[i]) == 0);
			++i;
		}	
	}
}
