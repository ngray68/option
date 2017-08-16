package com.ngray.option.mongo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static com.mongodb.client.model.Filters.*;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.FindOneAndReplaceOptions;

public class MongoCache<T extends MongoObject> {

	private final Map<String, T> cache;
	private final MongoCollection<T> collection;
	
	public MongoCache(MongoCollection<T> collection) {
		this.cache = new ConcurrentHashMap<>();
		this.collection = collection;
	}
	
	/**
	 * Get the instance of T with uniqueId key
	 * @param key
	 * @return
	 */
	public T get(String key) {
		return cache.get(key);
	}
	
	/**
	 * Get the instance of T with the uniqueId key. Read through to the Mongo db if not present
	 * in the cache, cache the returned value (if not null) and return it.
	 * If the value is not present in the db, return null.
	 * @param key
	 * @param readThrough
	 * @return
	 */
	public T get(String key, boolean readThrough) {
		T obj = get(key);
		if (readThrough && obj == null) {
			MongoCursor<T> iterator = collection.find(eq(MongoObject.getUniqueIdKey(), key)).iterator();
			if (iterator.hasNext()) {
				obj = iterator.next();
				put(obj);
			}
		}
		
		return obj;
	}
	
	/**
	 * Put the instance obj of T in the cache
	 * @param obj
	 */
	public void put(T obj) {
		cache.put(obj.getUniqueId(), obj);
	}
	
	/**
	 * Put the instance obj of T in the cache and write through to the mongo db
	 * @param obj
	 * @param writeThrough
	 */
	public void put(T obj, boolean writeThrough) {
		put(obj);
		if (writeThrough) {
			FindOneAndReplaceOptions options = new FindOneAndReplaceOptions();
			options.upsert(true);
			collection.findOneAndReplace(eq(MongoObject.getUniqueIdKey(), obj.getUniqueId()), obj, options);
		}
	}
	
	public Iterable<T> getAll() {
		return cache.values();
	}
	
	public Iterable<T> getAll(boolean readThrough) {
		MongoCursor<T> iterator = collection.find().iterator();
		while (iterator.hasNext()) {
			T obj = iterator.next();
			put(obj);
		}
		return getAll();
	}
}
