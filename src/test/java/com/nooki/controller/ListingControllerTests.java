package com.nooki.controller;

import com.nooki.dto.PageResponse;
import com.nooki.dto.listing.*;
import com.nooki.dto.location.LocationRequest;
import com.nooki.service.ListingServices.ListingCommandService;
import com.nooki.service.ListingServices.ListingImageService;
import com.nooki.service.ListingServices.ListingQueryService;
import com.nooki.service.ListingServices.ListingSpecificationService;
import com.nooki.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ListingController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testing Listing Controller")
public class ListingControllerTests extends AbstractControllerTest {
    @MockitoBean
    private ListingQueryService queryService;

    @MockitoBean
    private ListingCommandService commandService;

    @MockitoBean
    private ListingImageService imageService;

    @MockitoBean
    private ListingSpecificationService listingSpecificationService;

    @MockitoBean
    private UserService userService;


    @Nested
    @DisplayName("creating listing (post /listing)")
    class createListing{
        private final String URL = "/api/v1/listing";

        @Test
        @DisplayName("Valid request should return 200 and create listing")
        void validRequest_shouldCreateListing() throws Exception {
            ListingRequest listingRequest = new ListingRequest();
            listingRequest.setListingTitle("cutie biggy house");
            listingRequest.setDescription("test testy house");
            listingRequest.setPricePerNight(BigDecimal.valueOf(100));
            listingRequest.setLocationRequest(new LocationRequest());
            listingRequest.setCurrency("USD");
            ListingResponse listingResponse = new ListingResponse();

            when(commandService.createListing(listingRequest)).thenReturn(listingResponse);

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listingRequest))
            ).andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Incorrect price request (validation check)")
        @WithMockUser(roles = "HOST")
        void invalidRequest_shouldReturn403() throws Exception {
            ListingRequest listingRequest = new ListingRequest();
            listingRequest.setListingTitle("cutie biggy house");
            listingRequest.setDescription("test testy house");
            listingRequest.setPricePerNight(BigDecimal.valueOf(-52));
            listingRequest.setLocationRequest(new LocationRequest());

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listingRequest))
            ).andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("find listing (get /listing/{id})")
    class getListing {
        private final UUID listingId = UUID.randomUUID();
        private final String URL = "/api/v1/listing/" + listingId;
        private final String currency = "USD";
        @Test
        @DisplayName("valid request should return 200 and listing")
        @WithMockUser(roles = "USER")
        void validRequest_shouldReturnListing() throws Exception {
            FullListingResponse lr = new FullListingResponse();
            lr.setListingId(listingId);

            when(queryService.getPublicListingById(listingId, currency)).thenReturn(lr);

            mockMvc.perform(get(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()
            ).andExpect(jsonPath("$.listingId").value(lr.getListingId().toString()));
        }

        @Test
        @DisplayName("non-existing listing return 404")
        @WithMockUser(roles = "USER")
        void nonExistingListing_shouldReturn404() throws Exception {
            when(queryService.getPublicListingById(listingId, currency)).thenThrow(EntityNotFoundException.class);

            mockMvc.perform(get(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isNotFound());
        }

    }

    @Nested
    @DisplayName("get my listing (get /listings/my)")
    class getMyListing {

        @Test
        @DisplayName("valid should return List of ListingResponse")
        @WithMockUser(roles = "HOST")
        void validRequest_shouldReturnListOfListingResponse() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            String currency = "USD";

            UUID listingId = UUID.randomUUID();

            List<ListingResponse> lr = new ArrayList<>();
            ListingResponse lr1 = new ListingResponse();
            lr1.setListingId(listingId);
            lr.add(lr1);
            lr.add(new ListingResponse());

            Page<ListingResponse> expectedResponse = new PageImpl<>(lr, pageable, lr.size());

            when(queryService.getUsersListings(any(Pageable.class), eq(currency)))
                    .thenReturn(expectedResponse);

            String URL = "/api/v1/listings/my";

            mockMvc.perform(get(URL)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()
            ).andExpect(jsonPath("$.content[0].listingId").value(listingId.toString()));
        }
    }

    @Nested
    @DisplayName("get all listings (get /listings)")
    class getAllListing {
        @Test
        @DisplayName("valid request, should return all listings (not empty)")
        @WithMockUser(roles = "USER")
        void validRequest_shouldReturnAllListings() throws Exception {
            ShortListingResponse lr = new ShortListingResponse();
            lr.setListingTitle("test1");
            ShortListingResponse lr2 = new ShortListingResponse();
            lr.setListingTitle("test2");
            List<ShortListingResponse> llr = List.of(lr, lr2);

            Pageable pageable = PageRequest.of(0, 3);

            PageResponse<ShortListingResponse> pageResponse = PageResponse.from(new PageImpl<>(llr, pageable, llr.size()));

            when(queryService.getAll(any(Pageable.class))).thenReturn(pageResponse);

            String URL = "/api/v1/listings";
            mockMvc.perform(get(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print()).andExpect(status().isOk()
                    ).andExpect(jsonPath("$.content[0].listingTitle").value(lr.getListingTitle())
                    ).andExpect(jsonPath("$.content[1].listingTitle").value(lr2.getListingTitle()));
        }
    }

    @Nested
    @DisplayName("update listing (put /listings/{id})")
    class updateListing {
        private final UUID listingId = UUID.randomUUID();
        private final String URL = "/api/v1/listing/" + listingId;

        @Test
        @DisplayName("valid request should update listing and return 200")
        @WithMockUser(roles = "HOST")
        void validRequest_shouldUpdateListingAndReturn200() throws Exception {
            UpdateListingRequest listingRequest = new UpdateListingRequest();
            listingRequest.setListingTitle("new listing title");
            listingRequest.setPricePerNight(BigDecimal.valueOf(1000));

            ListingResponse lr = new ListingResponse();
            lr.setListingId(listingId);

            when(commandService.updateListing(listingRequest, listingId))
                    .thenReturn(lr);

            mockMvc.perform(put(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listingRequest))
            ).andExpect(status().isOk()
            ).andExpect(jsonPath("$.listingId").value(lr.getListingId().toString()));
        }

        @Test
        @DisplayName("incorrect request (validation check)")
        @WithMockUser(roles = "HOST")
        void incorrectRequest_shouldReturn400() throws Exception {
            UpdateListingRequest listingRequest = new UpdateListingRequest();
            listingRequest.setListingTitle("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum");
            listingRequest.setPricePerNight(BigDecimal.valueOf(1000));

            mockMvc.perform(put(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listingRequest))
            ).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("not owner of listing")
        @WithMockUser(roles = "HOST")
        void notOwner_shouldReturn409() throws Exception {
            UpdateListingRequest listingRequest = new UpdateListingRequest();
            listingRequest.setListingTitle("new listing title");
            when(commandService.updateListing(listingRequest, listingId)).thenThrow(IllegalStateException.class);

            mockMvc.perform(put(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listingRequest))
            ).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("delete listing (delete /listing/{id})")
    class deleteListing {
        private final UUID listingId = UUID.randomUUID();
        private final String URL = "/api/v1/listing/" + listingId;

        @Test
        @DisplayName("valid request successfull delete")
        @WithMockUser(roles = "ADMIN")
        void validRequest_shouldDeleteListing() throws Exception {
            doNothing().when(commandService).deleteListingById(listingId);

            mockMvc.perform(delete(URL)
                    .with(csrf())
            ).andExpect(status().isOk());

            verify(commandService).deleteListingById(listingId);
        }
    }
}
