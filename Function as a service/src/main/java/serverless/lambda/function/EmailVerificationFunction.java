package serverless.lambda.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

public class EmailVerificationFunction implements RequestHandler<SNSEvent, String> {
	
	private static final Logger logger = Logger.getLogger(EmailVerificationFunction.class.getName());
    private final String SENDGRID_API_KEY;
    private static final String ENVIRONMENT = System.getenv("ENVIRONMENT");
    
    public EmailVerificationFunction() {
    	this.SENDGRID_API_KEY = new  SendGridService().getApiKey();
    }

    @Override
    public String handleRequest(SNSEvent event, Context context) {
    	
    	 DatabaseService databaseService = new DatabaseService();
    	
    	
        for (SNSEvent.SNSRecord record : event.getRecords()) {
            String message = record.getSNS().getMessage();
            context.getLogger().log("Inside Handle request Message Received is --- "+message);
            UserPayload userPayload = parseUserPayload(message); // Deserialize JSON to UserPayload class

            // Generate token and verification link
            String token = UUID.randomUUID().toString();
            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(2); // Expires in 2 minutes
            String verificationLink = "https://"+ENVIRONMENT+".springbreeze.me/verify-email?token=" + token;
            
            // Store verification details in RDS
            databaseService.storeVerificationDetails(userPayload.getEmail(), token, expirationTime);
 

            logger.info("calling send grid email method---");
            // Send verification email with SendGrid
            sendVerificationEmail(userPayload.getEmail(), userPayload.getFirst_name(), verificationLink);

            context.getLogger().log("Email verification link sent to: " + userPayload.getEmail());
        }
        return "Processed " + event.getRecords().size() + " records.";
    }

    private UserPayload parseUserPayload(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        try {
            return objectMapper.readValue(message, UserPayload.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse UserPayload from message: " + message, e);
        }
    }


    private void sendVerificationEmail(String email, String firstName, String verificationLink) {
        logger.info("Inside email ---");
        
        // Use a more user-friendly sender address
        Email from = new Email("support@em3309.springbreeze.me", "Spring Breeze Support");
        Email to = new Email(email);
        String subject = "Verify Your Email Address";
        
        // Personalize the email content
        String bodyContent = "Hello " + firstName + ",\n\n"
                + "Please click the link below to verify your email address:\n"
                + verificationLink + "\n\n"
                + "Thank you,\n"
                + "Spring Breeze Team";

        logger.info("Inside email details ---" + to + " from - " + from + " link -" + verificationLink);
        
        // Use plain text content instead of HTML to reduce the likelihood of being marked as spam
        Content plainTextContent = new Content("text/plain", bodyContent);
        Mail mail = new Mail(from, subject, to, plainTextContent);
        
        // Configure TrackingSettings to disable click tracking
        TrackingSettings trackingSettings = new TrackingSettings();
        ClickTrackingSetting clickTrackingSetting = new ClickTrackingSetting();
        clickTrackingSetting.setEnable(false); // Disable click tracking
        clickTrackingSetting.setEnableText(false); // Disable click tracking for plain text emails
        trackingSettings.setClickTrackingSetting(clickTrackingSetting);

        // Attach tracking settings to mail
        mail.setTrackingSettings(trackingSettings);

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();
        try {
            logger.info("sending email.....");

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            // Log the time before sending the email
            long startTime = System.currentTimeMillis();
            Response response = sg.api(request); // Send the email

            // Log the time after receiving response
            long endTime = System.currentTimeMillis();

            // Log status and response body
            logger.info("Email sent. Status code: " + response.getStatusCode());
            logger.info("Response body: " + response.getBody());
            logger.info("Time taken to send email: " + (endTime - startTime) + " ms");

            // Check if the email was sent successfully
            if (response.getStatusCode() != 202) {
                logger.severe("Failed to send email, status code: " + response.getStatusCode() + ", response body: " + response.getBody());
            } else {
                logger.info("Email sent successfully.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.severe("Error sending email: " + ex.getMessage());
        }
    }


}

class UserPayload {
	private String email;
	private String userId;
	private String first_name;
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime accountCreated;

	public UserPayload(String email, String first_name,String userId, LocalDateTime accountCreated) {
		this.email = email;
		this.userId = userId;
		this.first_name = first_name;
		this.accountCreated = accountCreated;
	}
	
	public UserPayload() {
    }
	// Getters and setters (or alternatively, use public fields if preferred)
	public String getEmail() {
		return email;
	}

	public String getUserId() {
		return userId;
	}

	public LocalDateTime getCreatedAt() {
		return accountCreated;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setCreatedAt(LocalDateTime accountCreated) {
		this.accountCreated = accountCreated;
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	@Override
	public String toString() {
		return "UserPayload [email=" + email + ", userId=" + userId + ", first_name=" + first_name + ", accountCreated="
				+ accountCreated + "]";
	}
	
}
