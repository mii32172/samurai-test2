package com.example.tabelog.form;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReservationRegisterForm {    
    private Integer restaurantId;
        
    private Integer userId;    
        
    private String checkinDate;   
    
    /*
     private String checkinTime; // 予約時間を追加
     */
    
    private Integer numberOfPeople;
    
    private Integer amount; 
}
