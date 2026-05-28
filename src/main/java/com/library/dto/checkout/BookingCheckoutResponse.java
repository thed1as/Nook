package com.library.dto.checkout;

import com.library.dto.booking.BookingResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingCheckoutResponse {
    private BookingResponse bookingResponse;
    private String stripeId;
}
