package com.example.tabelog.controller;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.tabelog.entity.Reservation;
import com.example.tabelog.entity.Restaurant;
import com.example.tabelog.entity.User;
import com.example.tabelog.form.ReservationInputForm;
import com.example.tabelog.form.ReservationRegisterForm;
import com.example.tabelog.repository.ReservationRepository;
import com.example.tabelog.repository.RestaurantRepository;
import com.example.tabelog.security.UserDetailsImpl;
import com.example.tabelog.service.ReservationService;
import com.example.tabelog.service.StripeService;
import com.stripe.exception.StripeException;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ReservationController {

	private final ReservationRepository reservationRepository;
	private final RestaurantRepository restaurantRepository;
	private final ReservationService reservationService;
	private final StripeService stripeService;

	public ReservationController(ReservationRepository reservationRepository, RestaurantRepository restaurantRepository,
			ReservationService reservationService, StripeService stripeService) {
		this.reservationRepository = reservationRepository;
		this.restaurantRepository = restaurantRepository;
		this.reservationService = reservationService;
		this.stripeService = stripeService;
	}

	//予約一覧ページへの遷移
	@GetMapping("/reservations")
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
			Model model) {
		User user = userDetailsImpl.getUser();
		Page<Reservation> reservationPage = reservationRepository.findByUserOrderByCreatedAtDesc(user, pageable);

		model.addAttribute("reservationPage", reservationPage);

		return "reservations/index";
	}

	//予約する
	@GetMapping("/restaurants/{id}/reservations/input")
	public String input(@PathVariable(name = "id") Integer id,
			@ModelAttribute @Validated ReservationInputForm reservationInputForm,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes,
			Model model) {
		Restaurant restaurant = restaurantRepository.getReferenceById(id);

		/*
		 // 予約可能な時間リストを生成してモデルに追加
		List<LocalTime> availableTimes = getAvailableTimes(restaurant.getOpenTime(), restaurant.getCloseTime());
		model.addAttribute("availableTimes", availableTimes);
		
		// 営業時間をビューに渡す
		model.addAttribute("openTime", restaurant.getOpenTime().toString());
		model.addAttribute("closeTime", restaurant.getCloseTime().toString());
		
		// 営業時間内の予約可能な時間を生成
			private List<LocalTime> getAvailableTimes(LocalTime openTime, LocalTime closeTime) {
			List<LocalTime> times = new ArrayList<>();
			
			// 営業時間がnullでないか確認
			if (openTime == null || closeTime == null) {
			System.out.println("Open or close time is null");
			return times;
			}
			
			LocalTime time = openTime;
			while (time.isBefore(closeTime) || time.equals(closeTime)) {
			times.add(time);
			time = time.plusHours(1); // 1時間刻みで追加
			}
			return times;
			}
		
		 */

		if (bindingResult.hasErrors()) {
			/*
			 // 予約可能な時間リストを再度モデルに追加
			List<LocalTime> availableTimes = getAvailableTimes(restaurant.getOpenTime(),
					restaurant.getCloseTime());
			model.addAttribute("availableTimes", availableTimes);
			
			// その他の必要なデータもモデルに追加
			model.addAttribute("restaurant", restaurant);
			model.addAttribute("reservationInputForm", reservationInputForm);
			
			// 営業時間もモデルに渡す
			model.addAttribute("formattedOpenTime", restaurant.getOpenTime());
			model.addAttribute("formattedCloseTime", restaurant.getCloseTime());
			
			 */
			model.addAttribute("restaurant", restaurant);
			model.addAttribute("errorMessage", "予約内容に不備があります。");
			return "restaurants/show";
		}

		redirectAttributes.addFlashAttribute("reservationInputForm", reservationInputForm);

		return "redirect:/restaurants/{id}/reservations/confirm";
	}

	//決済
	@GetMapping("/restaurants/{id}/reservations/confirm")
	public String confirm(@PathVariable(name = "id") Integer id,
			@ModelAttribute ReservationInputForm reservationInputForm,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			HttpServletRequest httpServletRequest,
			Model model) {
		Restaurant restaurant = restaurantRepository.getReferenceById(id);
		User user = userDetailsImpl.getUser();

		//チェックイン日時と人数を取得
		String checkinDate = reservationInputForm.getFromCheckinDateToCheckoutDate();
		
		/*わからない
		String checkinTime = reservationInputForm.getcheckinTime();
		*/
		Integer numberOfPeople = reservationInputForm.getNumberOfPeople();

		// 料金を計算する
		Integer price = restaurant.getPrice();
		Integer amount = reservationService.calculateAmount(price, numberOfPeople);

		ReservationRegisterForm reservationRegisterForm = new ReservationRegisterForm(restaurant.getId(), user.getId(),
				checkinDate.toString(), reservationInputForm.getNumberOfPeople(), amount);

		String sessionId = stripeService.createStripeSession(restaurant.getName(), reservationRegisterForm,
				httpServletRequest);

		model.addAttribute("restaurant", restaurant);
		model.addAttribute("reservationRegisterForm", reservationRegisterForm);
		model.addAttribute("sessionId", sessionId);

		return "reservations/confirm";
	}

	@GetMapping("/reservations/{reservationId}/delete")
	public String delete(@PathVariable Integer reservationId, RedirectAttributes redirectAttributes) {
		try {
			// 予約を削除する
			Reservation reservation = reservationService.findById(reservationId);
			if (reservation != null) {
				// StripeのCheckout Sessionをキャンセルする
				if (reservation.getSessionId() != null && !reservation.getSessionId().isEmpty()) {
					stripeService.expireCheckoutSession(reservation.getSessionId());
				}
				// 予約を削除
				reservationService.delete(reservationId);
				redirectAttributes.addFlashAttribute("successMessage", "予約がキャンセルされました。");
			} else {
				redirectAttributes.addFlashAttribute("errorMessage", "予約が見つかりませんでした。");
			}
		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "支払いのキャンセルに失敗しました。");
			e.printStackTrace();
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessgae", "予約の削除に失敗しました。");
			e.printStackTrace();
		}

		return "redirect:/reservations?reserved";
	}

	/*
	@PostMapping("/houses/{id}/reservations/create")
	public String create(@ModelAttribute ReservationRegisterForm reservationRegisterForm) {
	 reservationService.create(reservationRegisterForm);
	 
	 return "redirect:/reservations?reserved";
	}
	*/
}