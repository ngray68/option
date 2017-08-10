package com.ngray.option.mongo;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.EuropeanOption.Type;

public class VolatilitySurfaceDefinitionCodec implements Codec<VolatilitySurfaceDefinition> {

	private Codec<Document> documentCodec;
	
	public VolatilitySurfaceDefinitionCodec(CodecRegistry registry) {
		documentCodec = registry.get(Document.class);
	}
	
	@Override
	public void encode(BsonWriter writer, VolatilitySurfaceDefinition volatilitySurfaceDefinition, EncoderContext context) {
		Document document = new Document();
		document.put("Name", volatilitySurfaceDefinition.getName());
		document.put("ValidFrom", volatilitySurfaceDefinition.getValidFrom().toString());
		document.put("ValidTo", volatilitySurfaceDefinition.getValidTo().toString());
		document.put("Underlyings", volatilitySurfaceDefinition.getUnderlyingEpics());
		
		Map<String, String> optionEpicForms = new HashMap<>();
		volatilitySurfaceDefinition.getUnderlyingEpics().forEach(epic -> optionEpicForms.put(epic, volatilitySurfaceDefinition.getOptionEpicForm(epic)));
		document.put("OptionEpicForm", optionEpicForms);
		document.put("StrikeOffsets", volatilitySurfaceDefinition.getStrikeOffsets());
		document.put("CallOrPut", volatilitySurfaceDefinition.getCallOrPut().toString());
		documentCodec.encode(writer, document, context);
	}

	@Override
	public Class<VolatilitySurfaceDefinition> getEncoderClass() {
		return VolatilitySurfaceDefinition.class;
	}

	@Override
	public VolatilitySurfaceDefinition decode(BsonReader reader, DecoderContext context) {
		Document document = documentCodec.decode(reader, context);
		String name = document.getString("Name");
		LocalDate validFrom = LocalDate.parse(document.getString("ValidFrom"));
		
		LocalDate validTo = null;
		if (document.containsKey("ValidTo") && document.getString("ValidTo") != null) {
			validTo = LocalDate.parse(document.getString("ValidTo"));
		}
		@SuppressWarnings("unchecked") // we know this is a list of strings
		List<String> underlyingEpics = (List<String>)document.get("Underlyings");
		@SuppressWarnings("unchecked") // we know this is a map of string, string pairs
		Map<String, String> optionEpicForm = (Map<String, String>)document.get("OptionEpicForm");
		@SuppressWarnings("unchecked") // we know this is a list of doubles
		List<Double> strikeOffsets = (List<Double>)document.get("StrikeOffsets");
		EuropeanOption.Type callOrPut = document.getString("CallOrPut").equals("CALL") ? Type.CALL : Type.PUT;
		return new VolatilitySurfaceDefinition(name, validFrom, validTo, optionEpicForm, underlyingEpics, strikeOffsets, callOrPut);
	}

}
