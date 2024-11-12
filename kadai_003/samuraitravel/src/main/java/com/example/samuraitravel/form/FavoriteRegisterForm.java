package com.example.samuraitravel.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FavoriteRegisterForm {

	@NotNull
	private Integer houseId;
	
	@NotNull
	private Integer userId;
	
}