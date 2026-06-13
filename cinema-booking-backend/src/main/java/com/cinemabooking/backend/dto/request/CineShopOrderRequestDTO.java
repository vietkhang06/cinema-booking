package com.cinemabooking.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CineShopOrderRequestDTO {
    private String itemName;
    private String itemImageUrl;
    private Integer quantity;
    private Double totalPrice;
    private String paymentMethod;
}
