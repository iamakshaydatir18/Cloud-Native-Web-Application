package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.example.demo.RequestResponseObjects.UserRequest;
import com.example.demo.RequestResponseObjects.UserResponse;
import com.example.demo.RequestResponseObjects.UserUploadResponse;
import com.example.demo.model.ImageMetadata;
import com.example.demo.model.User;
import com.example.demo.service.ImageMetadataService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.S3Service;
import com.example.demo.service.UserService;
import com.example.demo.util.PasswordUtil;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	UserService userservice;

	@Autowired
	private S3Service s3Service;

	@Autowired
	ImageMetadataService imgService;

	@Autowired
	private final MeterRegistry meterRegistry;

	@Autowired
	NotificationService notificationService;

	Set<String> HashSet;
	private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@(example\\.com|gmail\\.com|yahoo\\.com|northeastern\\.edu|srv1.mail-tester\\.com|test\\.com)$";
	private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
	private final BCryptPasswordEncoder passwordEncoder;
	private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/png", "image/jpeg", "image/jpg");

	public UserController(MeterRegistry meterRegistry, UserService userservice, NotificationService notificationService) {
		HashSet = new java.util.HashSet<>();
		checkHeaders();
		this.passwordEncoder = new BCryptPasswordEncoder();
		this.meterRegistry = meterRegistry;
		this.userservice = userservice;
		this.notificationService = notificationService;

	}

	@RequestMapping(value = "/v1/user/**", method = { RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST,
			RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.OPTIONS })
	public ResponseEntity<Void> handleInvalidRequestForCreate() {
		logger.error("Header section Error Path Params!!!! {}");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
	}

	@RequestMapping(value = "/v1/user/self/**", method = { RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST,
			RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.OPTIONS })
	public ResponseEntity<Void> handleInvalidRequestForGet() {
		logger.error("Header section Error Path Params!!!! {}");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
	}

	@RequestMapping(value = "/v1/user", method = { RequestMethod.GET, RequestMethod.PUT, RequestMethod.DELETE,
			RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.OPTIONS })
	public ResponseEntity<Void> methodNotAllowedForUser() {

		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).header("cache-control", "no-cache").build();
	}

	@RequestMapping(value = "/v1/user/self/", method = { RequestMethod.HEAD, RequestMethod.DELETE,
			RequestMethod.OPTIONS, RequestMethod.PATCH, RequestMethod.POST })
	public ResponseEntity<Void> methodNotAllowedForUserForGet() {
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).header("cache-control", "no-cache").build();
	}

	@RequestMapping(value = "/v1/user/self", method = { RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PATCH,
			RequestMethod.HEAD, RequestMethod.OPTIONS })
	public ResponseEntity<Void> methodNotAllowedForGet() {
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).header("cache-control", "no-cache").build();
	}

	@RequestMapping(value = "/v1/user/self/pic", method = { RequestMethod.PATCH, RequestMethod.HEAD,
			RequestMethod.OPTIONS })
	public ResponseEntity<Void> methodNotAllowedForPostPic() {
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).header("cache-control", "no-cache").build();
	}

	@RequestMapping(value = "/v1/user/self/pic/**", method = { RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST,
			RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.OPTIONS })
	public ResponseEntity<Void> methodNotAllowedForPicApi() {
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).header("cache-control", "no-cache").build();
	}

	@Timed(value = "POST.User.time", description = "Time taken for POST User requests in this controller")
	@PostMapping({ "/v1/user", "/v1/user/" })
	public ResponseEntity<UserResponse> createUser(@RequestBody(required = false) UserRequest userRequest,
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@RequestParam(required = false) Map<String, String> queryParams, HttpServletRequest request,
			@RequestHeader Map<String, String> headers) {

		logger.info("Inside POST User Service!!!! ");
		meterRegistry.counter("POST.User.counter").increment();
//		for (String key : headers.keySet()) {
//
//			if (!HashSet.contains(key)) {
//				System.out.println("Header section Error!!!! " + key + ",");
//				logger.error("Header section Error!!!! {}", key);
//				return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
//			}
//		}

		if (!userservice.getConnection()) {
			logger.error("DB Connection ERROR !!!! {}");
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).header("cache-control", "no-cache").build();
		}

		if (request.getQueryString() != null || queryParams != null && !queryParams.isEmpty() || userRequest == null) {
			logger.error("Bad Request ERROR !!!! {}");
			System.out.println("Erorr inside  params, request Object, authHeader ");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();

		}

		if (authHeader != null && authHeader.startsWith("Basic")) {
			logger.error("Bad Request ERROR !!!! {}");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
		}

		System.out.println("user requested -- " + userRequest.toString());
		logger.info("user requested -- " + userRequest.toString());

		if (userRequest.getFirst_name() == null || userRequest.getFirst_name().length() < 3
				|| userRequest.getLast_name() == null || userRequest.getLast_name().length() < 3
				|| userRequest.getEmail() == null || userRequest.getEmail().length() < 13
				|| userRequest.getPassword() == null || userRequest.getPassword().length() < 6
				|| userservice.getUserByEmail(userRequest.getEmail()) != null) {
			logger.error("Bad request!!!!! Emaill all ready exits or fields are not passed.....");
			System.out.println("Bad request!!!!! Emaill all ready exits or fields are not passed.....");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
		}

		if (!checkEmail(userRequest.getEmail())) {
			logger.error("Password validation went wrong!!!!");
			System.out.println("Password validation went wrong!!!!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
		}

		User user = new User(userRequest.getFirst_name(), userRequest.getLast_name(), userRequest.getEmail(),
				userRequest.getPassword());

		User createdUser = userservice.createUser(user);

		UserResponse userResponse = new UserResponse(createdUser.getId(), createdUser.getFirst_name(),
				createdUser.getLast_name(), createdUser.getEmail(), createdUser.getAccount_created(),
				createdUser.getAccount_updated());
		logger.info("User Created !!!! " + userResponse.toString());

		try {

			notificationService.PublishMessage(createdUser);
			return ResponseEntity.status(HttpStatus.CREATED).header("cache-control", "no-cache").body(userResponse);

		} catch (Exception e) {
			logger.info("Failed to publish json object to Topic SNS " + e.getMessage());
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).header("cache-control", "no-cache").build();
		}

	}

	@Timed(value = "GET.User.time", description = "Time taken for Get User requests in this controller")
	@GetMapping({ "/v1/user/self", "/v1/user/self/" })
	public ResponseEntity<UserResponse> getUser(@RequestBody(required = false) String requestBody,
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@RequestParam(required = false) Map<String, String> queryParams, HttpServletRequest request,
			@RequestHeader Map<String, String> headers) {

		System.out.println("Authentication -- " + authHeader);
		logger.info("Inside Get User Service!!!! ");

		meterRegistry.counter("GET.User.counter").increment();
//		for (String key : headers.keySet()) {
//
//			if (!HashSet.contains(key)) {
//				System.out.println("Header section Error!!!! " + key + ",");
//				logger.error("Header section Error!!!! " + key + ",");
//				return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
//			}
//		}

		if (!userservice.getConnection()) {
			logger.error("DB Service unavailable Error!!!! ");
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).header("cache-control", "no-cache").build();
		}

		if (authHeader == null || request.getQueryString() != null || !queryParams.isEmpty()
				|| requestBody != null && !requestBody.isEmpty()) {
			logger.error("Bad Request Error!!!! ");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
		}

		User user = authenticateUser(authHeader);

		if (user != null) {

			if (!user.getEmailVerified()) {
				logger.error(" User NOT AUTHORIZED || INCOMPELETE EMAIL varifaction !!!! ");
				return ResponseEntity.status(HttpStatus.FORBIDDEN).header("cache-control", "no-cache").build();
			}

			UserResponse userResponse = new UserResponse(user.getId(), user.getFirst_name(), user.getLast_name(),
					user.getEmail(), user.getAccount_created(), user.getAccount_updated());
			logger.info("Found User!!!! " + userResponse.toString());
			return ResponseEntity.ok().header("cache-control", "no-cache").body(userResponse);
		}

		logger.error(" User NOT FOUND!!!! ");
		return ResponseEntity.status(HttpStatus.NOT_FOUND).header("cache-control", "no-cache").build();

	}

	@Timed(value = "PUT.User.time", description = "Time taken for PUT User requests in this controller")
	@PutMapping({ "/v1/user/self", "/v1/user/self/" })
	public ResponseEntity<UserResponse> UpdateUser(@RequestBody(required = false) UserRequest userRequest,
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@RequestParam(required = false) Map<String, String> queryParams, HttpServletRequest request,
			@RequestHeader Map<String, String> headers) {

		logger.info("Inside PUT User Request!!!! ");

		meterRegistry.counter("PUT.User.counter").increment();
//		for (String key : headers.keySet()) {
//
//			if (!HashSet.contains(key)) {
//				System.out.println("Header section Error!!!! " + key + ",");
//				logger.error("Header section Error!!!! " + key + ",");
//				return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
//			}
//		}

		if (request.getQueryString() != null || !queryParams.isEmpty() || userRequest == null || authHeader == null) {
			logger.error("BAD request");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
		}

		if (userRequest.getEmail() != null || userRequest.getEmail() != null && userRequest.getEmail().length() > 0) {
			logger.error("BAD request email invalid !!!!!!!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
		}

		if (userRequest.getFirst_name() == null || userRequest.getLast_name() == null
				|| userRequest.getPassword() == null) {
			logger.error("BAD request invalid !!!!!!!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
		}

		System.out.println("user request Body " + userRequest.toString());
		logger.info("user request Body " + userRequest.toString());

		if (!userservice.getConnection()) {
			logger.error("DB Connection Unvailable !!!!!!!" + userRequest.toString());
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).header("cache-control", "no-cache").build();
		}

		if (userRequest.getFirst_name() != null && userRequest.getFirst_name().length() < 3
				|| userRequest.getLast_name() != null && userRequest.getLast_name().length() < 3
				|| userRequest.getPassword() != null && userRequest.getPassword().length() < 6) {
			logger.error("Update Error!!!!! Emaill all ready exits or fields are not passed.....");
			System.out.println("Update Error!!!!! Emaill all ready exits or fields are not passed.....");

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
		}

		try {

			User user = authenticateUser(authHeader);
			// .out.println("user authenticated "+ user.toString());

			if (user != null) {

				if (!user.getEmailVerified()) {
					logger.error(" User NOT AUTHORIZED || INCOMPELETE EMAIL varifaction !!!! ");
					return ResponseEntity.status(HttpStatus.FORBIDDEN).header("cache-control", "no-cache").build();
				}

				if (userRequest.getFirst_name() != null)
					user.setFirst_name(userRequest.getFirst_name());
				if (userRequest.getLast_name() != null)
					user.setLast_name(userRequest.getLast_name());
				if (userRequest.getPassword() != null) {

					user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

				}

				userservice.updateUser(user);
				logger.info("user Updated  " + user.toString());
				return ResponseEntity.status(HttpStatus.NO_CONTENT).header("cache-control", "no-cache").build();

			} else {
				logger.error("user NOT FOUND  ");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).header("cache-control", "no-cache").build();
			}
		} catch (Exception e) {

			System.out.println("Exception - " + e.getMessage());
			logger.error("Exception - " + e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
		}

	}

	@Timed(value = "POST.S3Image.time", description = "Time taken for POST S3Image requests in this controller")
	@PostMapping("/v1/user/self/pic")
	public ResponseEntity<UserUploadResponse> uploadProfilePicture(
			@RequestParam(required = false, value = "profilePic") MultipartFile image,
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@RequestParam(required = false) Map<String, String> queryParams, HttpServletRequest request,
			@RequestHeader Map<String, String> headers) {

		logger.info("Inside S3 Image POST!!!!");
		meterRegistry.counter("POST.S3Image.counter").increment();
		// Image is null
		if (image == null || image.isEmpty()) {
			System.out.println("Inside Image is null");
			logger.error("Inside Image is null");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
		}

		String contentType = image.getContentType();
		if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
			logger.error("Invalid content Type!!!!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
		}

		// check if contains extra headers
//		for (String key : headers.keySet()) {
//
//			if (!HashSet.contains(key)) {
//				System.out.println("Header section Error!!!! " + key + ",");
//				logger.error("Header section Error!!!! " + key + ",");
//				return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
//			}
//		}

		// check if contains Query Parameters
		if (request.getQueryString() != null || !queryParams.isEmpty() || authHeader == null) {
			System.out.println("Inside Query param and authe header check!!!!!!");
			logger.error("Inside Query param and authe header check!!!!!!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
		}

		// Check if DB is Down
		if (!userservice.getConnection()) {
			System.out.println("Inside connectioned falied!!!!!");
			logger.error("Inside connectioned falied!!!!");
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).header("cache-control", "no-cache").build();
		}

		User user = authenticateUser(authHeader);

		if (user == null) {
			logger.error("User unauthorized!!!!");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("cache-control", "no-cache").build();
		}

		if (user != null) {
			if (!user.getEmailVerified()) {
				logger.error(" User NOT AUTHORIZED || INCOMPELETE EMAIL varifaction !!!! ");
				return ResponseEntity.status(HttpStatus.FORBIDDEN).header("cache-control", "no-cache").build();
			}
		}

		logger.info("Found user!!!!" + user.toString());
		ImageMetadata contains = imgService.getMetaData(user.getId());

		if (contains != null) {
			logger.error("Image  Allready exists for user!!!!" + contains.toString());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
		}

		try {

			System.out.println("user authenticated inside image upload" + user.toString());
			logger.info("user authenticated inside image upload" + user.toString());
			// Upload to S3 and get the response
			String fileName = image.getOriginalFilename();
			String fileUrl = s3Service.uploadFile(image, user.getId());

			ImageMetadata newMetaData = new ImageMetadata(UUID.randomUUID().toString(), fileName, fileUrl,
					LocalDateTime.now(), user.getId());
			imgService.createUser(newMetaData);

			// Create response object
			UserUploadResponse response = new UserUploadResponse();
			response.setFile_name(newMetaData.getFile_name());
			response.setId(newMetaData.getId());
			response.setUrl(newMetaData.getS3Url());
			response.setUpload_date(newMetaData.getUpload_date());
			response.setUser_id(newMetaData.getUser_id());

			logger.info("image uploaded ");
			return ResponseEntity.status(HttpStatus.OK).body(response);

		} catch (Exception e) {

			System.out.println("Exception is ---" + e.toString());
			logger.error("Exception is ---" + e.toString());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
		}
	}

	@Timed(value = "GET.S3Image.time", description = "Time taken for Get S3Image requests in this controller")
	@GetMapping("/v1/user/self/pic")
	public ResponseEntity<UserUploadResponse> getProfilePicture(@RequestBody(required = false) String requestBody,
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@RequestParam(required = false) Map<String, String> queryParams, HttpServletRequest request,
			@RequestHeader Map<String, String> headers) {

		logger.info("Inside get S3 Image Service!!!!!!!! ");
		meterRegistry.counter("GET.S3Image.counter").increment();

//		for (String key : headers.keySet()) {
//
//			if (!HashSet.contains(key)) {
//				System.out.println("Header section Error!!!! " + key + ",");
//				logger.error("Header section Error!!!! " + key + ",");
//				return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
//			}
//		}

		if (request.getQueryString() != null || !queryParams.isEmpty() || authHeader == null
				|| requestBody != null && !requestBody.isEmpty()) {
			System.out.println("Inside Query param and authe header check!!!!!!");
			logger.error("Inside Query param and authe header check!!!!!!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
		}

		if (!userservice.getConnection()) {
			System.out.println("Inside connectioned falied");
			logger.error("Inside connectioned falied!!!!");
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).header("cache-control", "no-cache").build();
		}

		User user = authenticateUser(authHeader);

		if (user == null) {
			logger.error("User unauthorized!!!!");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("cache-control", "no-cache").build();
		}

		if (user != null) {
			if (!user.getEmailVerified()) {
				logger.error(" User NOT AUTHORIZED || INCOMPELETE EMAIL varifaction !!!! ");
				return ResponseEntity.status(HttpStatus.FORBIDDEN).header("cache-control", "no-cache").build();
			}
		}

		logger.info("Found user!!!!" + user.toString());
		ImageMetadata newMetaData = imgService.getMetaData(user.getId());

		if (newMetaData == null) {
			logger.error("Image Meta data NOT found !!!!");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).header("cache-control", "no-cache").build();
		}

		logger.info("Image Meta data found !!!!" + newMetaData.toString());
		UserUploadResponse response = new UserUploadResponse();
		response.setFile_name(newMetaData.getFile_name());
		response.setId(newMetaData.getId());
		response.setUrl(newMetaData.getS3Url());
		response.setUpload_date(newMetaData.getUpload_date());
		response.setUser_id(newMetaData.getUser_id());
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Timed(value = "DELETE.S3Image.time", description = "Time taken for DELETE S3Image requests in this controller")
	@DeleteMapping("/v1/user/self/pic")
	public ResponseEntity<UserUploadResponse> deleteProfilePicture(@RequestBody(required = false) String requestBody,
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@RequestParam(required = false) Map<String, String> queryParams, HttpServletRequest request,
			@RequestHeader Map<String, String> headers) {

		logger.info("Inside delete S3 Image Service !!!!");
		meterRegistry.counter("DELETE.S3Image.counter").increment();

//		for (String key : headers.keySet()) {
//
//			if (!HashSet.contains(key)) {
//				System.out.println("Header section Error!!!! " + key + ",");
//				return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
//			}
//		}

		if (request.getQueryString() != null || !queryParams.isEmpty() || authHeader == null
				|| requestBody != null && !requestBody.isEmpty()) {
			System.out.println("Inside Query param and authe header check!!!!!!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("cache-control", "no-cache").build();
		}

		if (!userservice.getConnection()) {
			System.out.println("Inside connectioned falied");
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).header("cache-control", "no-cache").build();
		}

		User user = authenticateUser(authHeader);

		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("cache-control", "no-cache").build();
		}

		if (user != null) {
			if (!user.getEmailVerified()) {
				logger.error(" User NOT AUTHORIZED || INCOMPELETE EMAIL varifaction !!!! ");
				return ResponseEntity.status(HttpStatus.FORBIDDEN).header("cache-control", "no-cache").build();
			}
		}

		ImageMetadata newMetaData = imgService.getMetaData(user.getId());

		if (newMetaData == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).header("cache-control", "no-cache").build();
		}

		try {

			s3Service.deleteFileFromS3(newMetaData.getUser_id(), newMetaData.getFile_name());
			imgService.deleteMetadataByUserIdAndFileName(newMetaData.getUser_id());

			return ResponseEntity.status(HttpStatus.NO_CONTENT).header("cache-control", "no-cache").build();

		} catch (AmazonS3Exception e) {

			if (e.getStatusCode() == 404) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).header("cache-control", "no-cache").build();
			} else if (e.getStatusCode() == 403) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).header("cache-control", "no-cache").build();
			} else {
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).header("cache-control", "no-cache")
						.build();
			}
		} catch (Exception e) {
			System.out.println("Exception inside delete is -- " + e.toString());
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).header("cache-control", "no-cache").build();
		}

	}

	@GetMapping("/verify-email")
	public ResponseEntity<?> verifyEmail(@RequestParam String token) {
		logger.info("Request received to verify email id");
		if (userservice.verifyUserEmail(token)) {
			logger.info("Email verified successfully...");
			return ResponseEntity.ok().body("Email verified successfully.");
		} else {
			logger.info("Failed to verify email. || Email verification link has expired.");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email verification link has expired.");
		}
	}

	public User authenticateUser(String authHeader) {

		if (authHeader != null && authHeader.startsWith("Basic")) {

			String base64Credentials = authHeader.substring("Basic".length()).trim();

			String credentials = new String(Base64.getDecoder().decode(base64Credentials));
			final String[] values = credentials.split(":", 2);

			if (values.length == 2) {
				String username = values[0];
				String password = values[1];

				User user = userservice.getUserByEmail(username);

				if (user != null) {

					boolean existingPassword = PasswordUtil.verifyPassword(password, user.getPassword());

					if (existingPassword)
						return user;
					else {
						System.out.println("Passowrd did not matched!!!!!!");
					}

				}
			}
		}

		return null;
	}

	public void checkHeaders() {

		String[] headers = { "Authorization", "Content-Type", "Content-Length", "content-type", "content-length",
				"user-agent", "accept", "postman-token", "host", "accept-encoding", "connection", "authorization" };

		for (String head : headers) {
			HashSet.add(head);
		}

	}

	public boolean checkEmail(String email) {

		if (email == null) {
			return false;
		}

		Matcher matcher = pattern.matcher(email);
		return matcher.matches();

	}

}
