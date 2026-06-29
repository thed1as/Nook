package com.nooki.dto.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ValidError {
    private Map<String, String> errors;
}
