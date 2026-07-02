package com.nooki.controller;

import com.nooki.dto.user.UserResponse;
import com.nooki.security.JwtService;
import com.nooki.security.RefreshTokenService;
import com.nooki.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTests extends AbstractControllerTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @Nested
    @DisplayName("Find user by id (get /user/{id}")
    class getUser {
        private final UUID userId = UUID.randomUUID();
        private final String URL = "/api/v1/user/" + userId;

        @Test
        @DisplayName("valid request find user should return UserResponse and 200")
        @WithMockUser(roles = "USER")
        void validRequest_ShouldReturnUserResponseAnd200() throws Exception {
            UserResponse userResponse = new UserResponse();
            userResponse.setUsername("tester");

            when(userService.getUserById(userId))
                    .thenReturn(userResponse);

            mockMvc.perform(get(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()
            ).andExpect(jsonPath("$.username").value("tester"));
        }

        @Test
        @DisplayName("user not found should return 404")
        @WithMockUser(roles = "USER")
        void userNotFound_shouldReturn404() throws Exception {
            when(userService.getUserById(userId)).thenThrow(EntityNotFoundException.class);

            mockMvc.perform(get(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isNotFound());
        }
    }

//    ADD TEST TO deleteMyAccount
}
