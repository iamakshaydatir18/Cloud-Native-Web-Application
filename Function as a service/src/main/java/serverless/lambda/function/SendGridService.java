package serverless.lambda.function;

import java.util.logging.Logger;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class SendGridService {
    private final SecretsManagerClient secretsClient = SecretsManagerClient.create();
    private static final Logger logger = Logger.getLogger(SendGridService.class.getName());
    
    public String getApiKey() {
        GetSecretValueRequest request = GetSecretValueRequest.builder()
            .secretId("sendgrid-api-key-secret")
            .build();
        
        logger.info("Inside sendGrid secret key getter ..... " + request.toString() );
        GetSecretValueResponse response = secretsClient.getSecretValue(request);
        
        logger.info("return response APi Key from sendGridService ..... " + response.secretString() );
        return response.secretString();
    }
}
