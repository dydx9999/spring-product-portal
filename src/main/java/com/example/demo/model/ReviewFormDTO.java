package com.example.demo.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewFormDTO {
    private UUID orderId;
    private Long orderItemId;
    private int rating;

}
