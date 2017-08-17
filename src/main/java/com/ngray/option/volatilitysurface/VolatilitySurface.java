package com.ngray.option.volatilitysurface;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.analysis.BivariateFunction;
import org.apache.commons.math3.analysis.interpolation.BivariateGridInterpolator;
import org.apache.commons.math3.exception.*;

import com.ngray.option.Log;
import com.ngray.option.financialinstrument.EuropeanOption.Type;
import com.ngray.option.ig.refdata.MissingReferenceDataException;
import com.ngray.option.ig.refdata.OptionReferenceData;
import com.ngray.option.ig.refdata.OptionReferenceDataMap;
import com.ngray.option.model.BlackScholesModel;
import com.ngray.option.mongo.MongoObject;
import com.ngray.option.mongo.Price.SnapshotType;
import com.ngray.option.volatilitysurface.VolatilitySurfaceDataSet.OptionData;

/**
 * This class builds an interpolating function from the supplied times to expiry,
 * strike offsets and implied volatilities given in the data set. This interpolating
 * function is used to calculate implied volatilities for any data point between the specified
 * ranges
 * @author nigelgray
 *
 */
public class VolatilitySurface implements MongoObject {

	//private final VolatilitySurfaceDataSet dataSet;
	
	private final String uniqueId;
	private final String name;
	private final LocalDate valueDate;
	private final Type optionType;
	private final SnapshotType snapshotType;
	private final BivariateGridInterpolator interpolator;
	
	private BivariateFunction interpolationFunction;
	private double[] strikeOffsets;
	private double[] daysToExpiry;
	private double[][] impliedVolatilities;
	
	// used when calculating IVs from a data set, not used for anything else
	private Map<Double, Integer> daysToExpiryIndices;
	private Map<Double, Integer> strikeOffsetIndices;
	
	/**
	 * Construct a volatility surface object from the supplied IVs
	 * @param uniqueId
	 * @param name
	 * @param valueDate
	 * @param optionType
	 * @param snapshotType
	 * @param interpolator
	 * @param daysToExpiry
	 * @param strikeOffsets
	 * @param impliedVolatilities
	 * @throws VolatilitySurfaceException
	 */
	public VolatilitySurface(String uniqueId, String name, LocalDate valueDate, Type optionType, SnapshotType snapshotType,
					BivariateGridInterpolator interpolator, double[] daysToExpiry, double[] strikeOffsets, double[][] impliedVolatilities)  {
		this.uniqueId = uniqueId;
		this.name = name;
		this.valueDate = valueDate;
		this.optionType = optionType;
		this.snapshotType = snapshotType;
		this.interpolator = interpolator;
		this.daysToExpiry = daysToExpiry.clone();
		this.strikeOffsets = strikeOffsets.clone();
		this.impliedVolatilities = impliedVolatilities.clone();
		buildInterpolationFunction();
	}
	/**
	 * Construct a VolatilitySurface from the given data set, using the prices specified by snapshotType
	 * and using the specified BivariateGridInterpolator. The size of the data set must be consistent with
	 * any restrictions imposed by the interpolator.
	 * @param dataSet
	 * @param snapshotType
	 * @param interpolator
	 * @throws VolatilitySurfaceException 
	 */
	public VolatilitySurface(VolatilitySurfaceDataSet dataSet, SnapshotType snapshotType, BivariateGridInterpolator interpolator) {
		//this.dataSet = dataSet;
		this.uniqueId = dataSet.getDefinition().getName() + ":" + dataSet.getValueDate() + ":" + snapshotType;
		this.name = dataSet.getDefinition().getName();
		this.valueDate = dataSet.getValueDate();
		this.optionType = dataSet.getDefinition().getCallOrPut();
		this.interpolator = interpolator;
		this.snapshotType = snapshotType;
		this.daysToExpiryIndices = new HashMap<>();
		this.strikeOffsetIndices = new HashMap<>();
		build(dataSet);
	}
	
	/**
	 * Get the implied volatility for the given daysToExpiry, strikeOffset coordinate
	 * @param daysToExpiry
	 * @param strikeOffset
	 * @return
	 * @throws VolatilitySurfaceException
	 */
	public double getImpliedVolatility(double daysToExpiry, double strikeOffset) throws VolatilitySurfaceException {
		if (interpolationFunction == null) {
			throw new VolatilitySurfaceException("VolatilitySurface: null interpolation function, build may have failed");
		}
		if (!isValidPoint(daysToExpiry, strikeOffset)) {
			throw new VolatilitySurfaceException("VolatilitySurface: interpolation function is not valid at daysToExpiry=" + daysToExpiry + ", strikeOffset=" + strikeOffset);	
		}
		return interpolationFunction.value(daysToExpiry, strikeOffset);
	}
	
