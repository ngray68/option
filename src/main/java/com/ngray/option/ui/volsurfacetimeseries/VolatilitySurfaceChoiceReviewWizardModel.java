package com.ngray.option.ui.volsurfacetimeseries;

import java.util.Iterator;
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
	
	public VolatilitySurfaceChoiceReviewWizardModel(JPanel panel) {
		this.panel = panel;
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
			}
			
			return result;
		} catch (MongoCacheRegistryException e) {
			Log.getLogger().error(e.getMessage(), e);
			return null;
		}
	}
}
