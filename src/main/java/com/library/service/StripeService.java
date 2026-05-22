package com.library.service;

import com.library.dto.exception.customException.paymentExceptions.PaymentFailedException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class StripeService {

    public String createPayment(BigDecimal amount, String currency) {
        try {
            long amountInCents = amount
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency.toLowerCase())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods
                                    .builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams
                                            .AutomaticPaymentMethods
                                            .AllowRedirects.NEVER)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return paymentIntent.getId();
        } catch (StripeException ex) {
            log.error("Stripe error during createPaymentIntent: {}", ex.getMessage());
            throw new PaymentFailedException("Payment failed: " + ex.getMessage());
        }
    }

    public void refundPayment(String stripePaymentId) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(stripePaymentId).build();

            Refund refund = Refund.create(params);

            log.info("Refund created: {}", refund.getId());
        } catch (StripeException ex) {
            log.error("Stripe error during refundPament: {}", ex.getMessage());
            throw new PaymentFailedException("Payment failed: " + ex.getMessage());
       }
    }

    public String getPaymentStatus(String stripePaymentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(stripePaymentId);
            return paymentIntent.getStatus();
        } catch (StripeException ex) {
            log.error("Stripe error during getPaymentStatus: {}", ex.getMessage());
            throw new PaymentFailedException("Payment failed: " + ex.getMessage());
        }
    }
}