	/**
	 * Return true if the specified point is within the ranges of the data
	 * from which the vol surface is built
	 * @param daysToExpiry
	 * @param strikeOffset
	 * @return
	 */
	public boolean isValidPoint(double daysToExpiry, double strikeOffset) {
		// BicubicInterpolatingFunction seems to have a bug here in that the indices
		// used are 1 and length - 2. This is at odds with what I see on Github for the latest
		// master, and makes no sense. I'm providing my own isValid instead.
		if (daysToExpiry < this.daysToExpiry[0] ||
			daysToExpiry > this.daysToExpiry[this.daysToExpiry.length -1] ||
			strikeOffset < this.strikeOffsets[0] ||
			strikeOffset > this.strikeOffsets[this.strikeOffsets.length - 1]) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Build the vol surface interpolation function
	 * @throws VolatilitySurfaceException
	 */
	public void build(VolatilitySurfaceDataSet dataSet) {
		buildStrikeOffsetsArray(dataSet);
		buildTimesToExpiryArray(dataSet);
		buildImpliedVolatityGrid(dataSet);
		buildInterpolationFunction();
	}
	
	public String getUniqueId() {
		return uniqueId;
	}
	
	public LocalDate getValueDate() {
		return valueDate; //dataSet.getValueDate();
	}
	
	private void buildInterpolationFunction() {
		try {
			this.interpolationFunction = interpolator.interpolate(daysToExpiry, strikeOffsets, impliedVolatilities);
			
		} catch (NumberIsTooSmallException|NonMonotonicSequenceException|DimensionMismatchException|NoDataException|InsufficientDataException e) {
			Log.getLogger().error("Error building volatility surface " + getUniqueId() + ": " + e.getMessage(), e);
			this.interpolationFunction = null;
		}
	}

	private void buildImpliedVolatityGrid(VolatilitySurfaceDataSet dataSet) {
		impliedVolatilities = new double[daysToExpiry.length][strikeOffsets.length];
		dataSet.getUnderlyingIdentifiers().forEach(
				underlyingId -> dataSet.getOptionData(underlyingId).forEach(
						optionData -> {
							try {
								calculateImpliedVolatility(
										underlyingId,
										dataSet.getUnderlyingPrice(underlyingId).getPrice(snapshotType),
										optionData,
										dataSet.getOptionPrice(optionData.getOptionId()).getPrice(snapshotType),
										dataSet.getDefinition().getCallOrPut());
							} catch (MissingReferenceDataException e) {
								Log.getLogger().error(e.getMessage(), e);
							}
						}
				)
			);
	}

	private void calculateImpliedVolatility(String underlyingId, double underlyingPrice, OptionData optionData,
			double optionPrice, Type callOrPut) throws MissingReferenceDataException {
		
		OptionReferenceData refData = OptionReferenceDataMap.getOptionReferenceData(optionData.getOptionId());
		
		double strike = refData.getStrike();
		long daysToExpiry = calculateDaysToExpiry(getValueDate(), refData.getExpiryDate());
		double timeToExpiry = ((double)daysToExpiry)/365.0;
		double riskFreeRate = refData.getRiskFreeRate();
		double dividendYield = refData.getDividendYield();
		double impliedVol = 0.0;
		if (callOrPut == Type.CALL) {
			impliedVol = BlackScholesModel.calcCallOptionImpliedVol(underlyingPrice, strike, optionPrice, timeToExpiry, riskFreeRate, dividendYield);
		} else {
			impliedVol = BlackScholesModel.calcPutOptionImpliedVol(underlyingPrice, strike, optionPrice, timeToExpiry, riskFreeRate, dividendYield);
		}
		
		int i = daysToExpiryIndices.get(Double.valueOf(daysToExpiry));
		int j = strikeOffsetIndices.get((Double)optionData.getAtmOffset());
		impliedVolatilities[i][j] = impliedVol;
	}

	private long calculateDaysToExpiry(LocalDate valueDate, LocalDate expiryDate) {
		long daysToExpiry = ChronoUnit.DAYS.between(valueDate, expiryDate);
		return daysToExpiry;
	}

	private void buildTimesToExpiryArray(VolatilitySurfaceDataSet dataSet) {
		daysToExpiry = new double[dataSet.getOptionExpiries().size()];
		int i = 0;
		for (LocalDate expiry : dataSet.getOptionExpiries()) {
			daysToExpiry[i] = (double)calculateDaysToExpiry(dataSet.getValueDate(), expiry);
			daysToExpiryIndices.put(Double.valueOf(daysToExpiry[i]), Integer.valueOf(i));
			++i;
		}
	}

	private void buildStrikeOffsetsArray(VolatilitySurfaceDataSet dataSet) {
		strikeOffsets = new double[dataSet.getDefinition().getStrikeOffsets().size()];
		int i = 0;
		for (double strikeOffset : dataSet.getDefinition().getStrikeOffsets()) {
			strikeOffsets[i] = strikeOffset;
			strikeOffsetIndices.put(Double.valueOf(strikeOffsets[i]), Integer.valueOf(i));
			++i;
		}
	}

	public double[][] getImpliedVolatilities() {
		return impliedVolatilities.clone();
	}

	public double[] getStrikeOffsets() {
		return strikeOffsets.clone();
	}
	
	public double[] getDaysToExpiry() {
		return daysToExpiry.clone();
	}

	public Type getOptionType() {
		return optionType;
	}

	public String getName() {
		return name;
	}

	public SnapshotType getSnapshotType() {
		return snapshotType;
	}

	public BivariateGridInterpolator getInterpolator() {
		return interpolator;
	}
	
	public double getMinDaysToExpiry() {
		return daysToExpiry[0];
	}
	
	public double getMaxDaysToExpiry() {
		return daysToExpiry[daysToExpiry.length - 1];
	}
	
	public double getMinStrikeOffset() {
		return strikeOffsets[0];
	}
	
	public double getMaxStrikeOffset() {
		return strikeOffsets[strikeOffsets.length - 1];
	}
}
