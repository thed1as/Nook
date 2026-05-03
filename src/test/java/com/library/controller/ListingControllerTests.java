package com.library.controller;

import com.library.config.SecurityConfig;
import com.library.dto.listing.ListingRequest;
import com.library.dto.listing.ListingResponse;
import com.library.dto.listing.UpdateListingRequest;
import com.library.dto.location.LocationRequest;
import com.library.service.ListingService;
import com.library.service.UserService;
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
@Import({SecurityConfig.class})
@DisplayName("Testing Listing Controller")
public class ListingControllerTests extends AbstractControllerTest {
    @MockitoBean
    private ListingService listingService;

    @MockitoBean
    private UserService userService;

    @Nested
    @DisplayName("creating listing (post /listing)")
    class createListing{
        private final String URL = "/api/listing";

        @Test
        @DisplayName("Valid request should return 200 and create listing")
        @WithMockUser(roles = "HOST")
        void validRequest_shouldCreateListing() throws Exception {
            ListingRequest listingRequest = new ListingRequest();
            listingRequest.setListingTitle("cutie biggy house");
            listingRequest.setDescription("test testy house");
            listingRequest.setPricePerNight(BigDecimal.valueOf(100));
            listingRequest.setLocationRequest(new LocationRequest());
            ListingResponse listingResponse = new ListingResponse();

            when(listingService.createListing(listingRequest)).thenReturn(listingResponse);

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listingRequest))
            ).andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Not the host trying to create a listing, return 403")
        @WithMockUser(roles = "USER")
        void notHost_shoulntCreateListing() throws Exception {
            ListingRequest listingRequest = new ListingRequest();
            listingRequest.setListingTitle("cutie biggy house");
            listingRequest.setDescription("test testy house");
            listingRequest.setPricePerNight(BigDecimal.valueOf(100));
            listingRequest.setLocationRequest(new LocationRequest());

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listingRequest))
            ).andExpect(status().isForbidden());
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
        private final String URL = "/api/listing/" + listingId;

        @Test
        @DisplayName("valid request should return 200 and listing")
        @WithMockUser(roles = "USER")
        void validRequest_shouldReturnListing() throws Exception {
            ListingResponse lr = new ListingResponse();
            lr.setListingId(listingId);

            when(listingService.getListingById(listingId)).thenReturn(lr);

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
            when(listingService.getListingById(listingId)).thenThrow(EntityNotFoundException.class);

            mockMvc.perform(get(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isNotFound());
        }

    }

    @Nested
    @DisplayName("get my listing (get /listing/my)")
    class getMyListing {
        private final String URL = "/api/listings/my";

        @Test
        @DisplayName("valid should return List of ListingResponse")
        @WithMockUser(roles = "HOST")
        void validRequest_shouldReturnListOfListingResponse() throws Exception {
            String email = "test@gmail.com";

            ListingResponse lr = new ListingResponse();
            lr.setListingId(UUID.randomUUID());
            List<ListingResponse> llr = new ArrayList<>();
            llr.add(lr);

            when(userService.getCurrentUserEmail()).thenReturn(email);
            when(listingService.getUsersListings(email)).thenReturn(llr);

            mockMvc.perform(get(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()
            ).andExpect(jsonPath("$[0].listingId")
                    .value(lr.getListingId().toString()));
        }

        @Test
        @DisplayName("not host trying to check his listings should return 403")
        @WithMockUser(roles = "USER")
        void notHost_shouldReturn403() throws Exception {
            mockMvc.perform(get(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("get all listings (get /listings)")
    class getAllListing {
        @Test
        @DisplayName("valid request, should return all listings (not empty)")
        @WithMockUser(roles = "USER")
        void validRequest_shouldReturnAllListings() throws Exception {
            ListingResponse lr = new ListingResponse();
            lr.setListingTitle("test1");
            ListingResponse lr2 = new ListingResponse();
            lr.setListingTitle("test2");
            List<ListingResponse> llr = List.of(lr, lr2);

            Pageable pageable = PageRequest.of(0, 3);

            Page<ListingResponse> pageResponse = new PageImpl<>(llr, pageable, llr.size());

            when(listingService.getAll(any(Pageable.class))).thenReturn(pageResponse);

            String URL = "/api/listings";
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
        private final String URL = "/api/listing/" + listingId;

        @Test
        @DisplayName("valid request should update listing and return 200")
        @WithMockUser(roles = "HOST")
        void validRequest_shouldUpdateListingAndReturn200() throws Exception {
            UpdateListingRequest listingRequest = new UpdateListingRequest();
            listingRequest.setListingTitle("new listing title");
            listingRequest.setPricePerNight(BigDecimal.valueOf(1000));

            ListingResponse lr = new ListingResponse();
            lr.setListingId(listingId);

            when(listingService.updateListing(listingRequest, listingId))
                    .thenReturn(lr);

            mockMvc.perform(put(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listingRequest))
            ).andExpect(status().isOk()
            ).andExpect(jsonPath("$.listingId").value(lr.getListingId().toString()));
        }

        @Test
        @DisplayName("not host so shouldn't update list should return 404")
        @WithMockUser(roles = "USER")
        void notHost_shouldReturn405() throws Exception {
            UpdateListingRequest listingRequest = new UpdateListingRequest();
            listingRequest.setListingTitle("new listing title");
            listingRequest.setPricePerNight(BigDecimal.valueOf(1000));

            mockMvc.perform(put(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listingRequest))
            ).andExpect(status().isForbidden());
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
            when(listingService.updateListing(listingRequest, listingId)).thenThrow(IllegalStateException.class);

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
        private final String URL = "/api/listing/" + listingId;

        @Test
        @DisplayName("valid request successfull delete")
        @WithMockUser(roles = "ADMIN")
        void validRequest_shouldDeleteListing() throws Exception {
            doNothing().when(listingService).deleteListingById(listingId);

            mockMvc.perform(delete(URL)
                    .with(csrf())
            ).andExpect(status().isOk());

            verify(listingService).deleteListingById(listingId);
        }

        @Test
        @DisplayName("User shouldn't able to delete listing return 403")
        @WithMockUser(roles = "USER")
        void notAdmin_shouldReturn403() throws Exception {
            mockMvc.perform(delete(URL)
                    .with(csrf())
            ).andExpect(status().isForbidden());

            verify(listingService, never()).deleteListingById(any());
        }
    }
}
