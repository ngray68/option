package com.ngray.option.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ngray.option.Log;

/**
 * Generic service class.
 * Services receive key, value pairs of type K,V from a publisher or publishers
 * and notify listeners that are subscribed to the service when an update occurs
 * @author nigelgray
 *
 * @param <K>
 * @param <V>
 */
public class Service<K, V> {

	private final String name;
	
	private final ConcurrentHashMap<K,V> cache;
	
	private final Object listenerLock;
	
	private final Map<K, List<ServiceListener<K,V>>> listeners;
	
	private final ServiceDataSource<K, V> dataSource;
	
	/**
	 * Construct a named service, with the given data source
	 * @param name
	 */
	public Service(String name, ServiceDataSource<K,V> dataSource) {
		this.name = name;
		this.cache = new ConcurrentHashMap<>();
		this.listenerLock = new Object();
		this.listeners = new HashMap<>();
		this.dataSource = dataSource;
	}
	
	/**
	 * Return the name of this service
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Return the listeners associated with the given key
	 * Returns an empty list if there are none
	 * @param key
	 * @return
	 */
	public List<ServiceListener<K,V>> getListeners(K key) {
		synchronized(listenerLock) {
			if (listeners.containsKey(key)) {
				return Collections.unmodifiableList(listeners.get(key));
			}
		}
		return new ArrayList<>();
	}
	
	/**
	 * Add a ServiceListener subscription for updates to the given key
	 * @param key
	 * @param listener
	 * @return
	 */
	public ServiceListener<K,V> addListener(K key, ServiceListener<K,V> listener) {
		Log.getLogger().info("Service " + getName() + ": adding subscription for " + key);
		if (key == null || listener == null) return null;
		
		synchronized(listenerLock) {	
			if (!listeners.containsKey(key)) {
				Log.getLogger().debug("Service " + getName() + ": creating new subscription list for " + key);
				listeners.put(key, new ArrayList<>());
				
				if (dataSource != null) {
					// as we now have a listener for the given key, we need to add a publisher to the data source
					// that will publish updates into this service
					dataSource.addSubscription(key, (k,v) -> { publishData(k, v); });
					
				}
			}
			
			Log.getLogger().debug("Service " + getName() + ": adding subscription to list for for " + key);
			listeners.get(key).add(listener);
		}
		
		return listener;
	}
	
	/**
	 * Remove a ServiceListener from the service
	 * @param key
	 * @param listener
	 */
	public void removeListener(K key, ServiceListener<K,V> listener) {
		Log.getLogger().info("Service " + getName() + ": removing subscription for " + key);
		if(key == null || listener == null) return;
		
		synchronized(listenerLock) {
			if (listeners.containsKey(key)) {
				listeners.get(key).remove(listener);
			}
			
			if (listeners.get(key).isEmpty()) {
				listeners.remove(key);
				if (dataSource != null) {
					dataSource.removeSubscription(key);
				}
			}
		}
	}
	
	/**
	 * Get the current value for the given key
	 * @param key
	 * @return
	 */
	public V getData(K key) throws ServiceException {
		Log.getLogger().info("Service " + getName() + ": getData Key: " + key);
		if (!cache.containsKey(key)) {
			throw new ServiceException("Service " + getName() + ": missing entry with key " + key);
		}
		return cache.get(key);
	}
	
	/**
	 * Publish the key, value pair and notify all listeners
	 * @param key
	 * @param value
	 */
	public void publishData(K key, V value) {
		Log.getLogger().info("Service " + getName() + ": publishData Key: " + key + "Value: " + value);
		cache.put(key, value);
		notifyListeners(key);
	}
	
	/**
	 * Start the service
	 */
	public void start() {
		Log.getLogger().info("Service " + getName() + ": start");
		dataSource.start();
	}
	
	/**
	 * Shutdown the service
	 */
	public void shutdown() {
		Log.getLogger().info("Service " + getName() + ": shutdown");
		listeners.clear();
		dataSource.shutdown();
	}
	
	/**
	 * Notify all listeners for the given key
	 * @param key
	 */
	protected void notifyListeners(K key) {
		Log.getLogger().info("Service " + getName() + ": notifying subscriptions to " + key);
		if (key == null) return;
		
		List<ServiceListener<K,V>> listenersCopy = null;
		synchronized(listenerLock) {
			if (listeners.containsKey(key)) {
				listenersCopy = new ArrayList<>(listeners.get(key));
			}
		}
		
		if (listenersCopy != null) {
			listenersCopy.forEach(listener -> listener.onUpdate(key, cache.get(key)));
		}		
	}

}
