package com.library.controller;

import com.library.config.SecurityConfig;
import com.library.dto.user.LoginRequest;
import com.library.dto.user.UserRequest;
import com.library.dto.user.UserResponse;
import com.library.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class})
class AuthControllerTests extends AbstractControllerTest {

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

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

            String expectedToken = "mocked-jwt-token";

            when(jwtService.generateToken("test@example.com")).thenReturn(expectedToken);

            mockMvc.perform(post(URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(expectedToken));

            verify(authenticationManager).authenticate(any());
        }

        @Test
        @DisplayName("Return 400, email field invalid (validation check)")
        void incorrectEmailRequest_ShouldReturn400() throws Exception {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("incorrectness");
            loginRequest.setPassword("password123");


            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest))
            ).andDo(print()).andExpect(status().isBadRequest());
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
            UserRequest userRequest = new UserRequest();
            userRequest.setUsername("tester");
            userRequest.setEmail(email);
            userRequest.setPassword("password123");

            String expectedToken = "mocked-jwt-token";

            UserResponse ur = new UserResponse();
            ur.setEmail(email);

            when(authService.createUser(userRequest)).thenReturn(ur);
            when(jwtService.generateToken(email)).thenReturn(expectedToken);

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            ).andExpect(status().isOk())
            .andExpect(content().string(expectedToken));

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