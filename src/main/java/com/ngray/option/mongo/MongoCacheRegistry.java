package com.ngray.option.mongo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.client.MongoCollection;

public class MongoCacheRegistry {
	
	private static Map<Class<? extends MongoObject>, MongoCache<? extends MongoObject>> registry = new ConcurrentHashMap<>();;
	
	public static <T extends MongoObject> void register(Class<T> klass, String collectionName) {
		MongoCollection<T> collection =
				Mongo.getMongoDatabase(MongoConstants.DATABASE_NAME).getCollection(collectionName, klass);
		registry.put(klass, new MongoCache<T>(collection));
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends MongoObject> MongoCache<T> get(Class<T> klass) throws MongoCacheRegistryException {
		if (registry.containsKey(klass)) {
			return (MongoCache<T>)registry.get(klass);
		}
		
		throw new MongoCacheRegistryException("MongoCache<" + klass + "> not registered");
	}
}
