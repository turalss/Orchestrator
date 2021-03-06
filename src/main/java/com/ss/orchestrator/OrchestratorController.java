package com.ss.orchestrator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping(
	produces = { "application/json", "application/xml", "text/xml" },
	consumes = MediaType.ALL_VALUE
)
public class OrchestratorController {

	private final String SERVICE_PATH_ORCHESTRATOR = "http://localhost:8080";
	private final String SERVICE_PATH_AIRPORTS = "http://AIRPORT-SERVICE";
	private final String SERVICE_PATH_ROUTES = "http://ROUTE-SERVICE";
	private final String SERVICE_PATH_USERS = "http://USER-SERVICE";

	@Autowired
	DiscoveryClient discoveryClient;

  @Autowired
	RestTemplate restTemplate;
  
  @RequestMapping(path = { "/airports", "/airports/**" })
	public ResponseEntity<String> airports(RequestEntity<String> incomingRequest) {
		return rerouteToService(incomingRequest, SERVICE_PATH_AIRPORTS);
	}

	@RequestMapping(path = { "/routes", "/routes/**" })
	public ResponseEntity<String> routes(RequestEntity<String> incomingRequest) {
		return rerouteToService(incomingRequest, SERVICE_PATH_ROUTES);
	}

	@RequestMapping(path = { "/users", "/users/**" })
	public ResponseEntity<String> users(RequestEntity<String> incomingRequest) {
		return rerouteToService(incomingRequest, SERVICE_PATH_USERS);
	}

	@RequestMapping(path = { "/services"})
	public ResponseEntity<String> services(RequestEntity<String> incomingRequest) {
		List<String> services = discoveryClient.getServices();
		return services != null
		? new ResponseEntity<>(services.toString(), HttpStatus.OK)
		: new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
	}

	private ResponseEntity<String> rerouteToService(RequestEntity<String> incomingRequest, String destinationPath) {
		String newURI = incomingRequest.getUrl().toString()
		.replace(SERVICE_PATH_ORCHESTRATOR, destinationPath);
		RequestEntity<String> outgoingRequest = RequestEntity
		.method(incomingRequest.getMethod(), newURI)
		.headers(incomingRequest.getHeaders())
		.body(incomingRequest.getBody());
		
		try {
			return restTemplate.exchange(outgoingRequest, String.class);
		} catch (HttpStatusCodeException e) {
			return ResponseEntity.status(e.getRawStatusCode())
			.headers(e.getResponseHeaders())
			.body(e.getResponseBodyAsString());
		}
	}
}