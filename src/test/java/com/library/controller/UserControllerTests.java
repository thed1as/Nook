//package com.library.controller;
//
//import com.library.config.SecurityConfig;
//import com.library.dto.user.UserResponse;
//import com.library.service.UserService;
//import jakarta.persistence.EntityNotFoundException;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//
//import java.util.UUID;
//
//import static org.mockito.Mockito.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(UserController.class)
//@Import({SecurityConfig.class})
//public class UserControllerTests extends AbstractControllerTest {
//
//    @MockitoBean
//    private UserService userService;
//
//    @Nested
//    @DisplayName("Find user by id (get /user/{id}")
//    class getUser {
//        private final UUID userId = UUID.randomUUID();
//        private final String URL = "/api/user/" + userId;
//
//        @Test
//        @DisplayName("valid request find user should return UserResponse and 200")
//        @WithMockUser(roles = "USER")
//        void validRequest_ShouldReturnUserResponseAnd200() throws Exception {
//            UserResponse userResponse = new UserResponse();
//            userResponse.setUsername("tester");
//
//            when(userService.getUserById(userId))
//                    .thenReturn(userResponse);
//
//            mockMvc.perform(get(URL)
//                    .with(csrf())
//                    .contentType(MediaType.APPLICATION_JSON)
//            ).andExpect(status().isOk()
//            ).andExpect(jsonPath("$.username").value("tester"));
//        }
//
//        @Test
//        @DisplayName("anonymous user couldn't find user should return 403")
//        void anonymousUser_shouldReturn403() throws Exception {
//
//            mockMvc.perform(get(URL)
//                    .with(csrf())
//                    .contentType(MediaType.APPLICATION_JSON)
//            ).andExpect(status().isForbidden());
//        }
//
//        @Test
//        @DisplayName("user not found should return 404")
//        @WithMockUser(roles = "USER")
//        void userNotFound_shouldReturn404() throws Exception {
//            when(userService.getUserById(userId)).thenThrow(EntityNotFoundException.class);
//
//            mockMvc.perform(get(URL)
//                    .with(csrf())
//                    .contentType(MediaType.APPLICATION_JSON)
//            ).andExpect(status().isNotFound());
//        }
//    }
//
//    @Nested
//    @DisplayName("Becoming host (post /user/become_host)")
//    class becomeHost {
//        private final String URL = "/api/user/me/role";
//
//        @Test
//        @DisplayName("request should return 200 and become Host")
//        @WithMockUser(roles = "USER")
//        void validRequest_shouldReturn200AndBecomeHost() throws Exception {
//            mockMvc.perform(post(URL)
//                    .with(csrf())
//                    .contentType(MediaType.APPLICATION_JSON)
//            ).andExpect(status().isOk());
//            verify(userService).makeHost();
//        }
//
//        @Test
//        @DisplayName("Host trying to become host should return 403")
//        @WithMockUser(roles = "HOST")
//        void alreadyHost_shouldReturn409 () throws Exception {
//            when(userService.makeHost())
//                    .thenThrow(IllegalStateException.class);
//
//            mockMvc.perform(post(URL)
//                    .with(csrf())
//                    .contentType(MediaType.APPLICATION_JSON)
//            ).andExpect(status().isForbidden());
//        }
//
//        @Test
//        @DisplayName("unauthorized should return 403")
//        void unauthorized_shouldReturn403 () throws Exception {
//            mockMvc.perform(post(URL)
//                    .with(csrf())
//                    .contentType(MediaType.APPLICATION_JSON)
//            ).andExpect(status().isForbidden());
//        }
//    }
//}
