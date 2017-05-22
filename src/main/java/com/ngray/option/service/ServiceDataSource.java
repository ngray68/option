package com.ngray.option.service;

/**
 * Generic interface a service data source.
 * @author nigelgray
 *
 * @param <K>
 * @param <V>
 */
public interface ServiceDataSource<K, V> {	
	
	/**
	 * Add a subscription to the given key
	 * @param key
	 * @param publisher
	 */
	public void addSubscription(K key, ServiceDataPublisher<K,V> publisher);
	
	/**
	 * Remove the subscription to the given key if it exists
	 * @param key
	 */
	public void removeSubscription(K key);
	
	/**
	 * Start the data source
	 */
	public void start();
	
	/**
	 * Shutdown the data source
	 */
	public void shutdown();
}
