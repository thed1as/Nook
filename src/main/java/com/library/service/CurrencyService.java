package com.library.service;

import com.library.dto.CurrencyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final StringRedisTemplate redisTemplate;

    private static final String REDIS_KEY_PREFIX = "currency:rates:";
    private static final String API_URL = "https://api.frankfurter.app/latest?from=";

    public BigDecimal convert(BigDecimal amount, String from, String to) {
        if(from.equalsIgnoreCase(to)) {
            return amount;
        }

        BigDecimal rate = getExchangeRate(from, to);
        log.debug("Converting amount {} from {} to {}", amount, from, to);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getExchangeRate(String from, String to) {
        String cacheKey = REDIS_KEY_PREFIX + from.toUpperCase();

        String cachedRate = (String) redisTemplate.opsForHash().get(cacheKey, to.toUpperCase());
        if(cachedRate != null) {
            return new BigDecimal(cachedRate);
        }

        updateCurrencyRates(from);

        String updatedRate = (String) redisTemplate.opsForHash().get(cacheKey, to.toUpperCase());
        if(updatedRate == null) {
            log.warn("User tried to use unsupported currency {}", to.toUpperCase());
            throw new IllegalArgumentException("Unsupported currency: " + to);
        }

        return new BigDecimal(updatedRate);
    }

    private void updateCurrencyRates(String baseCurrency) {
        String url = API_URL + baseCurrency.toUpperCase();
        try {
            CurrencyResponse response = restTemplate.getForObject(url, CurrencyResponse.class);
            if(response != null && response.getRates() != null) {
                String cacheKey = REDIS_KEY_PREFIX + baseCurrency.toUpperCase();

                response.getRates().forEach((currency, rates) ->
                        redisTemplate.opsForHash().put(cacheKey, currency.toUpperCase(), rates.toString())
                );

                redisTemplate.expire(cacheKey, Duration.ofHours(12));

                log.info("Created cache for currency {}", baseCurrency);
            }
        } catch (Exception e) {
            log.error("Frankfurter API failed {}", baseCurrency, e);
            throw new RuntimeException("failed from rate external API: " + e);
        }
    }

}
