package com.ngray.option.volatilitysurface.test;

import static org.junit.Assert.*;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.ngray.option.volatilitysurface.VolatilitySurfaceDefinition;

public class TestVolatilitySurfaceDefinition {

	MongoClient client;
	MongoDatabase db;
	
	@Before
	public void setUp() throws Exception {
		client = new MongoClient();
		db = client.getDatabase("testoptiondb");
	}
	
	@After
	public void tearDown() {
		if (client != null)
			client.close();
	}

	@Test
	public void testFromBsonDocument() {
		MongoCollection<Document> collection = db.getCollection("volatility_surface_definition");
		FindIterable<Document> docs = collection.find(Filters.eq("Name", "FTSE100-VOL-SURFACE-DEFINITION"));
		VolatilitySurfaceDefinition volSurfaceDef = VolatilitySurfaceDefinition.fromBsonDocument(docs.first());	
		assertNotNull(volSurfaceDef);
	}
	
	@Test(expected = NullPointerException.class)
	public void testFromBsonDocumentNoDocumentRetrieved() {
		MongoCollection<Document> collection = db.getCollection("volatility_surface_definition");
		FindIterable<Document> docs = collection.find(Filters.eq("Name", "FTSE100-VOL-SURFACE-DEFINITION-NON-EXISTENT"));
		VolatilitySurfaceDefinition volSurfaceDef = VolatilitySurfaceDefinition.fromBsonDocument(docs.first());	
		assertNotNull(volSurfaceDef);
	}

}
