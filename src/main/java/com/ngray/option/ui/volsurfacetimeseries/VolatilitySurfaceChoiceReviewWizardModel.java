package com.ngray.option.ui.volsurfacetimeseries;

import java.util.Arrays;
import java.util.Iterator;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.ngray.option.Log;
import com.ngray.option.mongo.MongoCache;
import com.ngray.option.mongo.MongoCacheRegistry;
import com.ngray.option.mongo.MongoCacheRegistryException;
import com.ngray.option.ui.components.WizardModel;
import com.ngray.option.volatilitysurface.VolatilitySurface;

public class VolatilitySurfaceChoiceReviewWizardModel implements WizardModel {

	private final JPanel panel;
	
	// store the ranges of the coordinates valid for all instances of the vol surface across the time series
	private double daysToExpiryMin;
	private double daysToExpiryMax;
	private double atmOffsetMin;
	private double atmOffsetMax;
	
	public VolatilitySurfaceChoiceReviewWizardModel(JPanel panel) {
		this.panel = panel;
		daysToExpiryMin = 0.0;
		daysToExpiryMax = Double.POSITIVE_INFINITY;
		atmOffsetMin = Double.NEGATIVE_INFINITY;
		atmOffsetMax = Double.POSITIVE_INFINITY;
	}
	@Override
	public boolean validate() {
		// TODO Auto-generated method stub
		return false;
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
		String[] columnNames = { "Name", "ValueDate", "OptionType", "SnapshotType" };
		Object[][] data = getChosenVolSurfaces(columnNames);
		
		JTable volSurfaceTable = new JTable(data, columnNames);
		JScrollPane scrollPane = new JScrollPane(volSurfaceTable);
		panel.removeAll();
		panel.add(scrollPane);
	}
	
	@Override
	public void onFinish() {
		// do nothing
	}
	
	public double getDaysToExpiryMin() {
		return daysToExpiryMin;
	}
	
	public double getDaysToExpiryMax() {
		return daysToExpiryMax;
	}
	
	public double getAtmOffsetMax() {
		return atmOffsetMax;
	}
	
	public double getAtmOffsetMin() {
		return atmOffsetMin;
	}

	private Object[][] getChosenVolSurfaces(String[] columnNames) {
		try {
			MongoCache<VolatilitySurface> cache = MongoCacheRegistry.get(VolatilitySurface.class);
			Set<String> keys = new TreeSet<>(cache.getKeys());
			Object[][] result = new Object[keys.size()][columnNames.length];
			
			Iterator<String> keyIter = keys.iterator();
			int row = 0;
			while(keyIter.hasNext()) {
				VolatilitySurface thisSurface = cache.get(keyIter.next());
				result[row][0] = thisSurface.getName();
				result[row][1] = thisSurface.getValueDate();
				result[row][2] = thisSurface.getOptionType();
				result[row][3] = thisSurface.getSnapshotType();
				++row;
				
				updateMinMaxValues(thisSurface);
			}
			
			return result;
		} catch (MongoCacheRegistryException e) {
			Log.getLogger().error(e.getMessage(), e);
			return null;
		}
	}
	
	private void updateMinMaxValues(VolatilitySurface thisSurface) {
		OptionalDouble maxDaysToExpiry = Arrays.stream(thisSurface.getDaysToExpiry()).max();
		OptionalDouble minDaysToExpiry = Arrays.stream(thisSurface.getDaysToExpiry()).min();
		if (maxDaysToExpiry.isPresent() && Double.compare(maxDaysToExpiry.getAsDouble(), this.daysToExpiryMax) < 0) {
				this.daysToExpiryMax = maxDaysToExpiry.getAsDouble();
		}
		if (minDaysToExpiry.isPresent() && Double.compare(minDaysToExpiry.getAsDouble(), this.daysToExpiryMin) > 0) {
			this.daysToExpiryMin = minDaysToExpiry.getAsDouble();
		}
		
		OptionalDouble maxAtmOffset = Arrays.stream(thisSurface.getStrikeOffsets()).max();
		OptionalDouble minAtmOffset = Arrays.stream(thisSurface.getStrikeOffsets()).min();
		if (maxAtmOffset.isPresent() && Double.compare(maxAtmOffset.getAsDouble(), this.atmOffsetMax) < 0) {
			this.atmOffsetMax = maxAtmOffset.getAsDouble();
		}
		if (minAtmOffset.isPresent() && Double.compare(minAtmOffset.getAsDouble(), this.atmOffsetMin) > 0) {
			this.atmOffsetMin = minAtmOffset.getAsDouble();
		}
	}
}
