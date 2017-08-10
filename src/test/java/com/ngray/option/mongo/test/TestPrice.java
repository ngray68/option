package com.ngray.option.mongo.test;

import static org.junit.Assert.*;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.ngray.option.mongo.Price;
import com.ngray.option.mongo.PriceCodec;

public class TestPrice {

	MongoClient client;
	MongoDatabase db;
	
	@Before
	public void setUp() throws Exception {
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
				MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromCodecs(new PriceCodec(MongoClient.getDefaultCodecRegistry()))
			);
		 MongoClientOptions options = MongoClientOptions.builder().codecRegistry(codecRegistry)
		            .build();

		client = new MongoClient("localhost:27017", options);
		db = client.getDatabase("testoptiondb");
	}
	
	@After
	public void tearDown() {
		if (client != null)
			client.close();
	}

	@Test
	public void testFromMongoDocument() {
		MongoCollection<Price> collection = db.getCollection("price", Price.class);
		FindIterable<Price> docs = collection.find(Filters.eq("Id", "TestPrice"));
		Price price = docs.first();	
		assertNotNull(price);
	}
	
	@Test
	public void testFromMongoDocumentNoDocumentRetrieved() {
		MongoCollection<Price> collection = db.getCollection("price", Price.class);
		FindIterable<Price> docs = collection.find(Filters.eq("Id", "NonExistent"));
		Price price = docs.first();	
		assertNull(price);
	}
}
