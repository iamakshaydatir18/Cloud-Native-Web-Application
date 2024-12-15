package com.example.demo.controller;


import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.service.HealthCheckService;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;


@RestController
public class HealthController {
	
	
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
    private HealthCheckService healthCheckService;
	
	@Autowired
	private final MeterRegistry meterRegistry;
	
	Set<String> HashSet;
	
	public HealthController(MeterRegistry meterRegistry, HealthCheckService healthCheckService) {
		this.meterRegistry = meterRegistry;
		this.healthCheckService = healthCheckService;
		HashSet = new java.util.HashSet<>();
		checkHeaders();
		
	}

	@Timed(value = "api.request.time", description = "Time taken for all API requests in this controller")
	@GetMapping("/healthz")
    public ResponseEntity<Void> checkHealth(
            @RequestParam(required = false) Map<String, String> queryParams,
            @RequestBody(required = false) String body, HttpServletRequest request
    ) {
       
		logger.info("Inside Health check APi Service!!!!!!!!!!");
        if (request.getQueryString() != null || !queryParams.isEmpty() || (body != null && !body.isEmpty())) {
        	logger.error("Bad request !!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
        }
        
//        for(String key : headers.keySet()) {
//			
//			if(!HashSet.contains(key)) {
//				System.out.println("Header section Error!!!! "+ key+",");
//				logger.error("Header section Error!!!! "+ key+",");
//				 return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
//			}
//		}

        if (healthCheckService.isDatabaseConnected()) {
        	logger.error("Database is Available !!!! ");
            return ResponseEntity.ok().header("cache-control", "no-cache").build();
        } else {
        	logger.error("Database is Unavailable!!!! ");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).header("cache-control", "no-cache").build();
        }
    }
	
	 @RequestMapping(value = "/healthz", method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH , RequestMethod.HEAD, RequestMethod.OPTIONS})
	    public ResponseEntity<Void> methodNotAllowed() {
	        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).header("cache-control", "no-cache").build();
	    }
	 
	 
	 public void checkHeaders() {
			
			
			String[] headers = {"content-type" ,"content-length", "user-agent" , "accept" , "postman-token" , "host" , "accept-encoding" , "connection" , "authorization" };
			
			for(String head : headers) {
				HashSet.add(head);
			}
			
		}
    
    
}

