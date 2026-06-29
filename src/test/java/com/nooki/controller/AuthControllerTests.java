package com.nooki.controller;

import com.nooki.dto.user.LoginRequest;
import com.nooki.dto.user.UserRequest;
import com.nooki.entity.CustomUserDetails;
import com.nooki.entity.User;
import com.nooki.enums.Role;
import com.nooki.security.RefreshTokenService;
import com.nooki.service.AuthService;
import com.nooki.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTests extends AbstractControllerTest {

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserService userService;

    @Nested
    @DisplayName("Login (/auth/login)")
    class AuthLogin {
        private final String URL = "/auth/login";


        @Test
        @DisplayName("A successful login should return a JWT token.")
        void login_ShouldReturnToken_WhenCredentialsAreValid() throws Exception {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("test@example.com");
            loginRequest.setPassword("password123");

            UUID userId = UUID.randomUUID();
            String role = "USER";
            String expectedAccessToken = "mocked-access-jwt-token";
            String expectedRefreshToken = "mocked-refresh-jwt-token";

            User u = new User();
            u.setUserId(userId);
            u.setRole(Role.USER);

            CustomUserDetails mockUserDetails = mock(CustomUserDetails.class);
            when(mockUserDetails.getUser()).thenReturn(u);

            Authentication mockAuthentication = mock(Authentication.class);
            when(mockAuthentication.getPrincipal()).thenReturn(mockUserDetails);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuthentication);
            when(jwtService.generateAccessToken(userId, role)).thenReturn(expectedAccessToken);
            when(jwtService.generateRefreshToken(userId)).thenReturn(expectedRefreshToken);

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(expectedAccessToken))
                    .andExpect(jsonPath("$.refreshToken").value(expectedRefreshToken))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"));
            ;

            verify(authenticationManager).authenticate(any());
        }

        @Test
        @DisplayName("Return 400, email field invalid (validation check)")
        void incorrectEmailRequest_ShouldReturn400() throws Exception {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("incorrectness");
            loginRequest.setPassword("password123");


            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest))
            ).andExpect(status().isBadRequest());
        }
    }
    @Nested
    @DisplayName("Register (/auth/register)")
    class AuthRegister {
        private final String URL = "/auth/register";

        @Test
        @DisplayName("Successful registration should create a user and return a token")
        void register_ShouldReturnToken_CredintalsAreValid() throws Exception {
            String email = "test@gmail.com";
            UUID userId = UUID.randomUUID();
            String role = "USER";

            UserRequest userRequest = new UserRequest();
            userRequest.setUsername("tester");
            userRequest.setEmail(email);
            userRequest.setPassword("password123");

            String expectedAccessToken = "mocked-access-jwt-token";
            String expectedRefreshToken = "mocked-refresh-jwt-token";

            User u = new User();
            u.setUserId(userId);
            u.setRole(Role.USER);

            when(authService.createUser(userRequest)).thenReturn(u);
            when(jwtService.generateAccessToken(userId, role)).thenReturn(expectedAccessToken);
            when(jwtService.generateRefreshToken(userId)).thenReturn(expectedRefreshToken);

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value(expectedAccessToken))
            .andExpect(jsonPath("$.refreshToken").value(expectedRefreshToken))
            .andExpect(jsonPath("$.tokenType").value("Bearer"));

            verify(authService).createUser(userRequest);
        }

        @Test
        @DisplayName("incorrect email (validation check)")
        void incorrectEmailRequest_ShouldReturn400() throws Exception {
            UserRequest userRequest = new UserRequest();
            userRequest.setEmail("incorrectness");
            userRequest.setPassword("password123");
            userRequest.setUsername("user1233");

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            ).andExpect(status().isBadRequest());
        }
    }
}