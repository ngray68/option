package com.ngray.option.mongo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.client.MongoCollection;
import com.ngray.option.Log;

public class MongoCacheRegistry {
	
	private static Map<Class<? extends MongoObject>, MongoCache<? extends MongoObject>> registry = new ConcurrentHashMap<>();;
	
	public static <T extends MongoObject> void register(Class<T> klass, String collectionName) {
		Log.getLogger().debug("MongoCacheRegistry: registering MongoCache<" + klass.getName() + ">");
		MongoCollection<T> collection =
				Mongo.getMongoDatabase(MongoConstants.DATABASE_NAME).getCollection(collectionName, klass);
		registry.put(klass, new MongoCache<T>(collection));
	}
	
	@SuppressWarnings("unchecked")
	/* We expect that the entry for a Class<T> will always be a MongoCache<T> if it is present.*/
	public static <T extends MongoObject> MongoCache<T> get(Class<T> klass) throws MongoCacheRegistryException {
		Log.getLogger().debug("MongoCacheRegistry: retrieving MongoCache<" + klass.getName() + ">");
		try {
			if (registry.containsKey(klass)) {
				return (MongoCache<T>)registry.get(klass);
			}
		} catch (ClassCastException e) {
			// not sure we need this, but it won't do any harm
			throw new MongoCacheRegistryException(e.getMessage());
		}
		
		throw new MongoCacheRegistryException("MongoCache<" + klass + "> not registered");
	}
}
