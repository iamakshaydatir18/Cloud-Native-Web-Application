package serverless.lambda.function;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.time.Duration;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DatabaseSecretService {
	private final SecretsManagerClient secretsClient = SecretsManagerClient.builder()
			.overrideConfiguration(ClientOverrideConfiguration.builder().apiCallTimeout(Duration.ofSeconds(5))
					.apiCallAttemptTimeout(Duration.ofSeconds(2)).build())
			.build();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private static final Logger logger = Logger.getLogger(DatabaseSecretService.class.getName());
	
	
	public String getDatabasePassword() {
	    int maxRetries = 3;
	    int attempt = 0;
	    
	    logger.info("Inside Databse Secret Service for calling ");
	    
	    while (attempt < maxRetries) {
	        try {
	            GetSecretValueRequest request = GetSecretValueRequest.builder()
	                .secretId("db-password")
	                .build();
	            GetSecretValueResponse response = secretsClient.getSecretValue(request);
	            logger.info("Inside Databse Secret Service for calling ");
	            logger.info("response from  GetSecretValueResponse" + response.secretString());
	            
	            logger.info("Response from RDS Secret Value --" + objectMapper.readTree(response.secretString()).get("password").asText());
	            
	            return objectMapper.readTree(response.secretString()).get("password").asText();
	        } catch (Exception e) {
	            attempt++;
	            if (attempt == maxRetries) {
	            	 logger.info("Error retrieving database password after " + maxRetries + " attempts" +  e);
	                throw new RuntimeException("Error retrieving database password after " + maxRetries + " attempts", e);
	            }
	            try {
	                Thread.sleep(Math.min(100 * attempt, 1000));
	            } catch (InterruptedException ie) {
	                Thread.currentThread().interrupt();
	                throw new RuntimeException("Interrupted while retrying", ie);
	            }
	        }
	    }
	    logger.info("Unexpected error in retry loop");
	    throw new RuntimeException("Unexpected error in retry loop");
	}
	
}
