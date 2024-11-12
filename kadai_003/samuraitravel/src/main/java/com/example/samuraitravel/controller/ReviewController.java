package com.example.samuraitravel.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.form.ReviewEditForm;
import com.example.samuraitravel.form.ReviewRegisterForm;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReviewRepository;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.ReviewService;

@Controller
@RequestMapping("houses/{houseId}/reviews")
public class ReviewController {
	private final ReviewService reviewService;
	private final ReviewRepository reviewRepository;
	private final HouseRepository houseRepository;

	public ReviewController(ReviewService reviewService, ReviewRepository reviewRepository,
			HouseRepository houseReository) {
		this.reviewService = reviewService;
		this.reviewRepository = reviewRepository;
		this.houseRepository = houseReository;
	}

	//レビュー一覧への遷移
	@GetMapping
	public String review(@PathVariable(name = "houseId") Integer houseId, Model model,
			@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
		Page<Review> reviewPage = reviewRepository.findByHouseId(houseId, pageable);
		House house = houseRepository.getReferenceById(houseId);

		model.addAttribute("house", house);
		model.addAttribute("reviewPage", reviewPage);

		return "reviews/review";
	}

	@GetMapping("/registerform")
	public String register(@PathVariable(name = "houseId") Integer houseId, Model model) {
		House house = houseRepository.getReferenceById(houseId);
		Review review = new Review();

		model.addAttribute("house", house);
		model.addAttribute("review", review);
		model.addAttribute("reviewRegisterForm", new ReviewRegisterForm());

		return "reviews/reviewRegister";
	}

	//レビューの作成
	@PostMapping("/create")
	public String create(@PathVariable(name = "houseId") Integer houseId,
			@ModelAttribute @Validated ReviewRegisterForm reviewRegisterForm, BindingResult bindingResult,
			RedirectAttributes redirectAttributes, Model model,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		House house = houseRepository.getReferenceById(houseId);
		Review review = new Review();

		

		if (bindingResult.hasErrors()) {
            model.addAttribute("house", house);
		    model.addAttribute("review", review);
			model.addAttribute("errorMessage", "投稿内容に不備があります。");
			return "reviews/reviewRegister";
		}

		reviewService.create(houseId, reviewRegisterForm, userDetailsImpl);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを登録しました。");

		return "redirect:/houses/{houseId}";
	}

	//レビューの編集
	@GetMapping("/{id}/edit")
	public String edit(@PathVariable(name = "houseId") Integer houseId, @PathVariable(name = "id") Integer id,
			Model model) {
		Review review = reviewRepository.getReferenceById(id);
		House house = houseRepository.getReferenceById(houseId);
		ReviewEditForm reviewEditForm = new ReviewEditForm(review.getId(), review.getStar(), review.getExplanation());

		model.addAttribute("review", review);
		model.addAttribute("house", house);
		model.addAttribute("reviewEditForm", reviewEditForm);

		return "reviews/edit";
	}

	//レビューの更新
	@PostMapping("/{id}/update")
	public String update(@PathVariable(name = "houseId") Integer houseId, @PathVariable(name = "id") Integer id,
			@ModelAttribute @Validated ReviewEditForm reviewEditForm, BindingResult bindingResult,
			RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			Model model) {
		Review review = reviewRepository.getReferenceById(id);
		House house = houseRepository.getReferenceById(houseId);

		if (bindingResult.hasErrors()) {
			model.addAttribute("house", house);
			model.addAttribute("review", review);

			return "reviews/edit";
		}

		reviewService.update(id, reviewEditForm, userDetailsImpl);
		redirectAttributes.addFlashAttribute("successMessage", "レビューの内容を編集しました。");

		return "redirect:/houses/{houseId}"; // 編集したら民宿詳細ページへ戻す
	}

	//レビューの削除
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable(name = "houseId") Integer houseId, @PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes, Model model) {
		reviewRepository.deleteById(id);

		redirectAttributes.addFlashAttribute("successMessage", "レビューを削除しました。");

		return "redirect:/houses/{houseId}"; // 編集したら民宿詳細ページへ戻す
	}

}