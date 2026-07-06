package com.community.dto;

import lombok.Data;

@Data
public class ServiceOrderReviewRequest {

    private Integer rating;

    private String comment;
}
