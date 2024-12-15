package com.example.demo.RequestResponseObjects;

import java.time.LocalDateTime;

public class UserUploadResponse {
    private String file_name;
    private String id;
    private String url;
    private LocalDateTime upload_date;
    private String user_id;

  

    public UserUploadResponse() {
    }

    public UserUploadResponse(String error) {
        this.file_name = error;
    }

	public String getFile_name() {
		return file_name;
	}

	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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
