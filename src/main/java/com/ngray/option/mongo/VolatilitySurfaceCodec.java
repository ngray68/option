package com.ngray.option.mongo;

import java.time.LocalDate;

import org.apache.commons.math3.analysis.interpolation.BivariateGridInterpolator;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import com.ngray.option.Log;
import com.ngray.option.financialinstrument.EuropeanOption.Type;
import com.ngray.option.mongo.Price.SnapshotType;
import com.ngray.option.volatilitysurface.VolatilitySurface;

public class VolatilitySurfaceCodec implements Codec<VolatilitySurface> {

	private Codec<Document> documentCodec;
	
	public VolatilitySurfaceCodec(CodecRegistry registry) {
		documentCodec = registry.get(Document.class);
	}
	
	@Override
	public void encode(BsonWriter writer, VolatilitySurface volSurface, EncoderContext context) {
		Document doc = new Document();
		doc.put("UniqueId", volSurface.getUniqueId());
		doc.put("Name", volSurface.getName());
		doc.put("ValueDate", volSurface.getValueDate());
		doc.put("SnapshotType", volSurface.getSnapshotType());
		doc.put("OptionType", volSurface.getOptionType().toString());
		doc.put("Interpolator", volSurface.getInterpolator().getClass().getName());
		doc.put("DaysToExpiryAxis", volSurface.getDaysToExpiry());
		doc.put("StrikeOffsetAxis", volSurface.getStrikeOffsets());
		doc.put("ImpliedVols", volSurface.getImpliedVolatilities());
		documentCodec.encode(writer, doc, context);
	}

	@Override
	public Class<VolatilitySurface> getEncoderClass() {
		return VolatilitySurface.class;
	}

	@Override
	public VolatilitySurface decode(BsonReader reader, DecoderContext context) {
		Document doc = documentCodec.decode(reader, context);
		String uniqueId = doc.getString("UniqueId");
		String name = doc.getString("Name");
		LocalDate valueDate = LocalDate.parse(doc.getString("ValueDate"));
		SnapshotType snapshotType = SnapshotType.fromString(doc.getString("SnapshotType"));
		Type optionType = Type.fromString(doc.getString("OptionType"));
		BivariateGridInterpolator interpolator = null;
		try {
			interpolator = (BivariateGridInterpolator)Class.forName(doc.getString("Interpolator")).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			Log.getLogger().error(e.getMessage(), e);
		}
		double[] daysToExpiry = (double[])doc.get("DaysToExpiryAxis");
		double[] strikeOffsets = (double[])doc.get("StrikeOffsetAxis");
		double[][] impliedVols = (double[][])doc.get("ImpliedVols");
		return new VolatilitySurface(uniqueId, name, valueDate, optionType, snapshotType, interpolator, daysToExpiry, strikeOffsets, impliedVols);
	}

}
