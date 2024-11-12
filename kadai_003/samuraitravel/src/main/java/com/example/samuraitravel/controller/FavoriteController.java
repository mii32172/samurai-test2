package com.example.samuraitravel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.repository.FavoriteRepository;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.FavoriteService;

@Controller
@RequestMapping("houses/{houseId}/favorites")
public class FavoriteController {

	@Autowired
	private final HouseRepository houseRepository;
	private final FavoriteService favoriteService;
	private final FavoriteRepository favoriteRepository;
	
	public FavoriteController(HouseRepository houseRepository, FavoriteService favoriteService, FavoriteRepository favoriteRepository) {
		this.houseRepository = houseRepository;
		this.favoriteService = favoriteService;
		this.favoriteRepository = favoriteRepository;
	}
	
	 /*お気に入り一覧への遷移*/
	@GetMapping("/favorites")
	public String show(Model model, @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
	User user = userDetailsImpl.getUser();
	Page<Favorite> favoritePage = favoriteRepository.findByUser(user, pageable);
	
	model.addAttribute("favoritePage", favoritePage);
	
	return "favorites/index";
	}
	
	/*お気に入りの追加*/
	@PostMapping("/add/{id}")
	public String add(@PathVariable(name = "houseId") Integer houseId, @PathVariable(name = "id") Integer id, Model model, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		favoriteService.add(userDetailsImpl, id);
	return "redirect:/houses/{houseId}"; 	
	}
	
	/*お気に入りの解除*/
	@PostMapping("/delete/{id}")
	public String delete(@PathVariable(name = "houseId") Integer houseId, @PathVariable(name = "id") Integer id, Model model, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		favoriteRepository.deleteById(id);
		
	return "redirect:/houses/{houseId}";
	}
}