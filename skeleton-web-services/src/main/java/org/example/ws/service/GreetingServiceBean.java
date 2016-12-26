package org.example.ws.service;

import java.util.Collection;

import org.example.ws.model.Greeting;
import org.example.ws.repository.GreetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class GreetingServiceBean implements GreetingService {
	
	@Autowired
	GreetingRepository greetingrepository;

	public Collection<Greeting> findAll() {
		Collection<Greeting> greetings = greetingrepository.findAll();
		return greetings;
	}

	@Cacheable(value = "greetings" , key = "#id")
	public Greeting findOne(Long id) {

		Greeting greeting = greetingrepository.findOne(id);
		return greeting;
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	@CachePut(value = "greetings" , key = "#return.id")
	public Greeting create(Greeting greeting) {

		if(greeting.getId() != null) {
			return null;
		}
		
		Greeting savedGreeting = greetingrepository.save(greeting);
		return savedGreeting;
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	@CachePut(value = "greetings" , key = "#greeting.id")
	public Greeting update(Greeting greeting) {

		Greeting greetingPersisted = greetingrepository.findOne(greeting.getId());
		
		if(greetingPersisted == null) {
			return null;
		}
		
		Greeting updatedGreeting = greetingrepository.save(greeting);
		return updatedGreeting;
	}

	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	@CacheEvict(value = "greetings" , key = "#id")
	public void delete(Long id) {
		greetingrepository.delete(id);
	}
	
	@CacheEvict(value = "greetings", allEntries = true)
	public void evictCache() {
		
	}
	
}
