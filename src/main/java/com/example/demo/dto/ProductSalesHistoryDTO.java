package com.example.demo.dto;

import java.time.LocalDateTime;

public record ProductSalesHistoryDTO(Integer productQuantity, LocalDateTime dateSold) {

}
