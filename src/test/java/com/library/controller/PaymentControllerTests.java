package com.library.controller;

import com.library.config.SecurityConfig;
import com.library.dto.exception.customException.forbiden.ForbiddenUserException;
import com.library.dto.exception.customException.paymentExceptions.RefundNotAllowedException;
import com.library.dto.payment.PaymentRequest;
import com.library.dto.payment.PaymentResponse;
import com.library.dto.payment.RefundRequest;
import com.library.enums.PaymentMethod;
import com.library.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import({SecurityConfig.class})
@DisplayName("Testing Payment Controller")
public class PaymentControllerTests extends AbstractControllerTest{
    @MockitoBean
    private PaymentService paymentService;

    @Nested
    @DisplayName("Payment (GET /payments/booking/{bookingId}")
    class get {
        private final UUID bookingId = UUID.randomUUID();
        private final String URL = "/api/payments/booking/" + bookingId;

        @Test
        @DisplayName("Return 200, and page of paymentResponse")
        @WithMockUser(roles = "USER")
        void validRequest_shouldReturn200 () throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            PaymentResponse pr1 = new PaymentResponse();
            PaymentResponse pr2 = new PaymentResponse();
            List<PaymentResponse> ppl = List.of(pr1, pr2);
            Page<PaymentResponse> pageResponse = new PageImpl<>(ppl, pageable, ppl.size());

            when(paymentService.getPaymentsByBookingId(bookingId, pageable)).thenReturn(pageResponse);

            mockMvc.perform(get(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk());
        }
    }
}
