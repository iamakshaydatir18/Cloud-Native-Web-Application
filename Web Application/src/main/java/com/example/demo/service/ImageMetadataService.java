package com.example.demo.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.ImageMetadata;
import com.example.demo.respository.ImageMetadataRepository;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;


@Service
public class ImageMetadataService {
	
	@Autowired
	ImageMetadataRepository Imrepo;
	
	private final MeterRegistry meterRegistry;
	
	
	public ImageMetadataService(ImageMetadataRepository Imrepo,MeterRegistry meterRegistry) {
		this.Imrepo = Imrepo;
		this.meterRegistry = meterRegistry;
	}
	

	 public ImageMetadata createUser(ImageMetadata imageMeatadata) {
		 Timer.Sample sample = Timer.start(meterRegistry);
		 try {
			 return Imrepo.save(imageMeatadata);
		 }finally{
			 sample.stop(meterRegistry.timer("db.query.time", "operation", "createMetadata"));
		 }
	       
	 }
	 
	 public ImageMetadata getMetaData(String id) {
		
		 
		 Timer.Sample sample = Timer.start(meterRegistry);
		 try {
			 return Imrepo.findByUserId(id);
		 }finally{
			 sample.stop(meterRegistry.timer("db.query.time", "operation", "FindByUser_id"));
		 }
		 
	 }
	 
	 public void deleteMetadataByUserIdAndFileName(String id) {
		 System.out.println("Inside delete method !!!!! id is " + id);
		 
		 
		 Timer.Sample sample = Timer.start(meterRegistry);
		 try {
			 Imrepo.deleteByUserId(id);
		 }finally{
			 sample.stop(meterRegistry.timer("db.query.time", "operation", "DeleteMetaDatabyUser_id"));
		 }
	 }
	
	
}
