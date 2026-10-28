package com.school.app.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.app.user.Role;
import com.school.app.user.User;
import com.school.app.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void loginSucceedsWithCorrectCredentials() throws Exception {
        User user = new User();
        user.setUsername("test_hm_login");
        user.setPasswordHash(passwordEncoder.encode("Passw0rd!"));
        user.setRole(Role.HEADMASTER);
        user.setActive(true);
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("test_hm_login");
        request.setPassword("Passw0rd!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.role").value("HEADMASTER"));
    }

    @Test
    void loginFailsWithWrongPassword() throws Exception {
        User user = new User();
        user.setUsername("test_admin_login");
        user.setPasswordHash(passwordEncoder.encode("Correct@123"));
        user.setRole(Role.ADMIN);
        user.setActive(true);
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("test_admin_login");
        request.setPassword("Wrong@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointRejectsMissingToken() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
}
