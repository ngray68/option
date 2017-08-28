package com.ngray.option.ui.volsurfacetimeseries;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.ngray.option.Log;
import com.ngray.option.analysis.timeseries.TimeSeriesException;
import com.ngray.option.analysis.timeseries.VolatilitySurfaceTimeSeries;
import com.ngray.option.ui.components.WizardModel;

public class AnalysisOptionsWizardModel implements WizardModel {

	private double daysToExpiry;
	private Set<Double> atmOffsets;
	
	private boolean calcMaxValue;
	private boolean calcMinValue;
	private boolean calcMean;
	private Set<Integer> movingAverages;
	private VolatilitySurfaceChooserWizardModel volSurfaceChooserModel;
	
	// If I were writing  wizard framework I would create a WizardData interface which wizards
	// would implement to allow all WizardModels access to all data but all I need here is for
	// the current model to access the vol surface choice data onFinish
	public AnalysisOptionsWizardModel(VolatilitySurfaceChooserWizardModel volSurfaceChooserModel) {
		this.volSurfaceChooserModel = volSurfaceChooserModel;
		setDaysToExpiry(Double.NaN);
		setCalcMaxValue(false);
		setCalcMinValue(false);
		setCalcMean(false);
		atmOffsets = new HashSet<>();
		movingAverages = new HashSet<>();
	}
	
	@Override
	public boolean validate() {
		// daysToExpiry and at least one atmOffset are not NaN
		// and at least one of calcMaxValue|calcMinValue|calcMean is true
		// or there is at least one moving average chosen
		return  (!Double.isNaN(daysToExpiry) && !atmOffsets.isEmpty()) &&
				       (calcMaxValue || calcMinValue || calcMean ||  !movingAverages.isEmpty());
	}

	@Override
	public void onBack() {
		// do nothing
	}

	@Override
	public void onNext() {
		// do nothing
	}

	@Override
	public void onShow() {
		// do nothing
	}
	
	@Override
	public void onFinish() {
		// we create a time series for all the vol surfaces and calculate all the chosen metrics
		try {
			VolatilitySurfaceTimeSeries volSurfaceTimeSeries 
					= VolatilitySurfaceTimeSeries.create(
									volSurfaceChooserModel.getVolatilitySurfaceName(), 
									volSurfaceChooserModel.getSnapshotType(),
									volSurfaceChooserModel.getFromDate(),
									volSurfaceChooserModel.getToDate()
								);
			for (double atmOffset : atmOffsets) {
				if (calcMinValue) {
					double min = volSurfaceTimeSeries.getImpliedVolatilityMinValue(daysToExpiry, atmOffset);
					System.out.println("Min=" + min);
				}
				if (calcMaxValue) {
					double max = volSurfaceTimeSeries.getImpliedVolatilityMaxValue(daysToExpiry, atmOffset);
					System.out.println("Max=" + max);
				}
				if (calcMean) {
					double mean = volSurfaceTimeSeries.getMeanImpliedVolatility(daysToExpiry, atmOffset);
					System.out.println("Mean=" + mean);
				}
			}
			
			
		} catch (TimeSeriesException e) {
			Log.getLogger().error(e.getMessage(), e);
			// TODO : UI work here
		}
	}

	public boolean getCalcMaxValue() {
		return calcMaxValue;
	}

	public void setCalcMaxValue(boolean calcMaxValue) {
		this.calcMaxValue = calcMaxValue;
	}

	public boolean getCalcMinValue() {
		return calcMinValue;
	}

	public void setCalcMinValue(boolean calcMinValue) {
		this.calcMinValue = calcMinValue;
	}

	public boolean getCalcMean() {
		return calcMean;
	}

	public void setCalcMean(boolean calcMean) {
		this.calcMean = calcMean;
	}

	public Set<Integer> getMovingAverages() {
		return Collections.unmodifiableSet(movingAverages);
	}

	public void addMovingAverage(int movingAverage) {
		movingAverages.add(movingAverage);
	}
	
	public void removeMovingAverage(int movingAverage) {
		movingAverages.remove(movingAverage);
	}

	public Set<Double> getAtmOffsets() {
		return Collections.unmodifiableSet(atmOffsets);
	}

	public void addAtmOffset(double atmOffset) {
		atmOffsets.add(atmOffset);
	}
	
	public double getDaysToExpiry() {
		return daysToExpiry;
	}

	public void setDaysToExpiry(double daysToExpiry) {
		this.daysToExpiry = daysToExpiry;
	}

	public void removeAtmOffset(double atmOffset) {
		atmOffsets.remove(atmOffset);
	}	
}
