package com.ngray.option.mongo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
		doc.put("ValueDate", volSurface.getValueDate().toString());
		doc.put("SnapshotType", volSurface.getSnapshotType().toString());
		doc.put("OptionType", volSurface.getOptionType().toString());
		doc.put("Interpolator", volSurface.getInterpolator().getClass().getName());
		List<Double> daysToExpiry = Arrays.stream(volSurface.getDaysToExpiry()).boxed().collect(Collectors.toList());
		List<Double> strikeOffsets = Arrays.stream(volSurface.getStrikeOffsets()).boxed().collect(Collectors.toList());
		List<List<Double>> impliedVols = new ArrayList<>();
		double[][] impliedVolsArray = volSurface.getImpliedVolatilities();
		for (double[] impliedVolsRow : impliedVolsArray) {
			impliedVols.add(Arrays.stream(impliedVolsRow).boxed().collect(Collectors.toList()));
		}
		doc.put("DaysToExpiryAxis", daysToExpiry);
		doc.put("StrikeOffsetAxis", strikeOffsets);
		doc.put("ImpliedVols", impliedVols);
		documentCodec.encode(writer, doc, context);
	}

	@Override
	public Class<VolatilitySurface> getEncoderClass() {
		return VolatilitySurface.class;
	}

	@SuppressWarnings("unchecked")
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
		
		List<Double> daysToExpiryList = (List<Double>)doc.get("DaysToExpiryAxis");
		List<Double> strikeOffsetsList = (List<Double>)doc.get("StrikeOffsetsAxis");
		List<List<Double>> impliedVolsList = (List<List<Double>>)doc.get("ImpliedVols");
		double[] daysToExpiry = daysToExpiryList.stream().mapToDouble(Double::doubleValue).toArray();
		double[] strikeOffsets = strikeOffsetsList.stream().mapToDouble(Double::doubleValue).toArray();
		double[][] impliedVols = new double[impliedVolsList.size()][];
		int i = 0;
		for (List<Double> impliedVolsRow : impliedVolsList) {
			impliedVols[i] =  impliedVolsRow.stream().mapToDouble(Double::doubleValue).toArray();
			++i;
		}
		
		return new VolatilitySurface(uniqueId, name, valueDate, optionType, snapshotType, interpolator, daysToExpiry, strikeOffsets, impliedVols);
	}

}
