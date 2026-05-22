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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
    @DisplayName("creating payment (post /payments")
    class CreatePayment {
        private final String URL = "/api/payments";
        private final UUID bookingId = UUID.randomUUID();

        @Test
        @DisplayName("Valid request should return 201 and create payment")
        @WithMockUser(roles = "USER")
        void validRequest_shouldReturn200AndCreatePayment() throws Exception {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setBookingId(bookingId);
            paymentRequest.setPaymentMethod(PaymentMethod.DEBIT_CARD);
            paymentRequest.setCurrency("USD");

            PaymentResponse paymentResponse = new PaymentResponse();

            when(paymentService.createPayment(any(PaymentRequest.class)))
                    .thenReturn(paymentResponse);

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(paymentRequest))
            ).andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Incorrect data should throw 400")
        @WithMockUser(roles = "USER")
        void incorrectData_shouldThrow400() throws Exception {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setBookingId(bookingId);
            paymentRequest.setCurrency("WRONG");
            paymentRequest.setPaymentMethod(PaymentMethod.DEBIT_CARD);

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(paymentRequest))
            ).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("anonymous user should throw 403")
        void anonymousUser_shouldThrow403() throws Exception {
            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new PaymentRequest()))
            ).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Booking not found should throw 404")
        @WithMockUser(roles = "USER")
        void bookingNotFound_shouldThrow() throws Exception {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setBookingId(bookingId);
            paymentRequest.setPaymentMethod(PaymentMethod.DEBIT_CARD);
            paymentRequest.setCurrency("USD");

            when(paymentService.createPayment(paymentRequest))
                    .thenThrow(EntityNotFoundException.class);

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(paymentRequest))
            ).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("refunding payment (post /payments)")
    class RefundPayment {
        private final UUID paymentId = UUID.randomUUID();
        private final String URL = "/api/payments/" + paymentId + "/refund";

        @Test
        @DisplayName("valid request should return 200 and paymentResponse")
        @WithMockUser(roles = "USER")
        void validRequest_shouldReturn200AndPaymentResponse() throws Exception {
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setReason("Refund testing");

            when(paymentService.refundPayment(refundRequest))
                    .thenReturn(new PaymentResponse());

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(refundRequest))
            ).andExpect(status().isOk());
        }

        @Test
        @DisplayName("not your payment should return 403")
        @WithMockUser(roles = "USER")
        void notYourPayment_shouldThrowForbiddenUserException() throws Exception {
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setReason("Refund testing");

            when(paymentService.refundPayment(any(RefundRequest.class)))
                    .thenThrow(ForbiddenUserException.class);

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(refundRequest))
            ).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("refund status isn't equal completed should throw 400")
        @WithMockUser(roles = "USER")
        void wrongRefundStatus_shouldThrowRefundNotAllowedException() throws Exception {
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setReason("Refund testing");

            when(paymentService.refundPayment(any(RefundRequest.class)))
                    .thenThrow(RefundNotAllowedException.class);

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(refundRequest))
            ).andExpect(status().isBadRequest());
        }
    }
}
