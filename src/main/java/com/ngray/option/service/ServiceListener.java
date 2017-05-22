package com.ngray.option.service;

/**
 * Listener interface for the generic Service<K,V> class
 * @author nigelgray
 *
 * @param <K>
 * @param <V>
 */
public interface ServiceListener<K, V> {
	
	public void onUpdate(K key, V value);

}
