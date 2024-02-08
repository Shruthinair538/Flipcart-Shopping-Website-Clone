package com.jsp.fc.cache;

import java.time.Duration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class CacheStore<T> {  //to store user objects for a specific time
	
	private Cache<String, T> cache;
	
	public CacheStore(Duration expiry) {
		this.cache=CacheBuilder.newBuilder()
				.expireAfterWrite(expiry)
				.concurrencyLevel(Runtime.getRuntime().availableProcessors()) //resources
				.build();
	}
	
	public void add(String key,T value) {
		cache.put(key, value);
	}
	
	public T get(String key) {
		return cache.getIfPresent(key);	
	}
	
	public void remove(String key) {
		cache.invalidate(key);
	}

}
