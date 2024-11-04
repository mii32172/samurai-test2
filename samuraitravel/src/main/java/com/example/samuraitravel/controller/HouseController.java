package com.example.samuraitravel.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.form.ReservationInputForm;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReviewRepository;
import com.example.samuraitravel.security.UserDetailsImpl;

@Controller
@RequestMapping("/houses")
public class HouseController {
	private final HouseRepository houseRepository;
	private final ReviewRepository reviewRepository;

	public HouseController(HouseRepository houseRepository,ReviewRepository reviewRepository) {
		this.houseRepository = houseRepository;
		this.reviewRepository = reviewRepository;
	}

	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword,
			//検索部分のパラメーター
			@RequestParam(name = "area", required = false) String area,
			@RequestParam(name = "price", required = false) Integer price,
			@RequestParam(name = "order", required = false) String order,
			@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
			Model model) {
		Page<House> housePage;
		//キーワードによる検索
		if (keyword != null && !keyword.isEmpty()) {
			if (order != null && order.equals("priceAsc")) {
				housePage = houseRepository.findByNameLikeOrAddressLikeOrderByPriceAsc("%" + keyword + "%",
						"%" + keyword + "%", pageable);
			} else {
				housePage = houseRepository.findByNameLikeOrAddressLikeOrderByCreatedAtDesc("%" + keyword + "%",
						"%" + keyword + "%", pageable);
			}
		//エリアによる検索
		} else if (area != null && !area.isEmpty()) {
			if (order != null && order.equals("priceAsc")) {
				housePage = houseRepository.findByAddressLikeOrderByPriceAsc("%" + area + "%", pageable);
			} else {
				housePage = houseRepository.findByAddressLikeOrderByCreatedAtDesc("%" + area + "%", pageable);
			}
		//価格による検索
		} else if (price != null) {
			if (order != null && order.equals("priceAsc")) {
				housePage = houseRepository.findByPriceLessThanEqualOrderByPriceAsc(price, pageable);
			} else {
				housePage = houseRepository.findByPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
			}
		//その他
		} else {
			if (order != null && order.equals("priceAsc")) {
				housePage = houseRepository.findAllByOrderByPriceAsc(pageable);
			} else {
				housePage = houseRepository.findAllByOrderByCreatedAtDesc(pageable);
			}
		}

		model.addAttribute("housePage", housePage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("area", area);
		model.addAttribute("price", price);
		model.addAttribute("order", order);

		return "houses/index";
	}
	


	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id, Model model,@PageableDefault(page = 0, size = 6, sort = "createdAt", direction = Direction.DESC) Pageable pageable,  @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		House house = houseRepository.getReferenceById(id);
		Page<Review> reviewPage = reviewRepository.findByHouseId(id, pageable);
		
		if(userDetailsImpl != null) {
		    Integer user = userDetailsImpl.getUser().getId();
		    List<Review> userHasReviews = reviewRepository.findByUserIdAndHouseId(user, id);
		    model.addAttribute("userHasReviews", !userHasReviews.isEmpty());
		} else {
		    List<Review> userHasReviews = reviewRepository.findByHouseId(id);
			model.addAttribute("userHasReviews", userHasReviews.isEmpty());		
		}
		    model.addAttribute("house", house);
		    model.addAttribute("reservationInputForm", new ReservationInputForm());
		
		    model.addAttribute("reviewPage", reviewPage);
		
		
		
		return "houses/show";
	}
	 
	 
}