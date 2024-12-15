package com.example.demo.model;


import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ImageMetadata {

    @Id
    private String id;
    private String file_name;
    private String s3Url;
    private LocalDateTime upload_date;
    private String user_id;
    
    
    
    
	public ImageMetadata() {
		super();
	}


	public ImageMetadata(String id,String file_name, String s3Url, LocalDateTime localDateTime, String user_id) {
		super();
		this.id = id;
		this.file_name = file_name;
		this.s3Url = s3Url;
		this.upload_date = localDateTime;
		this.user_id = user_id;
	}
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFile_name() {
		return file_name;
	}
	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}
	public String getS3Url() {
		return s3Url;
	}
	public void setS3Url(String s3Url) {
		this.s3Url = s3Url;
	}
	
	public LocalDateTime getUpload_date() {
		return upload_date;
	}


	public void setUpload_date(LocalDateTime upload_date) {
		this.upload_date = upload_date;
	}


	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

    
    
}
