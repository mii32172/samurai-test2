package com.example.samuraitravel.repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitravel.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer>{

	Page<Review> findByHouseId(Integer houseId, Pageable pageable);

	 List<Review> findByUserIdAndHouseId(Integer userId, Integer houseId);

	List<Review> findByHouseId(Integer id);

}