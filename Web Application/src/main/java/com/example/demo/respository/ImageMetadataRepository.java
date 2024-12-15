package com.example.demo.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.ImageMetadata;




public interface ImageMetadataRepository extends JpaRepository<ImageMetadata, Long>{
	
	
	 @Query("SELECT i FROM ImageMetadata i WHERE i.user_id = :id")
	 ImageMetadata findByUserId(@Param("id") String id);
	 
	 @Modifying
	 @Transactional
	 @Query("DELETE FROM ImageMetadata i WHERE i.user_id = :id") 
	 void deleteByUserId(@Param("id") String id);

}
