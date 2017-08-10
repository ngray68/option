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
import com.ngray.option.mongo.VolatilitySurfaceDefinition;
import com.ngray.option.mongo.VolatilitySurfaceDefinitionCodec;

public class TestVolatilitySurfaceDefinition {

	MongoClient client;
	MongoDatabase db;
	
	@Before
	public void setUp() throws Exception {
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
				MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromCodecs(new VolatilitySurfaceDefinitionCodec(MongoClient.getDefaultCodecRegistry()))
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
		MongoCollection<VolatilitySurfaceDefinition> collection = db.getCollection("volatility_surface_definition", VolatilitySurfaceDefinition.class);
		FindIterable<VolatilitySurfaceDefinition> docs = collection.find(Filters.eq("Name", "FTSE100-VOL-SURFACE-DEFINITION"));
		VolatilitySurfaceDefinition volSurfaceDef = docs.first();	
		assertNotNull(volSurfaceDef);
	}
	
	@Test
	public void testFromMongoDocumentNoDocumentRetrieved() {
		MongoCollection<VolatilitySurfaceDefinition> collection = db.getCollection("volatility_surface_definition", VolatilitySurfaceDefinition.class);
		FindIterable<VolatilitySurfaceDefinition> docs = collection.find(Filters.eq("Name", "FTSE100-VOL-SURFACE-DEFINITION-NON-EXISTENT"));
		VolatilitySurfaceDefinition volSurfaceDef = docs.first();	
		assertNull(volSurfaceDef);
	}

}
