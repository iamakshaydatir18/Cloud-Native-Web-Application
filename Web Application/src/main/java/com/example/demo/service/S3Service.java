package com.example.demo.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class S3Service {

    @Autowired
    private AmazonS3 s3Client;
    
    @Autowired
    private Environment env;
    
    private final MeterRegistry meterRegistry;
    
    public S3Service(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public String uploadFile(MultipartFile file, String userId) throws IOException {
    	
        String bucketName = env.getProperty("aws.S3.bucket_name");
        String fileName = userId + "/" + file.getOriginalFilename();
        
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        
        Timer timer = meterRegistry.timer("s3.uploadImage.time");
        timer.record(() -> {
	       	 try {
	       		 
				s3Client.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));
				
			} catch (IOException e) {
	
				e.printStackTrace();
		}
        });

        return s3Client.getUrl(bucketName, fileName).toString(); 
    }

	public void deleteFileFromS3(String user_id, String file_name) {
		
		 String bucketName =env.getProperty("aws.S3.bucket_name"); 
	     String key = user_id + "/" + file_name; 
	     
	     Timer timer = meterRegistry.timer("s3.DeleteImage.time");
	        timer.record(() -> {
	        		s3Client.deleteObject(new DeleteObjectRequest(bucketName, key));
	        });
	}
}

