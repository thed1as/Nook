package com.library.service.PaymentServices;

import com.library.dto.exception.customException.paymentExceptions.PaymentFailedException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class StripeService {


    public String createPayment(BigDecimal amount, String currency) {
        try {
            long amountInCents = amount.setScale(2, RoundingMode.HALF_UP)
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
            log.error("Stripe API error during createPaymentIntent for amount: {}", amount, ex);
            throw new PaymentFailedException("Payment failed: " + ex.getMessage());
        }
    }

    public void refundPayment(String stripePaymentId) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(stripePaymentId).build();

            Refund refund = Refund.create(params);

            log.info("Refund successfully created in Stripe: {}", refund.getId());
        } catch (StripeException ex) {
            log.error("Stripe API error during refundPayment for stripe Id: {}", stripePaymentId, ex);
            throw new PaymentFailedException("Payment failed: " + ex.getMessage());
       }
    }

    public String getPaymentStatus(String stripePaymentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(stripePaymentId);
            return paymentIntent.getStatus();
        } catch (StripeException ex) {
            log.error("Stripe API error during getPaymentStatus for stripe with Id: {} ", stripePaymentId, ex);
            throw new PaymentFailedException("Payment failed: " + ex.getMessage());
        }
    }
}
