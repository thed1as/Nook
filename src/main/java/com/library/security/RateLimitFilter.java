package com.library.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.exception.ApiError;
import com.library.config.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    public record RateLimiterRule(RequestMatcher matcher, int limit, int duration) {}

    private final RateLimitService rateLimiter;
    private final List<RateLimiterRule> rateLimiterRules;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimitService rateLimiter, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.rateLimiter = rateLimiter;

        var builder = PathPatternRequestMatcher.withDefaults();

        this.rateLimiterRules = List.of(
                new RateLimiterRule(builder.matcher(HttpMethod.POST, "/auth/login"), 7, 60),
                new RateLimiterRule(builder.matcher(HttpMethod.POST, "/auth/register"), 5, 60),

                new RateLimiterRule(builder.matcher(HttpMethod.POST, "/api/listing/reviews"), 20, 3600),
                new RateLimiterRule(builder.matcher(HttpMethod.PUT, "/api/listing/reviews/{id}"), 20, 3600),
                new RateLimiterRule(builder.matcher(HttpMethod.DELETE, "/api/listing/{id}/reviews"), 20, 3600),

                new RateLimiterRule(builder.matcher(HttpMethod.POST, "/api/listing"), 35, 3600),
                new RateLimiterRule(builder.matcher(HttpMethod.PUT, "/api/listing/{id}"), 100, 3600),
                new RateLimiterRule(builder.matcher(HttpMethod.DELETE, "/api/listing/{id}"), 20, 3600),

                new RateLimiterRule(builder.matcher(HttpMethod.POST, "/api/listings/{id}/images"), 150, 3600),

                new RateLimiterRule(builder.matcher(HttpMethod.POST, "/api/booking"), 30, 3600),
                new RateLimiterRule(builder.matcher(HttpMethod.DELETE, "/api/booking/{id}"), 20, 3600)
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();

        for(RateLimiterRule rule : rateLimiterRules) {
            if(rule.matcher().matches(request)) {
                if(!rateLimiter.isAllowed(ip, uri, rule.limit, rule.duration)) {
                    sendTooManyRequestsResponse(response);
                    return;
                }
                break;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendTooManyRequestsResponse(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiError apiError = new ApiError(429, "Too many requests", LocalDateTime.now());

        objectMapper.writeValue(response.getWriter(), apiError);
    }
}
