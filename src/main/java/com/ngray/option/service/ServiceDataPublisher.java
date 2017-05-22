package com.ngray.option.service;

public interface ServiceDataPublisher<K, V> {
	
	public void publish(K key, V value);

}
