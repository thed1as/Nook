package com.nooki.service.payment;

import com.nooki.entity.Booking;
import com.nooki.entity.Listing;
import com.nooki.entity.Payment;
import com.nooki.entity.User;
import com.nooki.enums.PaymentStatus;
import com.nooki.enums.Status;
import com.nooki.event.entities.BookingConfirmedEvent;
import com.nooki.event.entities.PaymentCompletedEvent;
import com.nooki.repository.BookingRepository;
import com.nooki.repository.PaymentRepository;
import com.nooki.service.PaymentServices.StripeWebhookHandler;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class StripeWebhookHandlerTest {

    @InjectMocks
    private StripeWebhookHandler stripeWebhookHandler;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Nested
    @DisplayName("handle payment success")
    class handlePaymentSuccess {
        private final String webhookSecret = "webhookSecret";
        private final String payload = "{}";
        private final String sigHeader = "signature";

        @Test
        @DisplayName("payment handle succesfull")
        void handlePaymentSuccess_shouldSavePaymentAsSuccess() throws SQLException {
            String stripeId = "pi_123";

            ReflectionTestUtils.setField(stripeWebhookHandler, "webhookSecret", webhookSecret);

            when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

            User user = new User();
            user.setEmail("host@gmail.com");

            Listing listing = new Listing();
            listing.setTitle("test title");

            Booking booking = new Booking();
            booking.setStatus(Status.PENDING);
            booking.setListing(listing);
            booking.setBookingId(UUID.randomUUID());
            booking.setUser(user);
            booking.setCheckInDate(LocalDateTime.of(1,1,1,1,1));
            booking.setCheckOutDate(LocalDateTime.of(1,1,1,1,1));
            booking.setTotalPrice(new BigDecimal("1000"));

            Payment payment = new Payment();
            payment.setPaymentId(UUID.randomUUID());
            payment.setStripeId(stripeId);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setAmount(new BigDecimal("1000"));
            payment.setCurrency("USD");

            payment.setBooking(booking);
            payment.setUser(user);

            try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {

                Event event = mock(Event.class);
                PaymentIntent paymentIntent = mock(PaymentIntent.class);
                EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);

                webhookMock.when(() ->
                                Webhook.constructEvent(payload, sigHeader, webhookSecret))
                        .thenReturn(event);

                when(event.getId()).thenReturn("evt_123");
                when(event.getType()).thenReturn("payment_intent.succeeded");
                when(event.getDataObjectDeserializer()).thenReturn(deserializer);
                when(deserializer.getObject()).thenReturn(Optional.of(paymentIntent));
                when(paymentIntent.getId()).thenReturn(stripeId);

                when(paymentRepository.findByStripeId(stripeId))
                        .thenReturn(Optional.of(payment));

                stripeWebhookHandler.handleWebHook(payload, sigHeader);

                ArgumentCaptor<Booking> bookingArgumentCaptor = ArgumentCaptor.forClass(Booking.class);
                ArgumentCaptor<Payment> paymentArgumentCaptor = ArgumentCaptor.forClass(Payment.class);
                verify(bookingRepository, times(1)).save(bookingArgumentCaptor.capture());
                verify(paymentRepository, times(1)).save(paymentArgumentCaptor.capture());

                Payment p = paymentArgumentCaptor.getValue();
                Booking b = bookingArgumentCaptor.getValue();

                assertThat(p.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
                assertThat(b.getStatus()).isEqualTo(Status.CONFIRMED);

                verify(applicationEventPublisher, times(1)).publishEvent(any(PaymentCompletedEvent.class));
                verify(applicationEventPublisher, times(1)).publishEvent(any(BookingConfirmedEvent.class));
            }
        }

        @Test
        @DisplayName("handle payment success payment_failed")
        void handlePaymentSuccess_shouldSavePaymentAsFailed() {
            String stripeId = "pi_123";
            Payment payment = new Payment();
            payment.setStatus(PaymentStatus.PENDING);

            ReflectionTestUtils.setField(stripeWebhookHandler, "webhookSecret", webhookSecret);

            when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

            try(MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
                Event event = mock(Event.class);
                PaymentIntent paymentIntent = mock(PaymentIntent.class);
                EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);

                webhookMock.when(() ->
                        Webhook.constructEvent(payload, sigHeader, webhookSecret))
                        .thenReturn(event);

                when(event.getId()).thenReturn("evt_123");
                when(event.getType()).thenReturn("payment_intent.payment_failed");
                when(event.getDataObjectDeserializer()).thenReturn(deserializer);
                when(deserializer.getObject()).thenReturn(Optional.of(paymentIntent));
                when(paymentIntent.getId()).thenReturn(stripeId);

                when(paymentRepository.findByStripeId(stripeId))
                        .thenReturn(Optional.of(payment));

                stripeWebhookHandler.handleWebHook(payload, sigHeader);

                ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

                verify(paymentRepository, times(1)).save(paymentCaptor.capture());

                Payment p = paymentCaptor.getValue();

                assertThat(p.getStatus()).isEqualTo(PaymentStatus.FAILED);
            }
        }
    }
}
