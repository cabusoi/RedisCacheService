package com.example.services.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

@SpringBootApplication
@EnableCaching
// @EnableDiscoveryClient
@RestController("/cache")
public class ProgramCacheApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProgramCacheApplication.class, args);
	}

	private Map<String, DefaultRedisMap<String, Object>> manager = new HashMap<>();
	private StringRedisTemplate template;

	public ProgramCacheApplication(StringRedisTemplate template) {
		this.template = template;
	}

	@PutMapping("/")
	public void create(@RequestParam("name") String name, @RequestParam("ttl") int ttl) {
		DefaultRedisMap<String, Object> redisMap = new DefaultRedisMap<>(name, template);
		redisMap.expire(ttl, TimeUnit.SECONDS);
		manager.put(name, redisMap);
	}

	@DeleteMapping("/")
	public void clear(@RequestParam("name") String name, @RequestParam(name = "key", defaultValue = "") String key) {
		DefaultRedisMap<?, ?> cache = manager.get(name);
		if (cache == null) {
			return;
		}
		if ("".equals(key)) {
			cache.clear();
		} else {
			cache.remove(key);
		}
	}

	@PostMapping("/")
	public void put(@RequestParam("name") String name, @RequestParam(name = "key") String key,
			@RequestBody(required = true) JsonNode value) {
		DefaultRedisMap<String, Object> cache = manager.get(name);
		if (cache == null) {
			return;
		}
		cache.put(key, value);
	}

	@GetMapping("/")
	public Object get(@RequestParam("name") String name, @RequestParam(name = "key") String key) {
		DefaultRedisMap<String, Object> cache = manager.get(name);
		if (cache == null) {
			return null;
		}
		return cache.get(key);
	}

}
