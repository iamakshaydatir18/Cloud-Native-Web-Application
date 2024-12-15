package serverless.lambda.function;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.logging.Logger;

public class DatabaseService {

	private static final Logger logger = Logger.getLogger(DatabaseService.class.getName());
	private final String dbUser = System.getenv("DB_USER");
	private final String dbPass = new DatabaseSecretService().getDatabasePassword();
	private final String dbName = System.getenv("DB_NAME");
	private final String dbHost = System.getenv("DB_HOST");

	public DatabaseService() {
		try {
			// Ensure the JDBC driver is available
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logger.severe("MySQL JDBC Driver is not found. Include it in your library path ");
			e.printStackTrace();
			return;
		}
	}

	public void storeVerificationDetails(String email, String token, LocalDateTime expiryTime) {
		// Build the JDBC URL
		String jdbcUrl = "jdbc:mysql://" + dbHost + "/" + dbName;
		logger.info("DB Url is  " + jdbcUrl);
		// SQL statement to update the user
		String sql = "UPDATE user SET email_verification_token = ?, email_verification_token_expiry = ? WHERE email = ?";

		try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {

			logger.info("DB Connection formed execuring query ---");
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, token);
			stmt.setTimestamp(2, Timestamp.valueOf(expiryTime));
			stmt.setString(3, email);

			int affectedRows = stmt.executeUpdate();
			logger.info("Query executed.......  row affected ---" + affectedRows);
			if (affectedRows > 0) {
				logger.info("User verification token updated successfully for " + email);
			} else {
				logger.warning("No user found with the username: " + email);
			}
		} catch (SQLException e) {
			logger.severe("Database update failed for " + email + ": " + e.getMessage());
		}
	}
}
