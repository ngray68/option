package com.ngray.option.ui.volsurfacetimeseries;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;

import com.ngray.option.analysis.timeseries.VolatilitySurfaceTimeSeriesStatistics;
import com.ngray.option.ui.components.Wizard;
import com.ngray.option.ui.components.WizardModel;

public class AnalysisOptionsWizardModel implements WizardModel {

	private double daysToExpiry;
	private Set<Double> atmOffsets;
	
	private boolean calcMaxValue;
	private boolean calcMinValue;
	private boolean calcMean;
	private boolean calcFiveDay;
	private boolean calcThirtyDay;
	private boolean calcNinetyDay;
	private VolatilitySurfaceChooserWizardModel volSurfaceChooserModel;
	private JFrame parentFrame;
	
	
	// If I were writing  wizard framework I would create a WizardData interface which wizards
	// would implement to allow all WizardModels access to all data but all I need here is for
	// the current model to access the vol surface choice data onFinish
	public AnalysisOptionsWizardModel(JFrame parentFrame, VolatilitySurfaceChooserWizardModel volSurfaceChooserModel) {
		this.volSurfaceChooserModel = volSurfaceChooserModel;
		setDaysToExpiry(Double.NaN);
		setCalcMaxValue(false);
		setCalcMinValue(false);
		setCalcMean(false);
		atmOffsets = new HashSet<>();
		this.parentFrame = parentFrame;
	}
	
	@Override
	public boolean validate() {
		// daysToExpiry and at least one atmOffset are not NaN
		// and at least one of calcMaxValue|calcMinValue|calcMean is true
		// or there is at least one moving average chosen
		return  (!Double.isNaN(daysToExpiry) && !atmOffsets.isEmpty()) &&
				       (calcMaxValue || calcMinValue || calcMean ||  calcFiveDay || calcThirtyDay || calcNinetyDay);
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
		VolatilitySurfaceTimeSeriesStatistics stats = new VolatilitySurfaceTimeSeriesStatistics(
				volSurfaceChooserModel.getVolatilitySurfaceName(),
				volSurfaceChooserModel.getSnapshotType(),
				volSurfaceChooserModel.getFromDate(),
				volSurfaceChooserModel.getToDate(),
				calcMinValue,
				calcMaxValue,
				calcMean,
				calcFiveDay,
				calcThirtyDay,
				calcNinetyDay,
				atmOffsets,
				daysToExpiry);
		stats.evaluate();
		VolatilitySurfaceTimeSeriesStatisticsViewer viewer = new VolatilitySurfaceTimeSeriesStatisticsViewer(parentFrame, stats);
		viewer.show();
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

	public boolean getCalcFiveDay() {
		return calcFiveDay;
	}

	public void setCalcFiveDay(boolean calcFiveDay) {
		this.calcFiveDay = calcFiveDay;
	}
	
	public boolean getCalcThirtyDay() {
		return calcThirtyDay;
	}

	public void setCalcThirtyDay(boolean calcThirtyDay) {
		this.calcThirtyDay = calcThirtyDay;
	}
	
	public boolean getCalcNinetyDay() {
		return calcNinetyDay;
	}

	public void setCalcNinetyDay(boolean calcNinetyDay) {
		this.calcNinetyDay = calcNinetyDay;
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
