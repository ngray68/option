package com.ngray.option.mongo;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import static com.mongodb.client.model.Filters.*;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.ngray.option.Log;

public class MongoCache<T extends MongoObject> {

	private final Map<String, T> cache;
	private final MongoCollection<T> collection;
	
	public MongoCache(MongoCollection<T> collection) {
		Log.getLogger().debug("Creating MongoCache for " + collection.getDocumentClass().getSimpleName());
		this.cache = new ConcurrentHashMap<>();
		this.collection = collection;
	}
	
	/**
	 * Get the instance of T with uniqueId key
	 * @param key
	 * @return
	 */
	public T get(String key) {
		Log.getLogger().debug("MongoCache: get " + key);
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
		Log.getLogger().debug("MongoCache: get " + key + ", readthrough=" + readThrough);
		T obj = get(key);
		if (readThrough && obj == null) {
			Log.getLogger().debug("MongoCache: get " + key + ", reading through to Mongo DB");
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
		Log.getLogger().debug("MongoCache: put " + obj.getUniqueId());
		cache.put(obj.getUniqueId(), obj);
	}
	
	/**
	 * Put the instance obj of T in the cache and write through to the mongo db
	 * @param obj
	 * @param writeThrough
	 */
	public void put(T obj, boolean writeThrough) {
		Log.getLogger().debug("MongoCache: put " + obj.getUniqueId() + ", writethrough=" + writeThrough);
		put(obj);
		if (writeThrough) {
			Log.getLogger().debug("MongoCache: put " + obj.getUniqueId() + ", writing through to Mongo DB");
			FindOneAndReplaceOptions options = new FindOneAndReplaceOptions();
			options.upsert(true);
			collection.findOneAndReplace(eq(MongoObject.getUniqueIdKey(), obj.getUniqueId()), obj, options);
		}
	}
	
	public Collection<T> getAll() {
		Log.getLogger().debug("MongoCache: getAll()");
		return cache.values();
	}
	
	public Collection<T> getAll(boolean readThrough) {
		Log.getLogger().debug("MongoCache: getAll() with readthrough");
		MongoCursor<T> iterator = collection.find().iterator();
		while (iterator.hasNext()) {
			T obj = iterator.next();
			put(obj);
		}
		return getAll();
	}

	public void clear() {
		cache.clear();
	}
	
	public Set<String> getKeys() {
		return cache.keySet();
	}
}
