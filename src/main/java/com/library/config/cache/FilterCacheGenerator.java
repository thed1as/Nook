package com.library.config.cache;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component("filterCacheKeyGenerator")
public class FilterCacheGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        String currency = (String) params[0];
        Object filter = params[1];
        Object pageable = params[2];

        return String.format("%s:curr:%s:filt:%s:page:%s",
                method.getName(),
                currency,
                filter.hashCode(),
                pageable.toString().replace(" ", ""));
    }
}
