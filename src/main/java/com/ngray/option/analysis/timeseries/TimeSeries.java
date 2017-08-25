package com.ngray.option.analysis.timeseries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Time series of double values. This class is not thread-safe
 * @author nigelgray
 *
 * @param <K>
 */
public class TimeSeries<K extends Comparable<K>> {
	
	/**
	 * Map of Double values keyed by an ordered set of keys
	 * representing a time series
	 */
	private final Map<K, Double> data;
	
	/**
	 * Create an empty time series
	 */
	public TimeSeries() {
		this.data = new HashMap<>();
	}
	
	/**
	 * Insert a value at the specified time point
	 * @param timePoint
	 * @param value
	 */
	public void insert(K timePoint, double value) {
		data.put(timePoint, value);
	}
	
	/**
	 * Return an ordered list of time points  in the series
	 * @return
	 */
	public List<K> getTimePoints() {
		List<K> timePoints = new ArrayList<>(data.keySet());
		Collections.sort(timePoints);
		return Collections.unmodifiableList(timePoints);
	}
	
	/**
	 * Return the double value at the given time point. 
	 * Return null if not present
	 * @param timePoint
	 * @return
	 */
	public double getValue(K timePoint) {
		return data.get(timePoint);
	}
	
	/**
	 * Return the number of time points in the series
	 * @return
	 */
	public int getSize() {
		return data.size();
	}

	/**
	 * Get the max value in the series. Will throw if the series is empty
	 * @return
	 * @throws TimeSeriesException
	 */
	public double getMaxValue() throws TimeSeriesException {
		if (data.isEmpty()) throw new TimeSeriesException("No maximum value in an empty TimeSeries");
		return data.values().stream().max(Double::compareTo).get();
	}
	
	/**
	 * Get the min value in the series. Will throw if the series is empty
	 * @return
	 * @throws TimeSeriesException
	 */
	public double getMinValue() throws TimeSeriesException {
		if (data.isEmpty()) throw new TimeSeriesException("No minimum value in an empty TimeSeries");
		return data.values().stream().min(Double::compareTo).get();
	}
	
	/**
	 * Get the moving average over the specified number of time points. Returns a time series
	 * of moving averages. The returned time series will have the following characteristics:
	 * 1. numPoints = 1; the returned time series is this time series
	 * 2. numPoints = this.getSize(); the returned time series has one element, a simple average
	 * 3. numPoints < this.getSize(); the returned time series has fewer points than this.getSize(), but more than one
	 * An exception is thrown if numPoints > this.getSize()
	 * @param numPoints
	 * @return
	 * @throws TimeSeriesException
	 */
	public TimeSeries<K> getMovingAverage(int numPoints) throws TimeSeriesException {
		if (numPoints > this.getSize()) throw new TimeSeriesException("Not enough data points in TimeSeries to calculate moving average");
		
		if (numPoints == 1) return this;
		
		List<K> timePoints = new ArrayList<>(data.keySet());
		Collections.sort(timePoints);
		TimeSeries<K> movingAverages = new TimeSeries<>();
		
		double movingSum = 0.0;
		Queue<Double> currentWindow = new LinkedList<>();
		for (int i = 0; i < timePoints.size(); ++i) {
			double value = data.get(timePoints.get(i));			
			movingSum += value;
			currentWindow.add(value);
			
			if (i >= numPoints) {
				movingSum -= currentWindow.remove();
			}
			
			if(i >= numPoints - 1) {
				movingAverages.insert(timePoints.get(i), movingSum/numPoints);
			}
		}
		
		return movingAverages;
	}
}
