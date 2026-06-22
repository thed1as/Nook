//package com.library.repository;
//
//import com.library.dto.listing.ListingFilterRequest;
//import com.library.entity.Booking;
//import com.library.entity.Listing;
//import com.library.entity.ListingSpecification;
//import com.library.entity.Location;
//import com.library.enums.Status;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//public class ListingSpecificationServiceTest extends BaseIntegrationTest{
//
//    @Autowired
//    private ListingRepository listingRepository;
//
//    @Autowired
//    private BookingRepository bookingRepository;
//
//    @Autowired
//    private LocationRepository locationRepository;
//
//    private Listing createListing(String title, BigDecimal price, String city, String country) {
//        Location loc = new Location();
//        loc.setCity(city);
//        loc.setCountry(country);
//        loc.setAddress("test address");
//        locationRepository.save(loc);
//
//        Listing l = new Listing();
//        l.setTitle(title);
//        l.setPricePerNight(price);
//        l.setLocation(loc);
//        return l;
//    }
//
//    private Booking createBooking(Listing listing, Status status, LocalDateTime checkIn, LocalDateTime checkOut) {
//        Booking b = new Booking();
//        b.setListing(listing);
//        b.setStatus(status);
//        b.setCheckInDate(checkIn);
//        b.setCheckOutDate(checkOut);
//        return b;
//    }
//
//    @Test
//    @DisplayName("Should find listings in price range")
//    void shouldFilterByPriceRange() {
//        Listing cheap = createListing("test", BigDecimal.valueOf(10000), "test", "test");
//        Listing expensive = createListing("test2", BigDecimal.valueOf(200000), "test2", "test2");
//
//        listingRepository.saveAll(List.of(cheap, expensive));
//
//        ListingFilterRequest filter = new ListingFilterRequest();
//        filter.setMinPrice(BigDecimal.valueOf(9000));
//        filter.setMaxPrice(BigDecimal.valueOf(100000));
//
//        List<Listing> results = listingRepository.findAll(ListingSpecification.build(filter));
//        assertEquals(1, results.size());
//        assertEquals(new BigDecimal(10000), results.get(0).getPricePerNight());
//    }
//
//    @Test
//    @DisplayName("Should find listings in available date range")
//    void shouldFilterByAvailableDate() {
//        Listing listing = createListing("big cutie house", BigDecimal.valueOf(10), "test", "test");
//        LocalDateTime checkIn = LocalDateTime.of(2026, 5, 15, 15, 30);
//        LocalDateTime checkOut = LocalDateTime.of(2026, 5, 19, 15, 30);
//
//        Booking booking = createBooking(listing, Status.PENDING, checkIn, checkOut);
//
//        listingRepository.save(listing);
//        bookingRepository.save(booking);
//
//        ListingFilterRequest filter = new ListingFilterRequest();
//        filter.setCheckIn(LocalDateTime.of(2026, 5, 15, 15, 30));
//        filter.setCheckOut(LocalDateTime.of(2026, 5, 19, 15, 30));
//
//        List<Listing> results = listingRepository.findAll(ListingSpecification.build(filter));
//        assertEquals(0, results.size());
//    }
//
//    @Test
//    @DisplayName("Shouldn't find listings in partial overlap date range (start)")
//    void shouldFilterByPartialOverlapStart() {
//        Listing listing = createListing("big cutie test house", BigDecimal.valueOf(10), "test", "test");
//        listing.setTitle("big cutie house");
//
//        LocalDateTime checkIn = LocalDateTime.of(2026, 5, 15, 15, 30);
//        LocalDateTime checkOut = LocalDateTime.of(2026, 5, 19, 15, 30);
//
//        Booking booking = createBooking(listing, Status.PENDING, checkIn, checkOut);
//
//        listingRepository.save(listing);
//        bookingRepository.save(booking);
//
//        ListingFilterRequest filter = new ListingFilterRequest();
//        filter.setCheckIn(LocalDateTime.of(2026, 5, 17, 15, 30));
//        filter.setCheckOut(LocalDateTime.of(2026, 5, 29, 15, 30));
//
//        List<Listing> results = listingRepository.findAll(ListingSpecification.build(filter));
//        assertEquals(0, results.size());
//    }
//
//    @Test
//    @DisplayName("Shouldn't find listings in partial overlap date range (end)")
//    void shouldFilterByPartialOverlapEnd() {
//        Listing listing = createListing("big cutie test house", BigDecimal.valueOf(10), "test", "test");
//
//        LocalDateTime checkIn = LocalDateTime.of(2026, 5, 18, 15, 30);
//        LocalDateTime checkOut = LocalDateTime.of(2026, 5, 29, 15, 30);
//
//        Booking booking = createBooking(listing, Status.PENDING, checkIn, checkOut);
//
//        listingRepository.save(listing);
//        bookingRepository.save(booking);
//
//        ListingFilterRequest filter = new ListingFilterRequest();
//        filter.setCheckIn(LocalDateTime.of(2026, 5, 15, 15, 30));
//        filter.setCheckOut(LocalDateTime.of(2026, 5, 19, 15, 30));
//
//        List<Listing> results = listingRepository.findAll(ListingSpecification.build(filter));
//        assertEquals(0, results.size());
//    }
//
//    @Test
//    @DisplayName("Shouldn't find listings in inside request date range")
//    void shouldFilterByBookingInsideTheRequest() {
//        Listing listing = createListing("big cutie testy house", BigDecimal.valueOf(10), "test", "test");
//
//        LocalDateTime checkIn = LocalDateTime.of(2026, 5, 18, 15, 30);
//        LocalDateTime checkOut = LocalDateTime.of(2026, 5, 19, 15, 30);
//
//        Booking booking = createBooking(listing, Status.PENDING, checkIn, checkOut);
//
//        listingRepository.save(listing);
//        bookingRepository.save(booking);
//
//        ListingFilterRequest filter = new ListingFilterRequest();
//        filter.setCheckIn(LocalDateTime.of(2026, 5, 12, 15, 30));
//        filter.setCheckOut(LocalDateTime.of(2026, 5, 22, 15, 30));
//
//        List<Listing> results = listingRepository.findAll(ListingSpecification.build(filter));
//        assertEquals(0, results.size());
//    }
//
//    @Test
//    @DisplayName("Should find listings by booking and show cancelled too")
//    void shouldFilterByBookingAndShowCancelledToo() {
//        Listing listing = createListing("big cutie testy house", BigDecimal.valueOf(10), "test", "test");
//
//        LocalDateTime checkIn =  LocalDateTime.of(2026, 5, 18, 15, 30);
//        LocalDateTime checkOut =  LocalDateTime.of(2026, 5, 19, 15, 30);
//
//        Booking booking = createBooking(listing, Status.CANCELLED, checkIn, checkOut);
//
//        listingRepository.save(listing);
//        bookingRepository.save(booking);
//
//        ListingFilterRequest filter = new ListingFilterRequest();
//        filter.setCheckIn(LocalDateTime.of(2026, 5, 12, 15, 30));
//        filter.setCheckOut(LocalDateTime.of(2026, 5, 22, 15, 30));
//
//        List<Listing> results = listingRepository.findAll(ListingSpecification.build(filter));
//        assertEquals(1, results.size());
//    }
//
//    @Test
//    @DisplayName("Should find and filter by country")
//    void shouldFilterByCountry() {
//        Listing listing = createListing("big cutie testy house", BigDecimal.valueOf(10), "dubai", "uae");
//        Listing listing2 = createListing("small cozy testy house", BigDecimal.valueOf(10), "new york", "usa");
//
//        listingRepository.saveAll(List.of(listing, listing2));
//
//        ListingFilterRequest filter = new ListingFilterRequest();
//        filter.setCountry("u");
//        List<Listing> results = listingRepository.findAll(ListingSpecification.build(filter));
//        assertEquals(2, results.size());
//    }
//
//    @Test
//    @DisplayName("Should find and filter by city")
//    void shouldFilterByCity() {
//        Listing listing = createListing("big cutie testy house", BigDecimal.valueOf(10), "dubai", "uae");
//
//        listingRepository.save(listing);
//
//        ListingFilterRequest filter = new ListingFilterRequest();
//        filter.setCity("dub");
//        List<Listing> results = listingRepository.findAll(ListingSpecification.build(filter));
//        assertEquals(1, results.size());
//    }
//
//    @Test
//    @DisplayName("Should find and filter by every field")
//    void shouldFilterByCityAndPriceAndDates() {
//
//        Listing listing = createListing("big cutie testy house", BigDecimal.valueOf(12500), "dubai", "uae");
//        Listing listing2 = createListing("small cozy testy house", BigDecimal.valueOf(75000), "new york", "usa");
//
//        LocalDateTime checkIn = LocalDateTime.of(2026, 5, 13, 15, 30);
//        LocalDateTime checkOut = LocalDateTime.of(2026, 5, 21, 15, 30);
//
//        Booking booking = createBooking(listing, Status.PENDING, checkIn, checkOut);
//
//        bookingRepository.save(booking);
//        listingRepository.save(listing);
//
//        ListingFilterRequest filter = new ListingFilterRequest();
//        filter.setCity("dubai");
//        filter.setMinPrice(BigDecimal.valueOf(10000));
//        filter.setMaxPrice(BigDecimal.valueOf(60000));
//        filter.setCheckIn(LocalDateTime.of(2026, 5, 22, 15, 30));
//        filter.setCheckOut(LocalDateTime.of(2026, 5, 28, 15, 30));
//
//        List<Listing> results = listingRepository.findAll(ListingSpecification.build(filter));
//
//        assertEquals(1, results.size());
//    }
//
//    @Test
//    @DisplayName("Listing should be available when new booking start exactly end of the old one")
//    void shouldBeAvailableWhenNewBookingStartExactlyAtOldBookingEnd() {
//        Listing listing = createListing("big cutie testy house", BigDecimal.valueOf(100), "Almaty", "kazakhstan");
//        listingRepository.save(listing);
//
//        LocalDateTime checkIn = LocalDateTime.of(2026, 6, 10, 14, 0);
//        LocalDateTime checkOut = LocalDateTime.of(2026, 6, 15, 12, 0);
//        bookingRepository.save(createBooking(listing, Status.PENDING, checkIn, checkOut));
//
//        ListingFilterRequest filter = new ListingFilterRequest();
//        filter.setCheckIn(LocalDateTime.of(2026, 6, 15, 12, 0));
//        filter.setCheckOut( LocalDateTime.of(2026, 6, 20, 12, 0));
//
//        List<Listing> results = listingRepository.findAll(ListingSpecification.build(filter));
//        assertEquals(1, results.size());
//    }
//}
