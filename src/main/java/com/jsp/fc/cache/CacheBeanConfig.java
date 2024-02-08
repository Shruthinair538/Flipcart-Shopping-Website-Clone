package com.jsp.fc.cache;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jsp.fc.entity.User;

@Configuration  //all bean objects are created in this configuration class
public class CacheBeanConfig {

	@Bean   //This bean can be autowired and used
	public CacheStore<User> userCacheStore(){
		return new CacheStore<User>(Duration.ofMinutes(10));
	}
	
	@Bean   
	public CacheStore<String> otpCacheStore(){
		return new CacheStore<String>(Duration.ofMinutes(1));
	}
}
