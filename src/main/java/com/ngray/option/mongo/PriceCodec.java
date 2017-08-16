package com.ngray.option.mongo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

public class PriceCodec implements Codec<Price> {

	private Codec<Document> documentCodec; 
	
	public PriceCodec(CodecRegistry registry) {
		documentCodec = registry.get(Document.class);
	}
	
	@Override
	public void encode(BsonWriter writer, Price price, EncoderContext context) {
		Document document = new Document();
		document.put("UniqueId", price.getUniqueId());
		document.put("Id", price.getId());
		document.put("ValueDate", price.getValueDate().toString());
		document.put("Open", price.getOpen());
		document.put("Close", price.getClose());
		document.put("Low", price.getLow());
		document.put("High", price.getHigh());
		document.put("Timestamp", price.getTimestamp().toString());
		documentCodec.encode(writer, document, context);		
	}

	@Override
	public Class<Price> getEncoderClass() {
		return Price.class;
	}

	@Override
	public Price decode(BsonReader reader, DecoderContext context) {
		Document document = documentCodec.decode(reader, context);
		String id = document.getString("Id");
		LocalDate valueDate = LocalDate.parse(document.getString("ValueDate"));
		double open = document.getDouble("Open");
		double close = document.getDouble("Close");
		double low = document.getDouble("Low");
		double high = document.getDouble("High");
		LocalDateTime timestamp = LocalDateTime.parse(document.getString("Timestamp"));
		return new Price(id, valueDate, open, close, low, high, timestamp);
	}

}
