package com.ngray.option.ui.volsurfacetimeseries;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;

import java.time.LocalDate;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.ngray.option.Log;
import com.ngray.option.financialinstrument.EuropeanOption.Type;
import com.ngray.option.mongo.Mongo;
import com.ngray.option.mongo.MongoCache;
import com.ngray.option.mongo.MongoCacheRegistry;
import com.ngray.option.mongo.MongoCacheRegistryException;
import com.ngray.option.mongo.MongoConstants;
import com.ngray.option.mongo.Price.SnapshotType;
import com.ngray.option.ui.components.WizardModel;
import com.ngray.option.volatilitysurface.VolatilitySurface;

public class VolatilitySurfaceChooserWizardModel implements WizardModel {

	private String volSurfaceName;
	private LocalDate fromDate;
	private LocalDate toDate;
	private SnapshotType snapshotType;
	private Type optionType;
	
	public VolatilitySurfaceChooserWizardModel() {
	}

	@Override
	public boolean validate() {
		// validate choice for the current panel and enable the next button if valid
		if (this.volSurfaceName != null && this.fromDate != null && this.toDate != null && this.snapshotType != null &&
				fromDate.compareTo(toDate) <= 0) {
			return true;
		}
		return false;
	}

	@Override
	public void onBack() {
		// do nothing
	}

	@Override
	public void onNext() {
		// Get the chosen vol surfaces from Mongo and fill the cache
		Log.getLogger().info("Loading Volatility Surface cache from Mongo DB");
		MongoCache<VolatilitySurface> cache = null;
		try {
			cache = MongoCacheRegistry.get(VolatilitySurface.class);
			cache.clear();
			MongoCollection<VolatilitySurface> volSurfaceCollection = Mongo.getMongoDatabase(MongoConstants.DATABASE_NAME).getCollection(MongoConstants.VOLATILITY_SURFACE_COLLECTION, VolatilitySurface.class);
			Bson filter = and(eq(VolatilitySurface.NAME_COL, volSurfaceName), gte(VolatilitySurface.VALUE_DATE_COL, fromDate.toString()), lte(VolatilitySurface.VALUE_DATE_COL, toDate.toString()));
			MongoCursor<VolatilitySurface> cursor = volSurfaceCollection.find(filter).iterator();
			while(cursor.hasNext()) {
				VolatilitySurface surface = cursor.next();
				cache.put(surface);
				setOptionType(surface.getOptionType());
			}
		} catch (MongoCacheRegistryException e) {
			Log.getLogger().error(e.getMessage(), e);
		}
	}

	@Override
	public void onShow() {
		// do nothing
	}

	
	public void setSnapshotType(SnapshotType snapshotType) {
		this.snapshotType = snapshotType;
	}

	public void setToDate(LocalDate date) {
		this.toDate = date;
	}

	public void setFromDate(LocalDate date) {
		this.fromDate = date;
	}

	public void setVolSurfaceName(String string) {
		this.volSurfaceName = string;
	}

	@Override
	public void onFinish() {
		// do nothing
	}

	public String getVolatilitySurfaceName() {
		return volSurfaceName;
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

	public Type getOptionType() {
		return optionType;
	}
	
	private void setOptionType(Type optionType) {
		this.optionType = optionType;
	}
}
