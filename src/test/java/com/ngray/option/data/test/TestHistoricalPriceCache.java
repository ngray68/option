package com.ngray.option.data.test;

import static org.junit.Assert.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoDatabase;
import com.ngray.option.RiskEngine;
import com.ngray.option.data.HistoricalPriceCache;
import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionException;
import com.ngray.option.ig.SessionLoginDetails;
import com.ngray.option.mongo.Price;
import com.ngray.option.mongo.PriceCodec;

public class TestHistoricalPriceCache {

	private Session session = null;
	private String  loginDetailsFile = "/Users/nigelgray/Documents/details.txt";
	
	MongoClient client;
	MongoDatabase db;
	HistoricalPriceCache cache = new HistoricalPriceCache("Test");
	
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
				CodecRegistries.fromCodecs(new PriceCodec(MongoClient.getDefaultCodecRegistry()))
			);
		 MongoClientOptions options = MongoClientOptions.builder().codecRegistry(codecRegistry)
		            .build();

		client = new MongoClient("localhost:27017", options);
		
		RiskEngine.setTestSession(session);
		RiskEngine.setTestMongoClient(client);
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
	public void testGetPrice() {
		testLoad();
		Price price = cache.getPrice(LocalDate.of(2017,8,1), "IX.D.FTSE.MONTH8.IP");
		assertNotNull(price);
	}

	public void testLoad() {	
		Set<String> identifiers = new TreeSet<>();
		identifiers.add("IX.D.FTSE.MONTH8.IP");
		identifiers.add("IX.D.FTSE.MONTH3.IP");
		LocalDate startDate = LocalDate.of(2017,8,1);
		LocalDate endDate = LocalDate.now();
		cache.load(startDate, endDate, identifiers);		
	}

}
