package com.library.service;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    private final String from = "nook@nook.mail";

    public void sendBookingConfirmation(
            String to,
            String listingTitle,
            LocalDateTime checkIn,
            LocalDateTime checkOut,
            BigDecimal totalAmount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("Booking confirmed - " + listingTitle);
            message.setText(buildBookingConfirmationText(
                    listingTitle, checkIn, checkOut, totalAmount
            ));

            mailSender.send(message);
            log.info("Booking confirmation sent to: {}", to);
        } catch (Exception e) {
            log.error("Error while sending email to: {}, {}", to, e.getMessage());
        }
    }

    public void sendPaymentConfirmation(
            String to,
            String listingTitle,
            BigDecimal amount,
            String currency) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("Payment confirmed - " + listingTitle);
            message.setText(buildPaymentCompletedText(
                    to, listingTitle, amount, currency
            ));

            mailSender.send(message);
            log.info("Payment confirmation sent to: {}", to);
        } catch (Exception e) {
            log.error("Error while sending email to: {}, {}", to, e.getMessage());
        }
    }

    public void sendBookingCancellation(String to, String listingTitle) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("Booking cancelled - " + listingTitle);
            message.setText(buildBookingCancellation(listingTitle));

            mailSender.send(message);
            log.info("Booking cancelation sent to: {}", to);
        } catch (Exception e) {
            log.error("Error while sending email to: {}, {}", to, e.getMessage());
        }
    }

    private String buildBookingCancellation(String listingTitle) {
        return """
                Hello, your booking was cancelled on listing: %s
                """.formatted(listingTitle);
    }

    private String buildPaymentCompletedText(String to, String listingTitle, BigDecimal amount, String currency) {
    return """
            Hello! %s
            
            Payment to this listing: %s was successfully completed!
                    %,.2f%s
            """.formatted(to, listingTitle, amount, currency);
    }

    private String buildBookingConfirmationText(String listingTitle, LocalDateTime checkIn, LocalDateTime checkOut, BigDecimal totalAmount) {
        return """
                Hello!
                
                Your booking has been confirmed!
                
                Listing: %s
                Check-in: %s
                Check-out: %s
                Total: %s
                
                Thank you for choosing Nook!
                """.formatted(listingTitle, checkIn, checkOut, totalAmount);
    }
}
