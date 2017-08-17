package com.ngray.option.mongo;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoDatabase;
import com.ngray.option.volatilitysurface.VolatilitySurface;

public class Mongo {
	
	private static Mongo instance = new Mongo();
	private MongoClient client;
	
	private Mongo() {
	}
	
	private void initializeCacheRegistry() {
		MongoCacheRegistry.register(Price.class, MongoConstants.PRICE_COLLECTION);
		MongoCacheRegistry.register(VolatilitySurfaceDefinition.class, MongoConstants.VOLATILITY_SURFACE_DEFINITION_COLLECTION);
		MongoCacheRegistry.register(VolatilitySurface.class, MongoConstants.VOLATILITY_SURFACE_COLLECTION);
	}

	private void initializeMongoClient() {
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
				MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromCodecs(new VolatilitySurfaceDefinitionCodec(MongoClient.getDefaultCodecRegistry())),
				CodecRegistries.fromCodecs(new PriceCodec(MongoClient.getDefaultCodecRegistry())),
				CodecRegistries.fromCodecs(new VolatilitySurfaceCodec(MongoClient.getDefaultCodecRegistry()))

			);
		 MongoClientOptions options = MongoClientOptions.builder().codecRegistry(codecRegistry)
		            .build();

		// TODO - authentication
		client = new MongoClient("localhost:27017", options);
	}
	
	private MongoClient getClient() {
		return client;
	}

	public static MongoClient getMongoClient() {
		return instance.getClient();
	}
	
	public static MongoDatabase getMongoDatabase(String databaseName) {
		return instance.getClient().getDatabase(databaseName);
	}
	
	public static void initialize() {
		instance.initializeMongoClient();
		instance.initializeCacheRegistry();
	}
}
