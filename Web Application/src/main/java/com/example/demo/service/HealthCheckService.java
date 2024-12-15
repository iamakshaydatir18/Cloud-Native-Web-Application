package com.example.demo.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Service
public class HealthCheckService {

    @Autowired
    private DataSource dataSource;
    
    private final MeterRegistry meterRegistry;
    
    public HealthCheckService(MeterRegistry meterRegistry) {
    	this.meterRegistry = meterRegistry;
    }

    public boolean isDatabaseConnected() {
    	
    	Timer.Sample sample = Timer.start(meterRegistry);
   
        try (Connection connection = dataSource.getConnection()) {
        	 return connection.isValid(1);
        } catch (SQLException e) {
            return false;
        }finally {
            sample.stop(meterRegistry.timer("db.query.time", "operation", "getDBConnection"));
        }
    }
}

