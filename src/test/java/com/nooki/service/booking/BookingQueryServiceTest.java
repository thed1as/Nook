package com.nooki.service.booking;

import com.nooki.dto.booking.BookingRequest;
import com.nooki.dto.booking.BookingResponse;
import com.nooki.dto.exception.customException.bookingException.BookingNotFoundException;
import com.nooki.entity.Booking;
import com.nooki.mapper.BookingMapper;
import com.nooki.repository.BookingRepository;
import com.nooki.service.BookingServices.BookingQueryService;
import com.nooki.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingQueryServiceTest {
    @InjectMocks
    private BookingQueryService bookingQueryService;

    @Mock
    private UserService userService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    private final UUID bookingId = UUID.randomUUID();

    @Test
    @DisplayName("Valid bookingId should return bookingResponse")
    void getBookingById_shouldReturnBookingResponse() {
        Booking booking = new Booking();
        BookingResponse expected = new BookingResponse();
        when(bookingRepository.findByDetailedId(bookingId))
                .thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingResponse(booking))
                .thenReturn(expected);

        BookingResponse result = bookingQueryService.getBookingById(bookingId);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Incorrect bookingId")
    void getBookingById_shouldThrowBookingNotFoundException() {
        when(bookingRepository.findByDetailedId(bookingId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingQueryService.getBookingById(bookingId))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessage("Booking not found with id: " + bookingId);
    }

    @Test
    @DisplayName("Valid request should return Page of BookingResponse")
    void getMyBookings_shouldReturnPageOfBookingResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        UUID userId = UUID.randomUUID();

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Page<UUID> ids = new PageImpl<>(List.of(id1, id2), pageable, 2);

        Booking booking1 = new Booking();
        Booking booking2 = new Booking();
        List<Booking> bookingsList = List.of(booking1, booking2);

        BookingResponse resp1 = new BookingResponse();
        BookingResponse resp2 = new BookingResponse();

        when(userService.getCurrentUserId()).thenReturn(userId);

        when(bookingRepository.findAllIdsOfUser(pageable, userId))
                .thenReturn(ids);

        when(bookingRepository.findUserBookings(List.of(id1, id2)))
                .thenReturn(bookingsList);

        when(bookingMapper.toBookingResponse(booking1)).thenReturn(resp1);
        when(bookingMapper.toBookingResponse(booking2)).thenReturn(resp2);

        Page<BookingResponse> result = bookingQueryService.getMyBookings(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent().get(0)).isEqualTo(resp1);
        assertThat(result.getContent().get(1)).isEqualTo(resp2);

        verify(userService).getCurrentUserId();
        verify(bookingRepository).findAllIdsOfUser(pageable, userId);
        verify(bookingRepository).findUserBookings(anyList());
        verify(bookingMapper, times(2)).toBookingResponse(any(Booking.class));
    }
}
