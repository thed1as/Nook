package com.library.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CurrencyResponse {
    private String base;
    private Map<String, Double> rates;
}
