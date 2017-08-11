package com.ngray.option.volatilitysurface.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;

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
import com.ngray.option.RiskEngine;
import com.ngray.option.data.HistoricalPriceCache;
import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionException;
import com.ngray.option.ig.SessionLoginDetails;
import com.ngray.option.ig.refdata.OptionReferenceDataMap;
import com.ngray.option.mongo.PriceCodec;
import com.ngray.option.mongo.VolatilitySurfaceDefinition;
import com.ngray.option.mongo.VolatilitySurfaceDefinitionCodec;
import com.ngray.option.volatilitysurface.VolatilitySurfaceDataSet;
import com.ngray.option.volatilitysurface.VolatilitySurfaceDataSetBuilder;
import com.ngray.option.volatilitysurface.VolatilitySurfaceException;

public class TestVolatilitySurfaceDataSetBuilder {

	private Session session = null;
	private String  loginDetailsFile = "/Users/nigelgray/Documents/details.txt";
	
	private MongoClient client;
	private MongoDatabase db;
	
	private HistoricalPriceCache cache = new HistoricalPriceCache("Test");
	
	private String readFile(String fileName) throws IOException {
		String result = "";
		try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			String nextLine = null;
			while ((nextLine = reader.readLine()) != null) {
				result += nextLine;
			}
		}
		return result;
	}
	
	@Before
	public void setUp() throws Exception {		
		boolean isLive = false;
		String json = readFile(loginDetailsFile);
		SessionLoginDetails loginDetails = SessionLoginDetails.fromJson(json);
		session = Session.login(loginDetails, isLive);	
		
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
				MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromCodecs(new PriceCodec(MongoClient.getDefaultCodecRegistry())),
				CodecRegistries.fromCodecs(new VolatilitySurfaceDefinitionCodec(MongoClient.getDefaultCodecRegistry()))
			);
		 MongoClientOptions options = MongoClientOptions.builder().codecRegistry(codecRegistry)
		            .build();

		client = new MongoClient("localhost:27017", options);
		db = client.getDatabase("optiondb");
		
		RiskEngine.setTestSession(session);
		RiskEngine.setTestMongoClient(client);
		RiskEngine.setHistoricalPriceCache(cache);
		String refDataFileName = "/OptionReferenceData*.csv";
		OptionReferenceDataMap.init(refDataFileName, session, true);
	}
	
	@After
	public void tearDown() {
		if (client != null) {
			client.close();
		}
		if (session != null) {
			try {
				session.logout();
			} catch (SessionException e) {
				e.printStackTrace();
			}
		}
	}
	@Test
	public void testBuild() throws VolatilitySurfaceException {
		MongoCollection<VolatilitySurfaceDefinition> collection = db.getCollection("volatility_surface_definition", VolatilitySurfaceDefinition.class);
		FindIterable<VolatilitySurfaceDefinition> docs = collection.find(Filters.eq("Name", "FTSE100-VOL-SURFACE-DEFINITION"));
		VolatilitySurfaceDefinition definition = docs.first();	
		assertNotNull(definition);
		VolatilitySurfaceDataSetBuilder builder = new VolatilitySurfaceDataSetBuilder(definition);
		VolatilitySurfaceDataSet dataSet = builder.build(LocalDate.now());
		assertNotNull(dataSet);	
	}

}
